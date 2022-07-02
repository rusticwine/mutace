package org.ryboun.sisa.module.alignment;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.ryboun.sisa.hemagglutinin.mutations.model.BareSequenceWithAccver;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Value
@Document
@Builder
public class AlignDto {

    public static final String SEQUENCE_SPLITTER = ">";

    String email;
    String format;
    @Singular("addSequence")
    List<BareSequenceWithAccver> sequences;

    //to some utils class?
    public static String sequencesToString(List<BareSequenceWithAccver> sequences) {
        return sequences.stream()
                .map(BareSequenceWithAccver::getBareSequence)
                .collect(Collectors.joining(SEQUENCE_SPLITTER + " "));
    }
}
