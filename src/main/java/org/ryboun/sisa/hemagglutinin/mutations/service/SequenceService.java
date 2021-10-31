package org.ryboun.sisa.hemagglutinin.mutations.service;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReactiveSequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class SequenceService {

    @Autowired
    ReactiveSequenceRepository reactiveSequenceRepository;

    @Autowired
    SequenceRepository sequenceRepository;

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

//    @Transactional
    public Sequence saveSequence(Sequence sequence) {
        return sequenceRepository.save(sequence);
    }

//    void x(){
//        sequenceRepository.
//    }

}
