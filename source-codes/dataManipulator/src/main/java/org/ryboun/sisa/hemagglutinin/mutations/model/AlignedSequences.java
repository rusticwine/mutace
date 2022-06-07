package org.ryboun.sisa.hemagglutinin.mutations.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document
@Data
@SuperBuilder
//@Value
//@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlignedSequences {

    private String jobId;
    private LocalDateTime collectionDate;
    private LocalDateTime downloadDate;
    private LocalDateTime alignDate;
    ALIGNMENT_METHOD alignmentMethod = ALIGNMENT_METHOD.MAFFT;// = ALIGNMENT_METHOD.MAFFT;
    ALIGNMENT_PROVIDER alignmentProvider = ALIGNMENT_PROVIDER.EBI;// = ALIGNMENT_PROVIDER.EBI;
    private String taxonomyId;

    private ReferenceInAlignemnt reference;
    private List<Alignment> alignedSequences;

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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReferenceInAlignemnt {

        public String accver;
        public String rawReferenceSequence;
        public String alignedReferenceSequence;
    }

    @Document
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alignment {

        @Indexed
        private String accver;
//        private String orgname;
//        private List<Integer> positions;
//        int[] positions;
//        private String referenceSequence;
        private String alignedSequence;
    }
}
