package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.module.alignment.AlignDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EbiAligner {


    private static final String RESULT_TYPE = "out";

    private final static String POST_JOB_URL = "https://www.ebi.ac.uk/Tools/services/rest/mafft/run";
    private final static String CHECK_JOB_STATUS_URL = "https://www.ebi.ac.uk/Tools/services/rest/mafft/status/";
    private final static String RETRIEVE_JOB_RESULT_URL = "https://www.ebi.ac.uk/Tools/services/rest/mafft/result";

    public String testAlign1_submitJob(AlignDto body) {
        WebClient webClient = WebClient.builder()
                .baseUrl(POST_JOB_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        System.out.println(body.toString());
        ResponseEntity<String> result = webClient.post()
                .body(BodyInserters.fromFormData("email", body.getEmail())
                        .with("format", "fasta")
                        .with("sequence", AlignDto.sequencesToString(body.getSequences())))
                .retrieve()
                .toEntity(String.class)
                .block();


        System.out.println("test align 1, status code: " + result.getStatusCode());
        return result.getBody().toString();
    }

    /**
     *
     * @param jobId Ebi job running remote alignment
     * @return Aligned sequences
     */
    public String testAlign1_getResult(String jobId) {
        WebClient webClient = WebClient.builder()
                //FIXME, obviously, of course... Lot of fixme in here
                .baseUrl(RETRIEVE_JOB_RESULT_URL + "/" + jobId + "/" + RESULT_TYPE)
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
                .baseUrl(CHECK_JOB_STATUS_URL + jobId)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .build();

        ResponseEntity<String> result = webClient.get()
                .retrieve()
                .toEntity(String.class)
                .block();

        return result.getBody();
    }
}
