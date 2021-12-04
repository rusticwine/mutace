package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.util.function.Tuple2;

@Document
@Data
@Builder
@ToString
public class StatusChange {


    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private Sequence sequence;

    SequencesProcessingStatus.STATUS statusFrom;

    SequencesProcessingStatus.STATUS statusTo;
}
