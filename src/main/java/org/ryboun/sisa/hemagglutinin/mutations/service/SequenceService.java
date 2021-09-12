package org.ryboun.sisa.hemagglutinin.mutations.service;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class SequenceService {

    final SequenceRepository sequenceRepository;

    @Autowired
    public SequenceService(SequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public Flux<Sequence> findAllSequences() {
        return sequenceRepository.findAll();
    }

    public Mono<Long> getSequenceCount() {
        return sequenceRepository.count();
    }

//    @Transactional
    public Mono<Sequence> saveSequence(Sequence sequence) {
        return sequenceRepository.save(sequence);
    }

//    void x(){
//        sequenceRepository.
//    }

}
