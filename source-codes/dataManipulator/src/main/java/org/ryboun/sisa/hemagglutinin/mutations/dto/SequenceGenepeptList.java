package org.ryboun.sisa.hemagglutinin.mutations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

@XmlRootElement(name = "GBSet")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SequenceGenepeptList implements SequenceTestable {

    @XmlElement(name = "GBSeq")
    @Getter
    private List<SequenceT2> sequenceList;

    @XmlRootElement(name = "GBSeq")
    @Getter
    public static class SequenceT2 implements SequenceTestableInner {

        @XmlElement(name = "GBSeq_sequence")
        private String sequence;

        @XmlElement(name = "GBSeq_organism")
        private String organism;

        //TODO - cannot work
        @XmlElement(name = "GBSeq_feature-table/GBFeature/GBFeature_quals/GBQualifier/GBQualifier_value")
        private String taxid;

        @XmlElement(name = "GBSeq_accession-version")
        private String accver;

        @XmlJavaTypeAdapter(type=LocalDate.class, value = SequenceGenepeptList.LocalDateAdapter.class)
        @XmlElement(name = "GBSeq_create-date")
        private LocalDate dateCreated;

        @XmlJavaTypeAdapter(type=LocalDate.class, value = SequenceGenepeptList.LocalDateAdapter.class)
        @XmlElement(name = "GBSeq_update-date")
        private LocalDate dateUpdated;
    }

    //may want to move away. Though so far prefer this encapsulation for now.
    public static class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
//                <GBSeq_update-date>24-JUL-2014</GBSeq_update-date>
        private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy").toFormatter();
//        private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        @Override
        public LocalDate unmarshal(String localDateStr) throws Exception {
            return LocalDate.parse(localDateStr, dateFormat);
        }

        @Override
        public String marshal(LocalDate localDate) throws Exception {
            return localDate.format(dateFormat);
        }
    }
}
