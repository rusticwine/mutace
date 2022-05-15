package org.ryboun.sisa.hemagglutinin.mutations;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceTestable;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;


public class Utils {

    public static List<Sequence> mapperNotYetWorkingForMe(SequenceTestable sequenceTest) {
        return sequenceTest.getSequenceList()
                           .stream()
                           .map(st -> Sequence.builder()
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
            sb.append(clientRequest.url());

            System.out.println(sb.toString());
            return Mono.just(clientRequest);
        });
    }


    public static int[] getAlignedPositions(String rawReferenceSequence, String alignedReferenceSequence) {
        int nextPosition = 0;
        int index = 0;
        int[] positions = new int[rawReferenceSequence.length()];

        for (char ch : rawReferenceSequence.toCharArray()) {
            nextPosition = getNextLetterPosition(ch, alignedReferenceSequence, nextPosition);
            positions[index++] = nextPosition;
        }

        return positions;
    }

    public static int getNextLetterPosition(char letterToMatch, String alignedReferenceSequence, int startPosition) {
        return alignedReferenceSequence.indexOf(letterToMatch, startPosition);
    }

    public static List<String> accverFromSequences(Collection<Sequence> sequences) {
         return sequences.stream().map(Sequence::getAccver).collect(Collectors.toList());
    }

    public static String accverFromSequencesToString(Collection<Sequence> sequences) {
        return String.join(", ", accverFromSequences(sequences));
    }

    public static <T> Stream<T> createLoggingStream(Collection<T> stream, Function<T, String> informationGatherer) {
        System.out.println("creating test stream logging");
        return stream.stream().peek(( item) -> System.out.println(informationGatherer.apply(item)));
    }
}
