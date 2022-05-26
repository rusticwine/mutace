package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev-mock")
public class AlignerServiceMock implements Aligner {

    public static final String ALIGN_JOB_ID = "_pm1_sdfsd_alignJobId";

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
