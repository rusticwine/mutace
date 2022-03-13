package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Data
@SuperBuilder
@ToString
public class ReferenceSequence extends Sequence {
    //to have separate collection for reference. Is that wise?
}
