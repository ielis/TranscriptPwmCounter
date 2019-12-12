module org.monarchinitiative.pwm_transcript_annotator.cli {
    requires org.monarchinitiative.threes.core;
    requires org.monarchinitiative.threes.autoconfigure;
    requires xyz.ielis.hyperutil.reference;
    requires jannovar.core;

    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.boot;

    requires argparse4j;
    requires commons.csv;
    requires com.google.common;
    requires snakeyaml;
    requires jblas;
    requires org.slf4j;

    opens org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;
//    opens org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes to snakeyaml;
}