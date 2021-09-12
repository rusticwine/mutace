package org.ryboun.sisa.hemagglutinin.mutations.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;


@Mapper
public interface TestSequenceMapper {

    TestSequenceMapper INSTANCE = Mappers.getMapper(TestSequenceMapper.class);

    @Mappings({
            @Mapping(target="", source="sequenceTest."),
            @Mapping(target="", source="sequenceTest."),
            @Mapping(target="", source="sequenceTest."),
            @Mapping(target="", source="sequenceTest.")
    })
    Sequence testSequenceXmlToProductionSequence(SequenceTest sequenceTest);
}
