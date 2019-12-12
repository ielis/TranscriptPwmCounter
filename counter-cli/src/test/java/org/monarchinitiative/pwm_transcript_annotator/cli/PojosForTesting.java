package org.monarchinitiative.pwm_transcript_annotator.cli;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

public class PojosForTesting {


    public static ReferenceDictionary referenceDictionary() {
        ReferenceDictionaryBuilder builder = new ReferenceDictionaryBuilder();

        builder.putContigID("chr1", 1);
        builder.putContigName(1, "chr1");
        builder.putContigLength(1, 10_000);

        builder.putContigID("chr2", 2);
        builder.putContigName(2, "chr2");
        builder.putContigLength(2, 100_000);

        builder.putContigID("chr3", 3);
        builder.putContigName(3, "chr3");
        builder.putContigLength(3, 200_000);
        return builder.build();
    }

    public static SplicingTranscript getTranscriptWithTwoExons(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("FIRST")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 100, 115))
                // two exons, one intron
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 100, 105))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 105, 110))
                        .build())
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 110, 115))
                        .build())
                .build();
    }

    public static SequenceInterval sequenceInterval(ReferenceDictionary referenceDictionary) {
        return SequenceInterval.builder()
                .interval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 90, 120))
                // with respect to the transcript above
                .sequence("ACGTACGTAC" + // upstream from tx
                        "acgta" + // first exon
                        "cgtac" + // intron
                        "ACGGA" + // last exon
                        "CGTAC") // downstream from tx
                .build();
    }
}
