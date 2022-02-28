package org.ryboun.sisa.hemagglutinin.mutations.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
@Builder
@ToString
public class SequenceDownloadEvent {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    LocalDate downloadFrom;

    //this will be probably starting point for next download period
    @Indexed(unique=true)
    LocalDate downloadTill;
    LocalDateTime downloadedOn;
    int downloadedSequenceCount;
//    Set<String> downloadedSequences
}
