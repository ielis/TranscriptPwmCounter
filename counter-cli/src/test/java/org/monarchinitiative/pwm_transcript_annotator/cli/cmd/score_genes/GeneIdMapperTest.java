package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

class GeneIdMapperTest {

    private static final Path TEST_CSV = Paths.get(GeneIdMapperTest.class.getResource("gene_transcript.csv").getPath());

    private GeneIdMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = GeneIdMapper.fromCsv(TEST_CSV);
    }

    @Test
    void map() {
        final Collection<String> txs = mapper.getTranscriptIds("ENSG00000000457.14");
        System.out.println(txs);
        assertThat(txs, hasSize(3));
        assertThat(txs, hasItems("ENST00000367771.11", "ENST00000367770.5", "ENST00000367772.8"));
    }
}