package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.module.alignment.AlignDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Profile({"default","dev"})
public class EbiAligner implements AlignerService {


    private static final String RESULT_TYPE = "out";

    @Value("${ebi.aligner.baseUrl}")
    private String BASE_URL;

    @Value("${ebi.aligner.postJobSegment}")
    private String POST_JOB_PATH_SEGMENT;

    @Value("${ebi.aligner.checkJobStatusSegment}")
    private String CHECK_JOB_STATUS_PATH_SEGMENT;

    @Value("${ebi.aligner.retrievJobResultSegment}")
    private String RETRIEVE_JOB_RESULT_PATH_SEGMENT;

    private WebClient webClient;

//    @PostConstruct //as in RawSequenceDownloader
//    private void init() {
//        this.webClient = WebClient.builder()
//                .baseUrl(BASE_URL)
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
//                .filters(exchangeFilterFunctions -> {
//                    exchangeFilterFunctions.add(Utils.logRequest());
//                })
//                .build();
//    }

    public String testAlign1_submitJob(AlignDto body) {
        WebClient webClient = WebClient.builder()
                .baseUrl(BASE_URL + POST_JOB_PATH_SEGMENT)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        ResponseEntity<String> result = webClient.post()
                .body(BodyInserters.fromFormData("email", body.getEmail())
                        .with("format", "fasta")
                        .with("sequence", AlignDto.sequencesToString(body.getSequences())))
                .retrieve()
                .toEntity(String.class)
                .block();


        System.out.println("test align 1, status code: " + result.getStatusCode());
        return result.getBody();
    }

    /**
     *
     * @param jobId Ebi job running remote alignment
     * @return Aligned sequences
     */
    public String testAlign1_getResult(String jobId) {
        WebClient webClient = WebClient.builder()
                //FIXME, obviously, of course... Lot of fixme in here
                .baseUrl(BASE_URL + RETRIEVE_JOB_RESULT_PATH_SEGMENT + jobId + "/" + RESULT_TYPE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        ResponseEntity<String> result = webClient.get()
                .retrieve()
                .toEntity(String.class)
                .block();

        return result.getBody();
    }

    /**
     *
     * @param jobId Ebi job running remote alignment
     * @return job status at Ebi site
     */
    public String testAlign1_checkJobStatus(String jobId) {
        WebClient webClient = WebClient.builder()
                .baseUrl(BASE_URL + CHECK_JOB_STATUS_PATH_SEGMENT + jobId)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .build();

        ResponseEntity<String> result = webClient.get()
                .retrieve()
                .toEntity(String.class)
                .block();

        return result.getBody();
    }
}
