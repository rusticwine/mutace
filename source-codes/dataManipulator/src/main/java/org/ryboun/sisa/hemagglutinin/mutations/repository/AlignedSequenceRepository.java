package org.ryboun.sisa.hemagglutinin.mutations.repository;

import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AlignedSequenceRepository extends MongoRepository<AlignedSequence, String> {

    List<AlignedSequence> downloadDateAfter(LocalDateTime startDate);
    List<AlignedSequence> downloadDateBefore(LocalDateTime endDate);
    List<AlignedSequence> downloadDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
