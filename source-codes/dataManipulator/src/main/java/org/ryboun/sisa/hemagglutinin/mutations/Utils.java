package org.ryboun.sisa.hemagglutinin.mutations;

import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceServiceForTest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static List<Sequence> mapperNotYetWorkingForMe(SequenceServiceForTest.SequenceTest sequenceTest) {
        return sequenceTest.getSequenceList()
                .stream()
                .map(st -> Sequence
                        .builder()
                        .sequence(st.getSequence())
                        .organism(st.getOrganism())
                        .taxid(st.getTaxid())
                        .accver(st.getAccver())
                        .status(Sequence.STATUS.DOWNLOADED) //TODO - move to post-download logic
                        .build())
                .collect(Collectors.toList());
    }

    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder sb = new StringBuilder("Request: \n");
            //append clientRequest method and url
            sb.append(
                    clientRequest.url());

            System.out.println(sb.toString());
            return Mono.just(clientRequest);
        });
    }
}
