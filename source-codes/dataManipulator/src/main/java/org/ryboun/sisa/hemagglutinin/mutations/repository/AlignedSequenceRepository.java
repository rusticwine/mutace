package org.ryboun.sisa.hemagglutinin.mutations.repository;

import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlignedSequenceRepository extends MongoRepository<AlignedSequences, String> {


    List<AlignedSequences> downloadDateAfter(LocalDateTime startDate);

    List<AlignedSequences> downloadDateBefore(LocalDateTime endDate);

    List<AlignedSequences> downloadDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
