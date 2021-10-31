package org.ryboun.sisa.hemagglutinin.mutations.repository;

import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequencesProcessingRepository extends MongoRepository<SequencesProcessingStatus, String> {

}
