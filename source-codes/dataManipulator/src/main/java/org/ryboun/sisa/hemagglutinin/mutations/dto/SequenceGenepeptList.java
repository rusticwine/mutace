package org.ryboun.sisa.hemagglutinin.mutations.dto;

import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "GBSet")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class SequenceGenepeptList implements SequenceTestable {

    @XmlElement(name = "GBSeq")
    @Getter
    private List<SequenceTestable> sequenceList;

    @XmlRootElement(name = "GBSeq")
    @Getter
    public static class SequenceT2 implements SequenceTestableInner {

        @XmlElement(name = "GBSeq_sequence")
        private String sequence;

        @XmlElement(name = "GBSeq_organism")
        private String organism;

        @XmlElement(name = "GBSeq_feature/GBFeature/GBFeature_quals/GBQualifier/GBQualifier_value")
        private String taxid;

        @XmlElement(name = "GBSeq_accession-version")
        private String accver;
    }
}
