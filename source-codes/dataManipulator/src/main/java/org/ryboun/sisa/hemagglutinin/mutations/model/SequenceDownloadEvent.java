package org.ryboun.sisa.hemagglutinin.mutations.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
@SuperBuilder
@NoArgsConstructor
@ToString
public class SequenceDownloadEvent extends BaseEntity {

//    @Id
//    @Setter(AccessLevel.NONE)
//    private String id;

    LocalDate downloadFrom;

    //this will be probably starting point for next download period
    @Indexed(unique=true)
    LocalDate downloadTill;
    LocalDateTime downloadedOn;
    int downloadedSequenceCount;
//    Set<String> downloadedSequences
}
