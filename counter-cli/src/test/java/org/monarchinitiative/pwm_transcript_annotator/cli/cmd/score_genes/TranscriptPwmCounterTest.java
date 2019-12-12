package org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.pwm_transcript_annotator.cli.PojosForTesting;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TranscriptPwmCounterTest {

    private static final ReferenceDictionary REFERENCE_DICTIONARY = PojosForTesting.referenceDictionary();
    private static final SplicingTranscript TRANSCRIPT = PojosForTesting.getTranscriptWithTwoExons(REFERENCE_DICTIONARY);
    private static final SequenceInterval SEQUENCE_INTERVAL = PojosForTesting.sequenceInterval(REFERENCE_DICTIONARY);
    private TranscriptPwmCounter counter;

    @BeforeEach
    void setUp() {
        counter = new TranscriptPwmCounter(List.of(new PWMatrix("first", 3.,
                List.of(List.of(1., 0., 0.),
                        List.of(0., 2., 0.),
                        List.of(0., 0., 0.),
                        List.of(0., 0., 3.)))));
    }

    @Test
    void count() {
        final ImmutableMap<String, Long> counts = counter.count(TRANSCRIPT, SEQUENCE_INTERVAL);
        assertThat(counts.size(), is(1));
        assertThat(counts.keySet(), hasItem("first"));
        assertThat(counts.values(), hasItem(3L));
    }
}