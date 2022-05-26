package org.ryboun.sisa.hemagglutinin.mutations.repository;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SequencesProcessingRepository extends MongoRepository<SequencesProcessingStatus, String> {

    List<SequencesProcessingStatus> findByStatus(Sequence.STATUS status);

}
