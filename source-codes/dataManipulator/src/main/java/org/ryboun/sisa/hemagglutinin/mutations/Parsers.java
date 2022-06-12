package org.ryboun.sisa.hemagglutinin.mutations;

import org.apache.commons.lang3.StringUtils;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequences;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Parsers {

    //TODO - move outside util class
    public static final String REFERENCE_SEQUENCE_RESOURCE = "referenceSequences.fasta";
    public static final String FASTA_SEQUENCE_SEPARATOR = ">";
    public static final String SPACE_CHARACTERS = "\u0020\t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000";

    public static List<ReferenceSequence> loadReferenceSequenceFromResource() throws IOException {

        String sequencesStr = Utils.loadResourceToString(REFERENCE_SEQUENCE_RESOURCE);
        return parseSequences(sequencesStr, ReferenceSequence::builder);
    }


    public static List<Sequence> parseFastaSequences(String sequencesStr) throws IOException {

        return parseSequences(sequencesStr, Sequence::builder);
    }

//    public static List<AlignedSequences> parseAlignment(String sequencesStr) throws IOException {
//        poslat custom builder - consumer ?
////        return parseSequences(sequencesStr, Sequence::builder);
//    }

    private static <T extends Sequence.SequenceBuilder, T2 extends Sequence> List<T2> parseSequences(String sequences, Supplier<T> sequenceBuilderFactory) {

        String[] alignedSequencesStr = normalizeFastaAndSplitBySequence(sequences);

        return Arrays.stream(alignedSequencesStr).map(sequencesStr -> StringUtils.split(sequencesStr, "[]")).map(sequenceElementsList -> {
            String[] accverWithProteing = StringUtils.split(sequenceElementsList[0], SPACE_CHARACTERS);
//                    this is doable just becaus objects are the same thouh
            return (T2) sequenceBuilderFactory.get()
                    .accver(accverWithProteing[0].trim())
                    .protein(accverWithProteing[1].trim())
                    .organism(sequenceElementsList[1].trim())
                    .sequence(sequenceElementsList[2].trim())
                    .build();
        }).collect(Collectors.toList());
    }

    public static AlignedSequences parseAlignedSequences(String alignedSequencesStr, SequencesProcessingStatus processingStatus) {

        String accverReferenceSequences = processingStatus.getReferenceSequence().getAccver();

        List<AlignedSequences.Alignment>  alignedSequences = parseAlignedSequences(alignedSequencesStr,
                (splitSequence) -> AlignedSequences.Alignment.builder()
                        .accver(splitSequence[0])
                        .alignedSequence(splitSequence[1])
                        .build());

        String alignedReferenceSequence = alignedSequences.stream()
                .filter(sequence -> sequence.getAccver().equals(accverReferenceSequences))
                .map(AlignedSequences.Alignment::getAlignedSequence)
                .findFirst()
                .get();

        return AlignedSequences.builder()
                .alignDate(processingStatus.getAlidnmentSubmitted())
                .reference(AlignedSequences.ReferenceInAlignemnt.builder()
                        .accver(accverReferenceSequences)
                        .rawReferenceSequence(processingStatus.getReferenceSequence().getSequence())
                        .alignedReferenceSequence(alignedReferenceSequence)
                        .build())
                .jobId(processingStatus.getAlignJobId())
                .alignedSequences(alignedSequences)
                .build();

    }
    /**
     * To be refactored with other parsing method. Need sequences to have common hierarchy
     *
     * @param sequences
     * @param createSequence
     * @param <T>
     * @param <T2>
     * @return
     */
    private static List<AlignedSequences.Alignment> parseAlignedSequences(String sequences, Function<String[], AlignedSequences.Alignment> buildSequence) {

        String[] alignedSequencesStr = normalizeFastaAndSplitBySequence(sequences);

        return Arrays.stream(alignedSequencesStr)
                .map(sequenceElementsList -> {
                    //this refactored
                    String[] accverWithProteing = StringUtils.split(sequenceElementsList, SPACE_CHARACTERS);
                    return buildSequence.apply(accverWithProteing);

                }).collect(Collectors.toList());
    }


    private static String[] normalizeFastaAndSplitBySequence(String sequences) {
        String normalizedAlignedSequencesStr = sequences.replace("\r", "").replace("\n", "");
        String[] alignedSequencesStr = StringUtils.splitByWholeSeparator(normalizedAlignedSequencesStr, FASTA_SEQUENCE_SEPARATOR);
        return alignedSequencesStr;
    }

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
