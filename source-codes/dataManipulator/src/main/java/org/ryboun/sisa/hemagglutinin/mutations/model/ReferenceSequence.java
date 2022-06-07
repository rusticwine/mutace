package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
@SuperBuilder
@NoArgsConstructor
@ToString
public class ReferenceSequence extends Sequence {
    //to have separate collection for reference. Is that wise?

    public ReferenceSequence(Sequence sequence) {
        super(sequence);
    }
}
