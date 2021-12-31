package org.ryboun.sisa.hemagglutinin.mutations.repository;

import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlignedSequenceRepository extends MongoRepository<AlignedSequence, String> {
}
