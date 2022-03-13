package org.ryboun.sisa.hemagglutinin.mutations.repository;

import java.util.List;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReferenceSequenceRepository extends MongoRepository<ReferenceSequence, String> {

    List<ReferenceSequence> findByAccver(String accver);
    List<ReferenceSequence> findByStatus(Sequence.STATUS status);

}
