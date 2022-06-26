package org.ryboun.sisa.hemagglutinin.mutations.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data //TODO - solve setStatus and make it immutable
@SuperBuilder
@NoArgsConstructor
@ToString
public class SequencesProcessingStatus extends BaseEntity {

//    @Id
//    @Setter(AccessLevel.NONE)
//    private String id;

    private String alignJobId;

    private Sequence.STATUS status;

    //pretty much redundant, just for easy observability
    private int rawSequenceCount;

    private ReferenceSequence referenceSequence;

    private LocalDateTime alidnmentSubmitted;

    //or separate collection?
    private LocalDateTime alidnmentFinishedFoundout;

    private List<Sequence> rawSequences;
}
