package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document
@Data
@Builder
@ToString
public class SequencesProcessingStatus {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String alignJobId;

    private Sequence.STATUS status;

    Sequence referenceSequence;

    private List<Sequence> rawSequences;
}
