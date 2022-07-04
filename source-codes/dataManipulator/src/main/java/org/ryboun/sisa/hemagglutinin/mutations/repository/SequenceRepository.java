package org.ryboun.sisa.hemagglutinin.mutations.repository;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SequenceRepository extends MongoRepository<Sequence, String> {

    Sequence findByAccver(String accver);
    List<Sequence> findByStatus(Sequence.STATUS status);

    List<Sequence> findByStatus(Sequence.STATUS status, Pageable pageable);

}
