package org.ryboun.sisa.hemagglutinin.mutations.repository;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReactiveSequenceRepository extends ReactiveMongoRepository<Sequence, String> {

}
