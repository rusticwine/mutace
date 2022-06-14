package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.hemagglutinin.mutations.Parsers;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequences;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("dev-mock")
public class AlignerServiceMock implements Aligner {

    private static final String REFERENCE_SEQUENCE_FASTA = "sequences/referenceSequence.fasta";
    private static final String TEST_SEQUENCES_RAW_FASTA = "sequences/rawSequences.fasta";
    private static final String TEST_SEQUENCES_ALIGNED_FASTA = "sequences/alignedSequences_test1.fasta";
    public static final String ALIGN_JOB_ID = "_pm1_sdfsd_alignJobId";
    @NotEmpty
    private List<Sequence> sequncesDownloaded;

    @Deprecated
    private AlignedSequences alignedSequences;
    @Deprecated
    private List<ReferenceSequence> referenceSequences;

    @Deprecated
    private SequencesProcessingStatus sequencesProcessingStatusMock;

    public final String ALIGNEMENT_ID_MOCK = "alignment_id_mock";

    private String sequencesStr;
    private String sequencesAlignedStr;
    @PostConstruct
    void initMockData() throws IOException {
        sequencesStr = Utils.loadResourceToString(TEST_SEQUENCES_RAW_FASTA);
        sequncesDownloaded = Parsers.parseFastaSequences(sequencesStr);

        referenceSequences = Parsers.loadReferenceSequenceFromResource();

        sequencesProcessingStatusMock = SequencesProcessingStatus.builder()
                .status(Sequence.STATUS.DOWNLOADED)
                .referenceSequence(referenceSequences.get(0))
                .alidnmentSubmitted(LocalDateTime.now())
                .rawSequences(sequncesDownloaded)
                .alignJobId(ALIGNEMENT_ID_MOCK)
                .build();
        sequencesAlignedStr = Utils.loadResourceToString(TEST_SEQUENCES_ALIGNED_FASTA);
        alignedSequences = Parsers.parseAlignedSequences(sequencesAlignedStr, sequencesProcessingStatusMock);
    }

    @Override
    public String alignWithSingleReference(AlignDto alignDto) {
        return ALIGN_JOB_ID;
    }

    @Override
    public String getJobResult(String jobId) {
        return sequencesAlignedStr;
    }

    @Override
    public String checkJobStatus(String jobId) {
        return "FINISHED";
    }
}
