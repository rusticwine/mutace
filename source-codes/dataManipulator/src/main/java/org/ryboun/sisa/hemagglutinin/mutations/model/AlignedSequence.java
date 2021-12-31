package org.ryboun.sisa.hemagglutinin.mutations.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;


@Document
@Data
@SuperBuilder
//@Value
//@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlignedSequence {

    private String jobId;
    private LocalDateTime collectionDate;
    private LocalDateTime downloadDate;
    private LocalDateTime alignDate;
    ALIGNMENT_METHOD alignmentMethod;// = ALIGNMENT_METHOD.MAFFT;
    ALIGNMENT_PROVIDER alignmentProvider;// = ALIGNMENT_PROVIDER.EBI;
    private String taxonomyId;

    private String alignedSequence;
    private String referenceSequenceId;

    private Reference reference;
    private Alignment alignment;

    public enum ALIGNMENT_METHOD {
        MAFFT;
    }
    public enum ALIGNMENT_PROVIDER {
        EBI;
    }

    @Document
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Reference {

        public String orgname;
        public String accver;
        public String sequence;
    }

    @Document
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @ToString
    public static class Alignment {

        private String accver;
        private String orgname;
//        private List<Integer> positions;
        int[] positions;
        private String referenceSequence;
        private String alignedSequence;
    }
}
