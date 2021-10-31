package org.ryboun.sisa.module.alignment;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class MafftAligner implements Aligner {

    private static final String RESULT_TYPE = "out";

    private final static String POST_JOB_URL = "https://www.ebi.ac.uk/Tools/services/rest/mafft/run";
    private final static String CHECK_JOB_STATUS_URL = "https://www.ebi.ac.uk/Tools/services/rest/mafft/status/";
    private final static String RETRIEVE_JOB_RESULT_URL = "https://www.ebi.ac.uk/Tools/services/rest/mafft/result";

    @Override
    public String alignWithSingleReference(AlignDto body) {
        return testAlign1_subitJob(body);
    }

    public String testAlign1_getResult(String jobType, String jobId) {
        WebClient webClient = WebClient.builder()
                //FIXME, obviously, of course... Lot of fixme in here
                .baseUrl(RETRIEVE_JOB_RESULT_URL + "/" + jobType + jobId + "/" + RESULT_TYPE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .build();

        ResponseEntity<String> result = webClient.get()
                .retrieve()
                .toEntity(String.class)
                .block();

        return result.getBody();
    }

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

    private String testAlign2(AlignDto body) {
//        WebClient client = WebClient.create("https://www.ebi.ac.uk/Tools/services/rest/mafft/run");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity responseEntity = restTemplate.postForEntity(POST_JOB_URL, body, AlignDto.class);

        return responseEntity.getStatusCode().toString();
    }

    private String testAlign1_subitJob(AlignDto body) {
        WebClient webClient = WebClient.builder()
                .baseUrl(POST_JOB_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        System.out.println(body.toString());
        ResponseEntity<String> result = webClient.post()
                .body(BodyInserters.fromFormData("email", body.getEmail())
                        .with("format", "fasta")
                        .with("sequence", body.getSequence()))
                .retrieve()
                .toEntity(String.class)
                .block();


        System.out.println("test align 1, status code: " + result.getStatusCode());
        return result.getBody().toString();
    }

    private void testAlign() {
//        WebClient client = WebClient.create("https://www.ebi.ac.uk/Tools/services/rest/mafft/run");
//        WebClient webClient = WebClient.builder()
//                .baseUrl("https://www.ebi.ac.uk/Tools/services/rest/mafft/run")
//                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
//                .build();
//        webClient.post()
//                .body();
    }
}
