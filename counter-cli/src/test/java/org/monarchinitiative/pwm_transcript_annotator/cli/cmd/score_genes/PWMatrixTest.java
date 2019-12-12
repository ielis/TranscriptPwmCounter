package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PWMatrixTest {

    private static List<List<Double>> DATA = List.of(
            List.of(1., 5.),
            List.of(2., 6.),
            List.of(3., 7.),
            List.of(4., 8.));

    private PWMatrix matrix;

    @BeforeEach
    void setUp() {
        matrix = new PWMatrix("theName", .2, DATA);
    }

    @Test
    void create() {
        assertThat(matrix.getName(), is("theName"));
        assertThat(matrix.getThreshold(), is(.2));
        assertThat(matrix.getMatrix(), is(DATA));
    }

    @Test
    void getDoubleMatrix() {
        final DoubleMatrix doubleMatrix = matrix.getDoubleMatrix();
        assertThat(doubleMatrix, is(new DoubleMatrix(new double[][]{
                {1., 5.},
                {2., 6.},
                {3., 7.},
                {4., 8.}})));
    }
}