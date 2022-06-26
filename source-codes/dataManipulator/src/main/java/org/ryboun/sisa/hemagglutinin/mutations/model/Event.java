package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Value
@SuperBuilder
public class Event extends BaseEntity {

//
//    @Id
//    @Setter(AccessLevel.NONE)
//    private String id;

    EventType eventType;
    String description;
    LocalDateTime created;
    String comment;

    public enum EventType {
        SEQUENCE_DOWNLOAD, ALIGNMENT_SUBMIT, ALIGNMENT_CHECKED, ALIGNMENT_DOWNLOADED;
    }
}
