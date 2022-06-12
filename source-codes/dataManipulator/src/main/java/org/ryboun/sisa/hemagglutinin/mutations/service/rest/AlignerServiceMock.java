package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@Profile("dev-mock")
public class AlignerServiceMock implements Aligner {

    private static final String REFERENCE_SEQUENCE_FASTA = "sequences/referenceSequence.fasta";
    private static final String TEST_SEQUENCES_RAW_FASTA = "sequences/rawSequences_test1.fasta";
    private static final String TEST_SEQUENCES_ALIGNED_FASTA = "sequences/alignedSequences_test1.fasta";
    public static final String ALIGN_JOB_ID = "_pm1_sdfsd_alignJobId";

    @PostConstruct
    void initMockData() throws IOException {
        String sequencesStr = Utils.loadResourceToString(TEST_SEQUENCES_RAW_FASTA);
    }

    @Override
    public String alignWithSingleReference(AlignDto alignDto) {
        return ALIGN_JOB_ID;
    }

    @Override
    public String getJobResult(String jobId) {
        return null;
    }

    @Override
    public String checkJobStatus(String jobId) {
        return null;
    }
}
