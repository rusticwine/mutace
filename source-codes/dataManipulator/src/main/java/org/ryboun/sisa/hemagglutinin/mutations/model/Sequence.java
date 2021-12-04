package org.ryboun.sisa.hemagglutinin.mutations.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.util.function.Tuple2;

import java.util.List;


@Document
@Data
@Builder
@ToString
public class Sequence {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @JsonProperty("X")
    private String organism;
    private String protein;
    private String originalSequence;
    private String alignedSequence;
    private String referenceSequenceId;

    List<Tuple2<Long, SequencesProcessingStatus.STATUS>> statuses;

}
