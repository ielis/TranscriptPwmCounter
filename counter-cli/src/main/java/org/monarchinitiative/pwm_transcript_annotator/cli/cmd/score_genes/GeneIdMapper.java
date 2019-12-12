package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * The motivation behind this class is to allow working with different versions of gene definitions.
 * <p>
 * The input file might contain list of genes, among others containing a gene `ENSG00000123456.8`. During the analysis,
 * we need to get list of transcript ids for this gene. For this task, we use dump from Icons database which contains
 * Gencode data.
 * </p>
 * <p>
 * If the Gencode data and input file are out of sync, then the gene id might have a different version. In case of the
 * gene above, the id might look like `ENSG00000123456.11`. Naive search might not be able to pair these ids even though
 * it should.
 * </p>
 * <p>
 * The purpose of this class is to fetch transcripts for a gene id based on <em>base</em> ENSEBML gene id (e.g.
 * `ENSG00000123456`).
 * </p>
 */
class GeneIdMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneIdMapper.class);

    private static final Pattern ENSG_PATTERN = Pattern.compile("(?<base>ENSG[\\d]{11})\\.\\d+(_PAR_Y)?");

    private final Multimap<String, String> geneTxMultimap;

    private GeneIdMapper(Multimap<String, String> geneTxMultimap) {
        this.geneTxMultimap = geneTxMultimap;
    }

    /**
     * Create mapper using data from CSV file, where first column contains gene ids while the second column contains
     * transcript ids for the gene.
     *
     * @param csvPath path to CSV file with 2 columns: `ENSG`, `ENST`
     * @return the mapper
     * @throws IOException in case of I/O problems
     */
    public static GeneIdMapper fromCsv(Path csvPath) throws IOException {
        final Multimap<String, String> geneTxMultimap = ArrayListMultimap.create();
        try (final BufferedReader reader = Files.newBufferedReader(csvPath);
             final CSVParser csvParser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .parse(reader)) {
            StreamSupport.stream(csvParser.spliterator(), false)
                    .forEach(csv -> {
                        final Matcher matcher = ENSG_PATTERN.matcher(csv.get("ENSG"));
                        if (matcher.matches()) {
                            geneTxMultimap.put(matcher.group("base"), csv.get("ENST"));
                        }
                    });
        }
        LOGGER.info("Read {} genes", geneTxMultimap.keySet().size());
        return new GeneIdMapper(geneTxMultimap);
    }

    public Collection<String> getTranscriptIds(String geneId) {
        final Matcher matcher = ENSG_PATTERN.matcher(geneId);
        if (matcher.matches()) {
            // strip `.3` suffix from `ENSG00000123456.3`
            final String baseId = matcher.group("base");
            return geneTxMultimap.get(baseId);
        } else {
            LOGGER.warn("Gene id `{}` does not match expected ENSEMBL pattern", geneId);
            return Collections.emptySet();
        }
    }
}
