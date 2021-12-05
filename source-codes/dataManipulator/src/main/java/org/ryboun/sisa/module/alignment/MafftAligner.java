package org.ryboun.sisa.module.alignment;

import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequencesProcessingRepository;
import org.ryboun.sisa.hemagglutinin.mutations.service.rest.EbiAligner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MafftAligner implements Aligner {

    @Autowired
    SequencesProcessingRepository sequencesProcessingRepository;

    @Autowired
    EbiAligner ebiAligner;


    /**
     * Method takes object containing (also) sequences
     * @param body
     * @return Job ID of an alidnment job that has been launched
     */
    @Override
    public String alignWithSingleReference(AlignDto body) {
        final String alignJobId = ebiAligner.testAlign1_submitJob(body);
        SequencesProcessingStatus sequencesProcessingStatus = SequencesProcessingStatus.builder()
                .sequences(body.getSequences())
                .status(SequencesProcessingStatus.STATUS.ALIGNING)
                .jobId(alignJobId)
                .build();

        sequencesProcessingRepository.save(sequencesProcessingStatus);

        return alignJobId;
    }

    /**
     *
     * @param jobId Ebi job running remote alignment
     * @return Aligned sequences
     */
    public String getJobResult(String jobId) {
        return ebiAligner.testAlign1_getResult(jobId);
    }

    /**
     *
     * @param jobId Ebi job running remote alignment
     * @return job status at Ebi site
     */
    public String checkJobStatus(String jobId) {
        return ebiAligner.testAlign1_checkJobStatus(jobId);
    }
}
