package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import com.google.common.collect.ImmutableMap;
import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class TranscriptPwmCounter {

    private static final Map<Character, Integer> IDX_MAPPER = Map.of(
            'A', 0, 'a', 0,
            'C', 1, 'c', 1,
            'G', 2, 'g', 2,
            'T', 3, 't', 3
    );

    private final Map<String, PWMatrix> matrixMap = new HashMap<>();

    public TranscriptPwmCounter(Collection<PWMatrix> matrices) {
        matrices.forEach(matrix -> matrixMap.put(matrix.getName(), matrix));
    }

    /**
     * Create subsequences/windows of size <code>'ws'</code> from nucleotide <code>sequence</code>.
     *
     * @param sequence {@link String} with nucleotide sequence to generate subsequences from
     * @param ws       window size
     * @return {@link Stream} of {@link String}s - subsequences of given <code>sequence</code> with length
     * <code>ws</code> or empty {@link Stream}, if '<code>ws</code> > <code>sequence.length()</code>'
     */
    static Stream<String> slidingWindow(String sequence, int ws) {
        return ws > sequence.length()
                ? Stream.empty()
                : IntStream.range(0, sequence.length() - ws + 1)
                .boxed()
                .map(idx -> sequence.substring(idx, idx + ws));
    }

    private static double scoreWindow(String window, DoubleMatrix matrix) {
        double score = 0.;
        final char[] bases = window.toCharArray();
        for (int column = 0; column < bases.length; column++) {
            score += matrix.get(IDX_MAPPER.get(bases[column]), column);
        }
        return score;
    }

    public ImmutableMap<String, Long> count(SplicingTranscript transcript, SequenceInterval sequence) {
        final List<String> exonSequences = transcript.getExons().stream()
                .map(SplicingExon::getInterval)
                .map(sequence::getSubsequence)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Map<String, Long> counter = new HashMap<>();
        for (String matrixName : matrixMap.keySet()) {
            final PWMatrix matrix = matrixMap.get(matrixName);
            final DoubleMatrix doubleMatrix = matrix.getDoubleMatrix();
            long count = 0;
            for (String exonSequence : exonSequences) {
                count += slidingWindow(exonSequence, doubleMatrix.columns)
                        .map(window -> scoreWindow(window, doubleMatrix))
                        .filter(score -> score >= matrix.getThreshold())
                        .count();
            }
            counter.put(matrixName, count);
        }
        return ImmutableMap.copyOf(counter);
    }
}
