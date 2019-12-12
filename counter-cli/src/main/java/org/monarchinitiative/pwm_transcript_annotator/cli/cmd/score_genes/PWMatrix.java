package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import org.jblas.DoubleMatrix;

import java.util.List;
import java.util.Objects;

/**
 * This class is a simple representation of position-weight matrix (PWM). The PWM attributes are: <ul><li><b>name</b> -
 * the name of the PWM</li> <li><b>threshold</b> - score of nucleotide sequence must higher than threshold in order to
 * be considered as positive example of this PWM</li><li><b>matrix</b> - internal representation of PWM values used for
 * scoring of nucleotide sequences</li></ul> Created by Daniel Danis on 4/26/17.
 */
public class PWMatrix {

    private String name;

    private double threshold;

    private List<List<Double>> matrix;

    PWMatrix(String name, double threshold, List<List<Double>> matrix) {
        this.name = name;
        this.threshold = threshold;
        this.matrix = matrix;
    }

    PWMatrix() {
    }

    /**
     * Map {@link List} to {@link DoubleMatrix} and perform sanity checks:
     * <ul>
     * <li>entries for all 4 nucleotides must be present</li>
     * <li>entries for all nucleotides must have the same size</li>
     * <li>probabilities/frequencies at each position must sum up to 1</li>
     * </ul>
     *
     * @param vals This list should contain another four lists. Each inner list represents one of the nucleotides
     *             A, C, G, T in this order
     * @return {@link DoubleMatrix} with data from <code>io</code>
     */
    private static DoubleMatrix mapToDoubleMatrix(List<List<Double>> vals) {
        if (vals == null)
            throw new IllegalArgumentException("Unable to create matrix with 0 rows");

        if (vals.size() != 4)
            throw new IllegalArgumentException("Matrix does not have 4 rows for 4 nucleotides");

        // all four lists must have the same size
        int size = vals.get(0).size();
        if (vals.stream().anyMatch(inner -> inner.size() != size))
            throw new IllegalArgumentException("Rows of the matrix do not have the same size");

        // checks are done
        DoubleMatrix dm = new DoubleMatrix(vals.size(), vals.get(0).size());
        for (int rowIdx = 0; rowIdx < vals.size(); rowIdx++) {
            List<Double> row = vals.get(rowIdx);
            for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                dm.put(rowIdx, colIdx, row.get(colIdx));
            }
        }
        return dm;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<Double>> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<List<Double>> matrix) {
        this.matrix = matrix;
    }

    public DoubleMatrix getDoubleMatrix() {
        return mapToDoubleMatrix(matrix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PWMatrix pwMatrix = (PWMatrix) o;
        return Double.compare(pwMatrix.threshold, threshold) == 0 &&
                Objects.equals(name, pwMatrix.name) &&
                Objects.equals(matrix, pwMatrix.matrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, threshold, matrix);
    }

    @Override
    public String toString() {
        return "PWMatrix{" +
                "name='" + name + '\'' +
                ", threshold=" + threshold +
                ", matrix=" + matrix +
                '}';
    }

}
