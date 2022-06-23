package org.ryboun.sisa.hemagglutinin.mutations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequences;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

class ParsersTest {

    private static final String REFERENCE_SEQUENCE_FASTA = "sequences/referenceSequence.fasta";
    private static final String TEST_SEQUENCES_RAW_FASTA = "sequences/rawSequences.fasta";
    private static final String TEST_SEQUENCES_ALIGNED_FASTA = "sequences/alignedSequences_test1.fasta";

    @Test
    void loadFastaSequenceFromResource() throws IOException {
        List<ReferenceSequence> referenceSequences = Parsers.loadReferenceSequenceFromResource();
        Assertions.assertNotNull(referenceSequences);
        Assertions.assertEquals(1, referenceSequences.size(), "One reference sequence is expected");
        Assertions.assertEquals("AAR02640.2", referenceSequences.get(0).getAccver(), "accver does not correspond");
        Assertions.assertEquals("hemagglutinin", referenceSequences.get(0).getProtein());
        Assertions.assertEquals("Influenza A virus (A/Netherlands/219/2003(H7N7))", referenceSequences.get(0).getOrganism());
        Assertions.assertEquals(562, referenceSequences.get(0).getSequence().length(), "Sequence has unexpected size");
    }

    @Test
    void parseFastaSequencesTest() throws IOException {
        String sequencesStr = Utils.loadResourceToString(TEST_SEQUENCES_RAW_FASTA);
        List<Sequence> sequences = Parsers.parseFastaSequences(sequencesStr);
        Assertions.assertNotNull(sequences);
        Assertions.assertEquals(1209, sequences.size(), "Parsed sequence count does not correspond");
    }

    @Test
    void parseAlignedSequencesTest() throws IOException {
        final String FIRST_SEQUENCE_BEGINING = "MNTQILVFALVASIPTNA";
        final String FIRST_SEQUENCE_END = "MRCTICI";
        String alignedSequencesStr = Utils.loadResourceToString(TEST_SEQUENCES_ALIGNED_FASTA);
        AlignedSequences alignedSequences = Parsers.parseAlignedSequences(alignedSequencesStr, sequencesProcessingStatusMock);

        System.out.println("alignedSequences: " + alignedSequences.getAlignedSequences().size());

        Assertions.assertNotNull(alignedSequences);
        Assertions.assertEquals(101, alignedSequences.getAlignedSequences().size(), "Parsed sequence count does not correspond");
        String firstAlignedSequence = alignedSequences.getAlignedSequences().get(0).getAlignedSequence();
        Assertions.assertTrue(firstAlignedSequence.startsWith(FIRST_SEQUENCE_BEGINING), "First aligned sequence (the begining) 'does not compute'");
        Assertions.assertTrue(firstAlignedSequence.endsWith(FIRST_SEQUENCE_END), "First aligned sequence (the end) 'does not compute'");

    }

    private static ReferenceSequence referenceSequence;
    private static SequencesProcessingStatus sequencesProcessingStatusMock;
    private static List<Sequence> rawFastaSequences;

    private static final String ALIGNEMENT_ID_MOCK = "alignment_id_mock";
    @BeforeAll
    static void init() throws IOException {

        referenceSequence = Parsers.loadReferenceSequenceFromResource().get(0);
        String sequencesStr = Utils.loadResourceToString(TEST_SEQUENCES_RAW_FASTA);
        rawFastaSequences = Parsers.parseFastaSequences(sequencesStr);

        sequencesProcessingStatusMock = SequencesProcessingStatus.builder()
                .status(Sequence.STATUS.DOWNLOADED)
                .referenceSequence(referenceSequence)
                .alidnmentSubmitted(LocalDateTime.now())
                .rawSequences(rawFastaSequences)
                .rawSequenceCount(rawFastaSequences.size()) //WHY ALL???
                .alignJobId(ALIGNEMENT_ID_MOCK)
                .build();
    }
}