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
    public enum STATUS {
        DOWNLOADED, ALIGNING, ALIGNED
    }

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private STATUS status;

    private String jobId;

    private List<Sequence> sequences;
}
