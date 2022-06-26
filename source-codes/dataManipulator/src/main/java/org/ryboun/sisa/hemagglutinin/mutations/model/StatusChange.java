package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.util.function.Tuple2;

@Document
@Data
@SuperBuilder
@ToString
public class StatusChange extends BaseEntity {


//    @Id
//    @Setter(AccessLevel.NONE)
//    private String id;

    private Sequence sequence;

    Sequence.STATUS statusFrom;

    Sequence.STATUS statusTo;
}
