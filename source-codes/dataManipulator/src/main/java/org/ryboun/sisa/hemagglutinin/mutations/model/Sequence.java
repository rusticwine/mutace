package org.ryboun.sisa.hemagglutinin.mutations.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.util.function.Tuple2;

import java.util.List;


@Document
@Data
//@Builder
@SuperBuilder
//@AllArgsConstructor
@ToString
public class Sequence {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @JsonProperty("X")
    private String organism;
    private String protein;
    private String sequence;
    @Indexed(unique=true)
    private String accver;
    private String taxid;

    //value is position of index value. Value of index 0 says where position 0 of the sequence reside, etc.
    int[] positions;

    List<Tuple2<Long, STATUS>> statuses;


    public enum STATUS {
        DOWNLOADED, ALIGNING, TO_BE_ALIGNED, ALIGNED
    }

    private STATUS status;


}
