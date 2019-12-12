package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PwmParserTest {

    private static final Path PWM_PATH = Paths.get(PwmParserTest.class.getResource("pwm.yaml").getPath());

    @Test
    void parseAll() throws Exception {
        final Map<String, PWMatrix> stringPWMatrixMap = PwmParser.parseAll(PWM_PATH);
        assertThat(stringPWMatrixMap.keySet(), hasItems("ESEFINDER_SRSF1", "ESEFINDER_SRSF1_IGM"));

        final PWMatrix matrix = stringPWMatrixMap.get("ESEFINDER_SRSF1");
        assertThat(matrix.getName(), is("ESEFINDER_SRSF1"));
        assertThat(matrix.getThreshold(), is(1.956));
        assertThat(matrix.getMatrix(), is(
                List.of(List.of(-1.14, .62, -1.58, 1.32, -1.58, -1.58, .62),
                        List.of(1.37, -1.1, .73, .33, .94, -1.58, -1.58),
                        List.of(-.21, .17, .48, -1.58, .33, .99, -.11),
                        List.of(-1.58, -.5, -1.58, -1.13, -1.58, -1.13, .27))));
    }
}