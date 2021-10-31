package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
@Builder
@ToString
public class SequencesProcessingStatus {
    public enum STATUS {
        DOWNLOADED, ALIGNING, ALIGNED
    }

    @Id
    private String id;

    private STATUS status;

    private Sequence sequence;
}
