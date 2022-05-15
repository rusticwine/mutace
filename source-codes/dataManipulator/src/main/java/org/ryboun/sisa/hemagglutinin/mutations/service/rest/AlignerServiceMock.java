package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.context.annotation.Profile;

@Profile("dev-mock")
public class AlignerServiceMock implements Aligner {


    @Override
    public String alignWithSingleReference(AlignDto alignDto) {
        return null;
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
