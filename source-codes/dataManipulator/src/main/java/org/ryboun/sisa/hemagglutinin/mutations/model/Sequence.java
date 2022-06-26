package org.ryboun.sisa.hemagglutinin.mutations.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.util.function.Tuple2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Document
@Data
//@Builder
@SuperBuilder
@NoArgsConstructor
//@AllArgsConstructor
public class Sequence extends BaseEntity {

    @JsonProperty("X")
    private String organism;
    private String protein;
    private String sequence;
    @Indexed(unique=true)
    private String accver;
    private String taxid;

    @Indexed
    private LocalDate sequenceCreatedOn;

    private LocalDate sequenceUpdatedOn;

    private LocalDateTime recordCreatedOn;

    //value is position of index value. Value of index 0 says where position 0 of the sequence reside, etc.
    int[] positions;

    List<Tuple2<Long, STATUS>> statuses;

    //Awkward. Redo this concept of reference-sequence-inheritance
    public Sequence(Sequence sequence) {
        this.positions = sequence.getPositions();
        this.organism = sequence.getOrganism();
        this.accver = sequence.getAccver();
        this.sequence = sequence.getSequence();
        this.protein = sequence.getProtein();
        this.taxid = sequence.getTaxid();
        //this. = sequence.get;
    }


    public enum STATUS {
        DOWNLOADED, ALIGNING, TO_BE_ALIGNED, ALIGNED
    }

    private STATUS status;


}
