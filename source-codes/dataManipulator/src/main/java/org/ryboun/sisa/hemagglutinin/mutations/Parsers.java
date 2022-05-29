package org.ryboun.sisa.hemagglutinin.mutations;

import org.apache.commons.lang3.StringUtils;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class Parsers {

//    public List<AlignedSequence> loadAlignedNormalizedSequences() throws IOException {
//        String NEW_SEQUENCE_MARKER = ">New|";
//
//        InputStream is = this.getClass().getClassLoader().getResourceAsStream("vysledny_alignment_mafft_v1.fasta");
//        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
//
//        String line;
//        while ((line = br.readLine()) != null) {
//            sb.append(line.replace("\r", "").replace("\n", ""));
//        }
//        String normalizedAlignedSequencesStr = sb.toString();
//        String[] normalizedAlignedSequences = StringUtils.splitByWholeSeparator(normalizedAlignedSequencesStr,
//                NEW_SEQUENCE_MARKER);
//
//        String[] referenceSequenceParameters = StringUtils.split(normalizedAlignedSequences[0], "[]");
//
//        String alignedReferenceSequence = referenceSequenceParameters[2].trim();
//        String rawReferenceSequence = StringUtils.remove(alignedReferenceSequence,"-");
//        ReferenceSequence referenceSequence = ReferenceSequence.builder()
//                .accver(referenceSequenceParameters[0].trim())
//                .organism(referenceSequenceParameters[1].trim())
//                .sequence(rawReferenceSequence)
//                .build();
//
//        referenceSequenceRepository.save(referenceSequence);
//
//        int[] positions = Utils.getAlignedPositions(rawReferenceSequence, alignedReferenceSequence);
//
//        LocalDateTime alignDateTimeNow = LocalDateTime.now();
//        LocalDateTime downloadDateTime = alignDateTimeNow.minusDays(1);
//
//        AtomicInteger hoursDeductor = new AtomicInteger(0);
//
//        final AtomicInteger counter = new AtomicInteger(0); //so ugly
//        List<Sequence> sequences = IntStream.range(1, normalizedAlignedSequences.length)
//                .mapToObj(i -> normalizedAlignedSequences[i])
//                .map(sequence -> sequence.split("[\\[\\]]"))
//                .map(this::<Sequence>createSequenceFromStrings)
//                .map(sequenceService::saveSequence) //use saved sequence for AlignedSequence creation
//                .collect(Collectors.toList());
//
//        List<AlignedSequence> alignedSequences = IntStream.range(0, (sequences.size() + BATCH_SIZE - 1) / BATCH_SIZE)
//                .mapToObj(i -> sequences.subList(i * BATCH_SIZE, Math.min(sequences.size(), (i + 1) * BATCH_SIZE)))
//                .map(sequenceBatch -> {
//                    List<AlignedSequence.Alignment> alignments = sequenceBatch.stream()
//                            .map(sequence -> AlignedSequence.Alignment.builder()
//                                    .accver(sequence.getAccver())
//                                    .alignedSequence(
//                                            sequence.getSequence())
//                                    .build())
//                            .collect(Collectors.toList());
//
//                    return AlignedSequence.builder()
//                            .reference(AlignedSequence.ReferenceSequence.builder()
//                                    .accver(referenceSequence.getAccver())
//                                    .positions(positions)
//                                    .rawReferenceSequence(rawReferenceSequence)
//                                    .alignedReferenceSequence(
//                                            alignedReferenceSequence)
//                                    .build())
//                            .alignment(alignments)
//                            .downloadDate(downloadDateTime.minusHours(
//                                    hoursDeductor.incrementAndGet() * 3))
//                            .alignDate(alignDateTimeNow.minusHours(hoursDeductor.incrementAndGet())) //not proud of this
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//        sequenceService.saveAlignedSequences(alignedSequences);
//
//        return alignedSequences;
//    }
}
