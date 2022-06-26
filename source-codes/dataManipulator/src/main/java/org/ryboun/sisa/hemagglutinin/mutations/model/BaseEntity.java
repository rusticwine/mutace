package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

//@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public  class BaseEntity {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private LocalDateTime recordCreated;

    public static abstract class BaseEntityBuilder<C extends BaseEntity, B extends BaseEntityBuilder<C, B>> {
        private LocalDateTime recordCreated = LocalDateTime.now();
    }

}
