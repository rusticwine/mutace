package org.ryboun.sisa.module.alignment;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Document
@Builder
public class AlignDto {

    String email;
    String format;
    String sequence;
}
