package org.ryboun.sisa.hemagglutinin.mutations.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data //TODO - solve setStatus and make it immutable
@Builder
@ToString
public class SequencesProcessingStatus {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String alignJobId;

    private Sequence.STATUS status;

    //pretty much redundant, just for easy observability
    private int rawSequenceCount;

    private ReferenceSequence referenceSequence;

    private final LocalDateTime alidnmentSubmitted;

    //or separate collection?
    private LocalDateTime alidnmentFinishedFoundout;

    private List<Sequence> rawSequences;
}
