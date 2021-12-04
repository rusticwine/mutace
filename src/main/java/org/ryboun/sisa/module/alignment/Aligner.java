package org.ryboun.sisa.module.alignment;

import org.springframework.stereotype.Service;


public interface Aligner {

    String alignWithSingleReference(AlignDto alignDto);

    public String getJobResult(String jobId);

    public String checkJobStatus(String jobId);
}
