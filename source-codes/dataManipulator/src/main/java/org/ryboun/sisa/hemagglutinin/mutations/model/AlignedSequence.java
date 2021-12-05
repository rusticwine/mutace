package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
@Builder
@ToString
public class AlignedSequence extends Sequence {

//    @Id
//    @Setter(AccessLevel.NONE)
//    private String id;
//
    private String alignedSequence;
    private String referenceSequenceId;

//    List<Tuple2<Long, SequencesProcessingStatus.STATUS>> statuses;

}
