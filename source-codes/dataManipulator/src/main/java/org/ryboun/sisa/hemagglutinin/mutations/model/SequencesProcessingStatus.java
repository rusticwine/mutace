package org.ryboun.sisa.hemagglutinin.mutations.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data //TODO - solve setStatus and make it immutable
@SuperBuilder
@NoArgsConstructor
@ToString
@CompoundIndexes({ //not yet needed - preparation for path of deciding what sequences to align not based on the status in main sequence collection, but rather on "last not aligned" or somehow else
        @CompoundIndex(name = "status_recordCreated_idx", def = "{'status' : 1, 'recordCreated' : 1}"),
        @CompoundIndex(name = "recordCreated_idx", def = "{'recordCreated' : 1}")
})
public class SequencesProcessingStatus extends BaseEntity {

    private String alignJobId;

    private Sequence.STATUS status;

    //pretty much redundant, just for easy observability
    private int rawSequenceCount;

    private ReferenceSequence referenceSequence;

    private LocalDateTime alidnmentSubmitted;

    //or separate collection?
    private LocalDateTime alidnmentFinishedFoundout;

    private List<BareSequenceWithAccver> rawSequences;
}
