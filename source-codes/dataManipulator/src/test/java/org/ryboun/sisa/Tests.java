package org.ryboun.sisa;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.ryboun.sisa.hemagglutinin.mutations.Application;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequences;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootTest(classes = Application.class)
//@RunWith(SpringRunner.class)
public class Tests {


    private static String readFileFromResources(String fileName) throws IOException {
        return IOUtils.resourceToString(fileName, StandardCharsets.UTF_8);
    }

//    @Test
    public void loadAlignedSequences() throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();

        String json = readFileFromResources("/sequences/aligned/alignmentMultiple2.json");
        Assertions.assertNotNull(json);
        List<AlignedSequences> alignments = objectMapper.readValue(json, List.class);
        Assertions.assertNotNull(alignments);
        Assertions.assertEquals(3, alignments.size(), "size of loaded alignments list does not correspond");
    }

}
