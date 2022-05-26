package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceGenepeptList;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceTestable;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReferenceSequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Profile("dev-mock")
public class RawSequenceDownloaderServiceMock implements RawSequenceDownloaderService {

    @Value("${profile.my}")
    String profile;
    @Autowired
    Environment env;

    int BATCH_SIZE = 100;

//    @Autowired
//    private SequenceService sequenceService;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    SequenceRepository sequenceRepository;

    private static final AtomicInteger invocationCounter = new AtomicInteger(0);

    @Override
    public Mono<SequenceTestable> downloadSequencesFromTo(LocalDate from, LocalDate to) {

        invocationCounter.incrementAndGet();
        //SequenceT2 implements SequenceTestableInner
        List<? extends SequenceTestable.SequenceTestableInner> filteredSequences = genePeptSequences.getSequenceList().stream()
                .filter(sequence -> from.isBefore(sequence.getDateCreated()) && to.isAfter(sequence.getDateCreated()))
                .collect(Collectors.toList());

        return Mono.just(new SequenceGenepeptList((List<SequenceGenepeptList.SequenceT2>) filteredSequences));
    }

    SequenceTestable genePeptSequences;

    ///////////-------------------------------------/////////////
    ///////////-------------------------------------/////////////
    ///////////-------------------------------------/////////////
    @PostConstruct
    void init() {
        System.out.println("ACTIVE PROFILES: " + Arrays.toString(env.getActiveProfiles()));
        try {
            genePeptSequences = loadDbData();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


    private SequenceGenepeptList loadDbData() throws JAXBException {

        return loadTestSequencesInGenepept();
    }


    private SequenceGenepeptList loadTestSequencesInGenepept() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SequenceGenepeptList.class);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sequences1HemagglutininGenepept.xml");
        SequenceGenepeptList st = (SequenceGenepeptList) context.createUnmarshaller().unmarshal(is);

        return st;
    }


/*
    @PostConstruct
    void init() {
        System.out.println("ACTIVE PROFILES: " + Arrays.toString(env.getActiveProfiles()));
        try {
            SequenceTestable st = loadDbData();
            List<Sequence> sequences = Utils.mapperNotYetWorkingForMe(st);
            List<Sequence> savedSequences = sequences.stream()
//                    .map(s -> sequenceService.saveSequence(s))
                    .map(s -> sequenceRepository.save(s))
                    .collect(Collectors.toList());

            //            SequencesProcessingStatus downloadedSequences = addDownloadedSequences(savedSequences);

            List<AlignedSequence> testAlignedSequences = loadAlignedNormalizedSequences();
            System.out.println("number of test aligned sequences: " + testAlignedSequences.size());
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
    }


    private SequenceGenepeptList loadDbData() throws JAXBException {

        return loadTestSequencesInGenepept();
    }


    private SequenceGenepeptList loadTestSequencesInGenepept() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SequenceGenepeptList.class);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sequences1HemagglutininGenepept.xml");
        SequenceGenepeptList st = (SequenceGenepeptList) context.createUnmarshaller().unmarshal(is);

        return st;
    }


    public List<AlignedSequence> loadAlignedNormalizedSequences() throws IOException {
        String NEW_SEQUENCE_MARKER = ">New|";

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("vysledny_alignment_mafft_v1.fasta");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line.replace("\r", "").replace("\n", ""));
        }
        String normalizedAlignedSequencesStr = sb.toString();
        String[] normalizedAlignedSequences = StringUtils.splitByWholeSeparator(normalizedAlignedSequencesStr,
                NEW_SEQUENCE_MARKER);

        String[] referenceSequenceParameters = StringUtils.split(normalizedAlignedSequences[0], "[]");

        String alignedReferenceSequence = referenceSequenceParameters[2].trim();
        String rawReferenceSequence = StringUtils.remove(alignedReferenceSequence,"-");
        ReferenceSequence referenceSequence = ReferenceSequence.builder()
                .accver(referenceSequenceParameters[0].trim())
                .organism(referenceSequenceParameters[1].trim())
                .sequence(rawReferenceSequence)
                .build();

        referenceSequenceRepository.save(referenceSequence);

        int[] positions = Utils.getAlignedPositions(rawReferenceSequence, alignedReferenceSequence);

        LocalDateTime alignDateTimeNow = LocalDateTime.now();
        LocalDateTime downloadDateTime = alignDateTimeNow.minusDays(1);

        AtomicInteger hoursDeductor = new AtomicInteger(0);

        final AtomicInteger counter = new AtomicInteger(0); //so ugly
        List<Sequence> sequences = IntStream.range(1, normalizedAlignedSequences.length)
                .mapToObj(i -> normalizedAlignedSequences[i])
                .map(sequence -> sequence.split("[\\[\\]]"))
                .map(this::<Sequence>createSequenceFromStrings)
                .map(sequenceService::saveSequence) //use saved sequence for AlignedSequence creation
                .collect(Collectors.toList());

        List<AlignedSequence> alignedSequences = IntStream.range(0, (sequences.size() + BATCH_SIZE - 1) / BATCH_SIZE)
                .mapToObj(i -> sequences.subList(i * BATCH_SIZE, Math.min(sequences.size(), (i + 1) * BATCH_SIZE)))
                .map(sequenceBatch -> {
                    List<AlignedSequence.Alignment> alignments = sequenceBatch.stream()
                            .map(sequence -> AlignedSequence.Alignment.builder()
                                    .accver(sequence.getAccver())
                                    .alignedSequence(
                                            sequence.getSequence())
                                    .build())
                            .collect(Collectors.toList());

                    return AlignedSequence.builder()
                            .reference(AlignedSequence.ReferenceSequence.builder()
                                    .accver(referenceSequence.getAccver())
                                    .positions(positions)
                                    .rawReferenceSequence(rawReferenceSequence)
                                    .alignedReferenceSequence(
                                            alignedReferenceSequence)
                                    .build())
                            .alignment(alignments)
                            .downloadDate(downloadDateTime.minusHours(
                                    hoursDeductor.incrementAndGet() * 3))
                            .alignDate(alignDateTimeNow.minusHours(hoursDeductor.incrementAndGet())) //not proud of this
                            .build();
                })
                .collect(Collectors.toList());

        sequenceService.saveAlignedSequences(alignedSequences);

        return alignedSequences;
    }


    private Sequence createSequenceFromStrings(String[] sequence) {
        return Sequence.builder() //store the sequence, though alrady aligned, just to have the reference in main collections
                .accver(StringUtils.remove(sequence[0], "hemagglutinin").trim())
                .organism(sequence[1].trim())
                .sequence(sequence[2].trim())
                .build();
    }
    /**/
}
