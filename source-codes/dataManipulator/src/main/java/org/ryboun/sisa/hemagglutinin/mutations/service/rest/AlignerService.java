package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.module.alignment.AlignDto;

public interface AlignerService {

    String testAlign1_submitJob(AlignDto body);

    String testAlign1_getResult(String jobId);

    String testAlign1_checkJobStatus(String jobId);
}
