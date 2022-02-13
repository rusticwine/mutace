package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import lombok.Data;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceServiceForTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class RawSequenceDownloader {

    @Value("${ncbi.downloader.baseUrl}")
    public String NCBI_EUTILS_BASE_URL;
//    public String NCBI_EUTILS_BASE_URL = "https://5fb6fe54-3b8f-4ab4-8963-f69b898d9b64.mock.pstmn.io/";

    @Value("${ncbi.downloader.esearchSegment}")
    public String NCBI_ESEARCH_PATH_SEGMENT;

    @Value("${ncbi.downloader.efetchSegment}")
    public String NCBI_EFETCH_PATH_SEGMENT;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY/MM/dd");

    private WebClient webClient;



    @PostConstruct
    void init() {
        //FIXME - check if such a reuse is allowed (WebClient is IIRC immutable)
//        SslContext context = SslContextBuilder.forClient()
//                .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                .build();
//
//        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(context));
//
//        WebClient wc = WebClient
//                .builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        this.webClient = WebClient.builder()
                .baseUrl(NCBI_EUTILS_BASE_URL) //FIXME - one from properties is not working
//                .baseUrl("https://5fb6fe54-3b8f-4ab4-8963-f69b898d9b64.mock.pstmn.io/")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(Utils.logRequest());
                })
                .build();
    }

    public Mono<EsearchResponse> downloadSequencesFrom2(LocalDate from, LocalDate to) {
        return esearchNcbi(from, to);
    }

    public Mono<SequenceServiceForTest.SequenceTest> downloadSequencesFrom(LocalDate from, LocalDate to) {
        return esearchNcbi(from, to)
                .flatMap(this::efetchNcbi)
                ;

//        EsearchResponse esearchResponse = esearchNcbi(from, to);
//        if (esearchResponse != null && esearchResponse.getQueryKey() != null) {
//            SequenceServiceForTest.SequenceTest st = efetchNcbi(esearchResponse);
//
//            List<Sequence> sequences = Utils.mapperNotYetWorkingForMe(st);
//            List<Sequence> savedSequences = sequences
//                    .stream()
////                    .map(s -> sequenceService.saveSequence(s))
//                    .collect(Collectors.toList());
//
//            System.out.println(Arrays.toString(sequences.toArray()));
//        }
//        return null;
    }

    public Mono<SequenceServiceForTest.SequenceTest> efetchNcbi(EsearchResponse esearchResponse) {
        return webClient.get().uri(uriBuilder -> uriBuilder
                        .path(NCBI_EFETCH_PATH_SEGMENT)
                        .queryParam("db", "nucleotide")
                        .queryParam("WebEnv", esearchResponse.getWebEnv())
                        .queryParam("query_key", esearchResponse.getQueryKey())
                        .queryParam("retmode", "xml")
                        .queryParam("rettype", "fasta")
                        .build())
                .retrieve()
                .bodyToMono(SequenceServiceForTest.SequenceTest.class);
//                .bodyToMono(String.class)
//                .block();

//        System.out.println(sequenceTest);
//        return sequenceTest;
    }

    private Mono<EsearchResponse> esearchNcbi(LocalDate from, LocalDate to) {
        Mono<EsearchResponse> esearchResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(NCBI_ESEARCH_PATH_SEGMENT)
                        .queryParam("db", "nucleotide")
                        .queryParam("term", termBuilder("txid333278", from, to))
                        .queryParam("usehistory", "y")
                        .build())
                .retrieve()
                .bodyToMono(EsearchResponse.class);

        return esearchResponse;
    }

    private String termBuilder(String organism, LocalDate from, LocalDate to) {
        return String.format("%s[Organism] AND hemagglutinin[All Fields] AND (\"%s\"[PDAT] : \"%s\"[PDAT])",
                organism, formatter.format(from), formatter.format(to));
    }


    /////******-----******/////
    @XmlRootElement(name = "eSearchResult")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class EsearchResponse {

        @XmlElement(name = "QueryKey")
        private Integer queryKey;

        @XmlElement(name = "WebEnv")
        private String webEnv;
    }


    /*
https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi
?db=nucleotide
&term=txid333278%5BOrganism%5D%20AND%20hemagglutinin%5BAll%20Fields%5D%20AND%20%28%222013/01/01%22%5BPDAT%5D%20%3A%20%222013/3/31%22%5BPDAT%5D%29&usehistory=y&WebEnv=MCID_61225db4653ab242360909f5

https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi
    ?db=nucleotide
        &usehistory=y
        &WebEnv=MCID_61ec63e48ee850690176c94e
        &query_key=1&retmode=xml&rettype=genepept
    */
}
