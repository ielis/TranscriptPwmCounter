package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.pwm_transcript_annotator.cli.cmd.Command;
import org.monarchinitiative.pwm_transcript_annotator.cli.cmd.CommandException;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ScoreGenesCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreGenesCommand.class);

    private static final Pattern ENSG_PATTERN = Pattern.compile("ENSG[\\d]{11}\\.\\d");

    /**
     * How many more base pairs to fetch from FASTA file for transcript.
     */
    private static final int TX_PADDING = 100;

    private final GenomeSequenceAccessor genomeSequenceAccessor;

    private final SplicingTranscriptSource splicingTranscriptSource;

    public ScoreGenesCommand(GenomeSequenceAccessor genomeSequenceAccessor,
                             SplicingTranscriptSource splicingTranscriptSource) {
        this.genomeSequenceAccessor = genomeSequenceAccessor;
        this.splicingTranscriptSource = splicingTranscriptSource;
    }

    public static void setupSubparsers(Subparsers subparsers) {
        Subparser subparser = subparsers.addParser("score-genes")
                .setDefault("cmd", "score-genes")
                .help("score genes from provided file");

        subparser.addArgument("genes")
                .help("path to file with one ENSEMBL gene ID per line");

        subparser.addArgument("output")
                .help("where to write the output file");
    }

    @Override
    public void run(Namespace namespace) throws CommandException {
        final Path genesPath = Paths.get(namespace.getString("genes"));
        final Path outputPath = Paths.get(namespace.getString("output"));

        // 0 - read gene definitions
        LOGGER.info("Reading gene ids from `{}`", genesPath);
        List<String> genes = new ArrayList<>();
        try (final BufferedReader reader = Files.newBufferedReader(genesPath)) {
            reader.lines()
                    .filter(ENSG_PATTERN.asPredicate())
                    .forEach(genes::add);
        } catch (IOException e) {
            LOGGER.warn("Error: ", e);
            throw new CommandException(e);
        }
        LOGGER.info("Read {} gene ids", genes.size());

        // 1 - Read gene to transcripts map
        final Path geneTranscript = Paths.get(ScoreGenesCommand.class.getResource("/gencode.v32.hg38.gene_transcript.csv").getPath());
        LOGGER.info("Reading gene to transcript map from `{}`", geneTranscript);

        final GeneIdMapper geneToTranscriptMapper;
        try {
            geneToTranscriptMapper = GeneIdMapper.fromCsv(geneTranscript);
        } catch (IOException e) {
            LOGGER.warn("Error: ", e);
            throw new CommandException(e);
        }

        // 2 - read PWM definitions
        final Path pwmPath = Paths.get(ScoreGenesCommand.class.getResource("/pwm.yaml").getPath());
        LOGGER.info("Reading PWMs from `{}`", pwmPath);

        final List<PWMatrix> pwmList;
        try (final BufferedReader reader = Files.newBufferedReader(pwmPath)) {
            pwmList = new ArrayList<>(PwmParser.parseAll(reader).values());
        } catch (IOException e) {
            LOGGER.warn("Error: ", e);
            throw new CommandException(e);
        }
        LOGGER.info("Read {} PWMs", pwmList.size());

        /* 3 - For each gene:
               - get transcripts
               - determine coordinates of the largest transcript and fetch FASTA sequence
               - for each transcript:
                 - for each exon:
                   - apply sliding window on a sequence and count number of positive ESE matches
                 - write the result to the output file
         */
        final TranscriptPwmCounter counter = new TranscriptPwmCounter(pwmList);
        final ReferenceDictionary rd = genomeSequenceAccessor.getReferenceDictionary();

        /*
         the map keys:
          - gene
            - transcript id
              - PWM name: count
         */
        final Map<String, Map<String, Map<String, Long>>> countMap = new HashMap<>();
        List<String> unknownGenes = new ArrayList<>();
        LOGGER.info("Annotating {} genes", genes.size());
        final Instant start = Instant.now();
        for (String geneId : genes) {
            final Collection<String> txIds = geneToTranscriptMapper.getTranscriptIds(geneId);
            if (txIds.isEmpty()) {
                // we do not know about transcripts of this gene. No transcript is presnt in the table managed by
                // gene to transcript mapper
                unknownGenes.add(geneId);
                continue;
            }
            // get splicing transcripts for a gene from splicing database
            final List<SplicingTranscript> transcripts = txIds.stream()
                    .map(tx -> splicingTranscriptSource.fetchTranscriptByAccession(tx, rd))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (transcripts.isEmpty()) {
                LOGGER.warn("No splicing transcripts found for gene `{}` and tx ids `{}`", geneId, String.join(", ", txIds));
                continue;
            }

            // get enough reference sequence for the gene
            final GenomeInterval paddedLongestTxRegion = transcripts.stream()
                    .max(Comparator.comparing(SplicingTranscript::getTxLength))
                    .get()
                    .getTxRegionCoordinates()
                    .withMorePadding(TX_PADDING, TX_PADDING);
            final Optional<SequenceInterval> sequenceIntervalOptional = genomeSequenceAccessor.fetchSequence(paddedLongestTxRegion);
            if (sequenceIntervalOptional.isEmpty()) {
                LOGGER.warn("Unable to fetch enough sequence for `{}`:`{}`", geneId, paddedLongestTxRegion);
                continue;
            }
            final SequenceInterval sequenceInterval = sequenceIntervalOptional.get();

            //
            Map<String, Map<String, Long>> txCountMap = new ConcurrentHashMap<>();
            transcripts.parallelStream()
                    .forEach(tx -> {
                        final ImmutableMap<String, Long> counts = counter.count(tx, sequenceInterval);
                        txCountMap.put(tx.getAccessionId(), counts);
                    });
            countMap.put(geneId, txCountMap);
        }
        final Instant end = Instant.now();
        final Duration time = Duration.between(start, end);
        LOGGER.info("Annotation took {}.{}s", time.getSeconds(), time.getNano() / 1_000_000);
        LOGGER.warn("No transcripts found for {} genes: `{}`", unknownGenes.size(), String.join(", ", unknownGenes));

        // 4 - write out results
        List<String> header = new ArrayList<>();
        header.add("ENSG");
        header.add("ENST");
        final List<String> pwmNames = pwmList.stream()
                .map(PWMatrix::getName)
                .sorted()
                .collect(Collectors.toList());
        header.addAll(pwmNames);
        LOGGER.info("Writing results to `{}`", outputPath);
        try (final BufferedWriter writer = Files.newBufferedWriter(outputPath);
             final CSVPrinter csvPrinter = CSVFormat.DEFAULT
                     .withHeader(header.toArray(String[]::new))
                     .print(writer)) {
            csvPrinter.printComment("Created by TranscriptPwmCounter v0.0.1");
            for (String geneId : countMap.keySet()) {
                final Map<String, Map<String, Long>> txCounts = countMap.get(geneId);
                if (txCounts == null) {
                    LOGGER.warn("No data found for gene `{}`", geneId);
                    continue;
                }
                for (String txId : txCounts.keySet()) {
                    final Map<String, Long> counts = txCounts.get(txId);
                    csvPrinter.print(geneId);
                    csvPrinter.print(txId);
                    for (String pwmName : pwmNames) {
                        csvPrinter.print(counts.get(pwmName));
                    }
                    csvPrinter.println();
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error: ", e);
            throw new CommandException(e);
        }
    }

}
