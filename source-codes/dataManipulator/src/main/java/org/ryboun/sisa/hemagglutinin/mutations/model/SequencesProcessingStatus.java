package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@Document
@Data //TODO - solve setStatus and make it immutable
@SuperBuilder
@NoArgsConstructor
@ToString
@CompoundIndexes({ //not yet needed - preparation for path of deciding what sequences to align not based on the status in main sequence collection, but rather on "last not aligned" or somehow else
        @CompoundIndex(name = "status_recordCreated_idx", def = "{'status' : 1, 'recordCreated' : 1}"),
        @CompoundIndex(name = "recordCreated_idx", def = "{'recordCreated' : 1}")
})
public class SequencesProcessingStatus extends BaseEntity {

    private String alignJobId;

    private Sequence.STATUS status;

    //pretty much redundant, just for easy observability
    private int rawSequenceCount;

    private ReferenceSequence referenceSequence;

    private LocalDateTime alidnmentSubmitted;

    //for searching purposes this contains interval when the containing sequences were downloaded
    private LocalDateTime oldestSequenceDownloadedOn;
    //for searching purposes this contains interval when the containing sequences were downloaded
    private LocalDateTime youngestSequenceDownloadedOn;


    //or separate collection?
    private LocalDateTime alidnmentFinishedFoundout;

    private List<BareSequenceWithAccver> rawSequences;

//    public static abstract class SequencesProcessingStatusBuilder<C extends SequencesProcessingStatus, B extends SequencesProcessingStatusBuilder<C, B>>
//            extends BaseEntityBuilder<C, B> {
//
//        private List<BareSequenceWithAccver> rawSequences;
//
//        //for searching purposes this contains interval when the containing sequences were downloaded
//        private LocalDateTime oldestSequenceDownloadedOn;
//        //for searching purposes this contains interval when the containing sequences were downloaded
//        private LocalDateTime youngesSequenceDownloadedOn;
//
//        public B rawSequences(List<BareSequenceWithAccver>  referenceSequence) {
//            this.rawSequences = rawSequences;
//
//            rawSequences.sort(Comparator.comparing(c -> c.getAccver()));
//
//            return self();
//        }
//    }
}
