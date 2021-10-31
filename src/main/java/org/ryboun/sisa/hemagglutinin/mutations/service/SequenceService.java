package org.ryboun.sisa.hemagglutinin.mutations.service;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReactiveSequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequencesProcessingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SequenceService {

    @Autowired
    ReactiveSequenceRepository reactiveSequenceRepository;

    @Autowired
    SequenceRepository sequenceRepository;

    @Autowired
    SequencesProcessingRepository sequencesProcessingRepository;

    @Autowired
//    public SequenceService(SequenceRepository sequenceRepository) {
//        this.sequenceRepository = sequenceRepository;
//    }

    public List<Sequence> findAllSequences() {
        return sequenceRepository.findAll();
    }

    public long getSequenceCount() {
        return sequenceRepository.count();
    }

    @Transactional
    public Sequence saveSequence(Sequence sequence) {
        return sequenceRepository.save(sequence);
    }

    public List<SequencesProcessingStatus> addDownloadedSequences(List<Sequence> downloadedSequences) {
        return downloadedSequences.stream()
                .map(sequence -> SequencesProcessingStatus.builder()
                        .sequence(sequence)
                        .status(SequencesProcessingStatus.STATUS.DOWNLOADED)
                        .build())
                .map(sequenceProcessingStatus -> sequencesProcessingRepository.save(sequenceProcessingStatus))
                .collect(Collectors.toList());
    }

    public List<SequencesProcessingStatus> findAllSequencesProcessingStatuses() {
        return sequencesProcessingRepository.findAll();
    }


//    void x(){
//        sequenceRepository.
//    }

}
