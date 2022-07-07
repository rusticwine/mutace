package org.ryboun.sisa.hemagglutinin.mutations.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

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

    private LocalDateTime recordCreatedOn;

    public static abstract class BaseEntityBuilder<C extends BaseEntity, B extends BaseEntityBuilder<C, B>> {
        private LocalDateTime recordCreated = LocalDateTime.now();
    }

}
