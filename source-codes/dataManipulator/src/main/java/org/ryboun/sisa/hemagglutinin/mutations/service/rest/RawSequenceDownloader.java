package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceServiceForTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


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

        WireMockConfiguration w = WireMockConfiguration.wireMockConfig().dynamicPort();
        System.out.println("PROBABLE PORT: " + w.dynamicPort().portNumber());
        WireMockServer wireMockServer = new WireMockServer(w);

        //        WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        webClient = WebClient.builder().baseUrl(wireMockServer.baseUrl()).build();

        //wireMockServer.stubFor(WireMock.get("entrez/eutils/esearch.fcgi?db=nucleotide&term=txid333278%5BOrganism%5D%20AND%20hemagglutinin%5BAll%20Fields%5D%20AND%20(%222021/01/01%22%5B%60PDAT%5D%20:%20%222021/01/03%22%5BPDAT%5D)&usehistory=y").willReturn(WireMock.ok(esearch)));
        wireMockServer.stubFor(WireMock.get(
                                               "/entrez/eutils/esearch.fcgi?db=nucleotide&term=txid333278%5BOrganism%5D%20AND%20hemagglutinin%5BAll%20Fields%5D%20AND%20(%222021/01/01%22%5B%60PDAT%5D%20:%20%222021/01/03%22%5BPDAT%5D)&usehistory=y")
                                       .willReturn(aResponse()
                                                           .withHeader("Content-Type", "text/xml")
                                                           .withBody(esearch)));

        wireMockServer.stubFor(WireMock.get("/entrez/eutils/efetch.fcgi?db=nucleotide&WebEnv=MCID_61f6329fbc9d2b299b4979e0&query_key=1&retmode=xml&rettype=fasta").willReturn(aResponse()
                                                                                          .withHeader("Content-Type", "text/xml")
                                                                                          .withBody(efetch)));
        //        this.webClient = WebClient.builder()
        //                .baseUrl(NCBI_EUTILS_BASE_URL) //FIXME - one from properties is not working
        ////                .baseUrl("https://5fb6fe54-3b8f-4ab4-8963-f69b898d9b64.mock.pstmn.io/")
        //                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
        //                .filters(exchangeFilterFunctions -> {
        //                    exchangeFilterFunctions.add(Utils.logRequest());
        //                })
        //                .build();
    }


    public Mono<EsearchResponse> downloadSequencesFrom2(LocalDate from, LocalDate to) {
        return esearchNcbi(from, to);
    }


    public Mono<SequenceServiceForTest.SequenceTest> downloadSequencesFrom(LocalDate from, LocalDate to) {
        return esearchNcbi(from, to).flatMap(this::efetchNcbi);
    }


    public Mono<SequenceServiceForTest.SequenceTest> efetchNcbi(EsearchResponse esearchResponse) {
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(NCBI_EFETCH_PATH_SEGMENT)
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
                                                         .uri(uriBuilder -> uriBuilder.path(NCBI_ESEARCH_PATH_SEGMENT)
                                                                                      .queryParam("db", "nucleotide")
                                                                                      .queryParam("term",
                                                                                                  termBuilder(
                                                                                                          "txid333278",
                                                                                                          from,
                                                                                                          to))
                                                                                      .queryParam("usehistory", "y")
                                                                                      .build())
                                                         .retrieve()
                                                         .bodyToMono(EsearchResponse.class);

        return esearchResponse;
    }


    private String termBuilder(String organism, LocalDate from, LocalDate to) {
        return String.format("%s[Organism] AND hemagglutinin[All Fields] AND (\"%s\"[`PDAT] : \"%s\"[PDAT])",
                             organism,
                             formatter.format(from),
                             formatter.format(to));
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

    private final static String esearch = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                                          "<!DOCTYPE eSearchResult PUBLIC \"-//NLM//DTD esearch 20060628//EN\" \"https://eutils.ncbi.nlm.nih.gov/eutils/dtd/20060628/esearch.dtd\">\n" +
                                          "<eSearchResult>\n" + "    <Count>36</Count>\n" + "    <RetMax>6</RetMax>\n" +
                                          "    <RetStart>0</RetStart>\n" + "    <QueryKey>1</QueryKey>\n" +
                                          "    <WebEnv>MCID_61f6329fbc9d2b299b4979e0</WebEnv>\n" + "    <IdList>\n" +
                                          "        <Id>479280293</Id>\n" + "        <Id>478735268</Id>\n" +
                                          "        <Id>390535061</Id>\n" + "        <Id>475662453</Id>\n" +
                                          "        <Id>482661570</Id>\n" + "        <Id>444344487</Id>\n" +
                                          "    </IdList>\n" + "    <TranslationSet/>\n" + "    <TranslationStack>\n" +
                                          "        <TermSet>\n" + "            <Term>txid333278[Organism]</Term>\n" +
                                          "            <Field>Organism</Field>\n" +
                                          "            <Count>8590</Count>\n" + "            <Explode>Y</Explode>\n" +
                                          "        </TermSet>\n" + "        <TermSet>\n" +
                                          "            <Term>hemagglutinin[All Fields]</Term>\n" +
                                          "            <Field>All Fields</Field>\n" +
                                          "            <Count>357127</Count>\n" + "            <Explode>N</Explode>\n" +
                                          "        </TermSet>\n" + "        <OP>AND</OP>\n" + "        <TermSet>\n" +
                                          "            <Term>\"2013/01/01\"[PDAT]</Term>\n" +
                                          "            <Field>PDAT</Field>\n" + "            <Count>0</Count>\n" +
                                          "            <Explode>N</Explode>\n" + "        </TermSet>\n" +
                                          "        <TermSet>\n" + "            <Term>\"2013/4/31\"[PDAT]</Term>\n" +
                                          "            <Field>PDAT</Field>\n" + "            <Count>0</Count>\n" +
                                          "            <Explode>N</Explode>\n" + "        </TermSet>\n" +
                                          "        <OP>RANGE</OP>\n" + "        <OP>GROUP</OP>\n" +
                                          "        <OP>AND</OP>\n" + "    </TranslationStack>\n" +
                                          "    <QueryTranslation>txid333278[Organism] AND hemagglutinin[All Fields] AND (\"2013/01/01\"[PDAT] : \"2013/4/31\"[PDAT])</QueryTranslation>\n" +
                                          "</eSearchResult>";

    private static final String efetch = "<?xml version=\"1.0\" encoding=\"UTF-8\"  ?>\n" +
                                         "<!DOCTYPE TSeqSet PUBLIC \"-//NCBI//NCBI TSeq/EN\" \"https://www.ncbi.nlm.nih.gov/dtd/NCBI_TSeq.dtd\">\n" +
                                         "<TSeqSet>\n" + "    <TSeq>\n" +
                                         "        <TSeq_seqtype value=\"nucleotide\"/>\n" +
                                         "        <TSeq_accver>KC899669.1</TSeq_accver>\n" +
                                         "        <TSeq_taxid>1322048</TSeq_taxid>\n" +
                                         "        <TSeq_orgname>Influenza A virus (A/chicken/Zhejiang/DTID-ZJU01/2013(H7N9))</TSeq_orgname>\n" +
                                         "        <TSeq_defline>Influenza A virus (A/chicken/Zhejiang/DTID-ZJU01/2013(H7N9)) segment 4 hemagglutinin (HA) gene, complete cds</TSeq_defline>\n" +
                                         "        <TSeq_length>1683</TSeq_length>\n" +
                                         "        <TSeq_sequence>ATGAACACTCAAATCCTGGTATTCGCTCTGATTGCGATCATTCCAACAAATGCAGACAAAATCTGCCTCGGACATCATGCCGTGTCAAACGGAACCAAAGTAAACACTTTAACTGAAAGAGGAGGGGAAGTCGTCAATGCAACTGAAACAGTGGAACGAACAAACATCCCCAGGATCTGCTCAAAGGGGAAAAAGACAGTTGACCTCGGTCAAGGGGGACCCCGGGGGACAATCACTGGACCACCTCAATGTGACCAATTCCTAGAATTTTCAGCCGATTTAATTATGGAGAGGCGAGAAGGAAGTGATGTCTGTTATCCGGGGAAATTCGTGAATGAAGAAGCTCTGAGGCAAATTCTCAGAGAATCAGGCGGAATTGACAAGGAAGCAATGGGATTCACATACAGTGGAATAAGAACTAATGGAGCAACCAGTGCATGTAGGAGATCAGGATCTTCATTCTATGCAGAAATGAAATGGCTCCTGTCAAACACAGATAATGCTGCATTCCCGCAGATGACTAAGTCATATAAAAATACAAGAAAAAGCCCAGCTCTAATAGTATGGGGGATCCATCATTCCGTATCAACTGCAGAGCAAACCAAGCTATATGGGAGTGGAAACAAACTGGTGACAGTTGGGAGTTCTAATTATCAACAATCTTTTGTACCGAGTCCAGGAGCGAGACCACAAGTTAATGGTCAATCTGGAAGAATTGACTTTCATTGGCTAATGCTAAATCCCAATGATACAGTCACTTTCAGTTTCAATGGGGCTTTCATAGCTCCAGACCGTGCAAGCTTCCTGAGAGGAAAATCTATGGGAATCCAGAGTGGAGTACAGGTTGATGCCAATTGTGAAGGGGACTGCTATCATAGTGGAGGGACAATAATAAGTAACTTGCCATTTCAGAACATAGATAGCAGGGCAGTTGGAAAATGTCCGAGATATGTTAAGCAAAGGAGTCTGCTGCTAGCAACAGGGATGAAGAATGTTCCTGAGATTCCAAAGGGAAGAGGCCTATTTGGTGCTATAGCGGGTTTCATTGAAAATGGATGGGAAGGCCTAATTGATGGTTGGTATGGTTTCAGACACCAGAATGCACAGGGAGAGGGAACTGCTGCAGATTACAAAAGCACTCAATCGGCAATTGATCAAATAACAGGAAAATTAAACCGGCTTATAGAAAAAACCAACCAACAATTTGAGTTGATAGACAATGAATTCAATGAGGTAGAGAAGCAAATCGGTAATGTGATAAATTGGACCAGAGATTCTATAACAGAAGTGTGGTCATACAATGCTGAACTCTTGGTAGCAATGGAGAACCAGCATACAATTGATCTGGCTGATTCAGAAATGGACAAACTGTACGAACGAGTGAAAAGACAGCTGAGAGAGAATGCTGAAGAAGATGGCACTGGTTGCTTTGAAATATTTCACAAGTGTGATGATGACTGTATGGCCAGTATTAGAAATAACACCTATGATCACAGCAAATACAGGGAAGAGGCAATGCAAAATAGAATACAGATTGACCCAGTCAAACTAAGCAGCGGCTACAAAGATGTGATACTTTGGTTTAGCTTCGGGGCATCATGTTTCATACTTCTAGCCATTGTAATGGGCCTTGTCTTCATATGTGTAAAGAATGGAAACATGCGGTGCACTATTTGTATATAA</TSeq_sequence>\n" +
                                         "    </TSeq>\n" + "    <TSeq>\n" +
                                         "        <TSeq_seqtype value=\"nucleotide\"/>\n" +
                                         "        <TSeq_accver>KC885956.1</TSeq_accver>\n" +
                                         "        <TSeq_taxid>1318616</TSeq_taxid>\n" +
                                         "        <TSeq_orgname>Influenza A virus (A/Zhejiang/DTID-ZJU01/2013(H7N9))</TSeq_orgname>\n" +
                                         "        <TSeq_defline>Influenza A virus (A/Zhejiang/DTID-ZJU01/2013(H7N9)) segment 4 hemagglutinin (HA) gene, complete cds</TSeq_defline>\n" +
                                         "        <TSeq_length>1710</TSeq_length>\n" +
                                         "        <TSeq_sequence>ATGAACACTCAAATCCTGGTATTCGCTCTGATTGCGATCATTCCAACAAATGCAGACAAAATCTGCCTCGGACATCATGCCGTGTCAAACGGAACCAAAGTAAACACATTAACTGAAAGAGGAGTGGAAGTCGTCAATGCAACTGAAACAGTGGAACGAACAAACATCCCCAGGATCTGCTCAAAAGGGAAAAGGACAGTTGACCTCGGTCAATGTGGACTCCTGGGGACAATCACTGGACCACCTCAATGTGACCAATTCCTAGAATTTTCAGCCGATTTAATTATTGAGAGGCGAGAAGGAAGTGATGTCTGTTATCCTGGGAAATTCGTGAATGAAGAAGCTCTGAGGCAAATTCTCAGAGAATCAGGCGGAATTGACAAGGAAGCAATGGGATTCACATACAGTGGAATAAGAACTAATGGAGCAACCAGTGCATGTAGGAGATCAGGATCTTCATTCTATGCAGAAATGAAATGGCTCCTGTCAAACACAGATAATGCTGCATTCCCGCAGATGACTAAGTCATATAAAAATACAAGAAAAAGCCCAGCTCTAATAGTATGGGGGATCCATCATTCCGTATCAACTGCAGAGCAAACCAAGCTATATGGGAGTGGAAACAAACTGGTGACAGTTGGGAGTTCTAATTATCAACAATCTTTTGTACCGAGTCCAGGAGCGAGACCACAAGTTAATGGTCTATCTGGAAGAATTGACTTTCATTGGCTAATGCTAAATCCCAATGATACAGTCACTTTCAGTTTCAATGGGGCTTTCATAGCTCCAGACCGTGCAAGCTTCCTGAGAGGAAAATCTATGGGAATCCAGAGTGGAGTACAGGTTGATGCCAATTGTGAAGGGGACTGCTATCATAGTGGAGGGACAATAATAAGTAACTTGCCATTTCAGAACATAGATAGCAGGGCAGTTGGAAAATGTCCGAGATATGTTAAGCAAAGGAGTCTGCTGCTAGCAACAGGGATGAAGAATGTTCCTGAGATTCCAAAGGGAAGAGGCCTATTTGGTGCTATAGCGGGTTTCATTGAAAATGGATGGGAAGGCCTAATTGATGGTTGGTATGGTTTCAGACACCAGAATGCACAGGGAGAGGGAACTGCTGCAGATTACAAAAGCACTCAATCGGCAATTGATCAAATAACAGGAAAATTAAACCGGCTTATAGAAAAAACCAACCAACAATTTGAGTTGATAGACAATGAATTCAATGAGGTAGAGAAGCAAATCGGTAATGTGATAAATTGGACCAGAGATTCTATAACAGAAGTGTGGTCATACAATGCTGAACTCTTGGTAGCAATGGAGAACCAGCATACAATTGATCTGGCTGATTCAGAAATGGACAAACTGTACGAACGAGTGAAAAGACAGCTGAGAGAGAATGCTGAAGAAGATGGCACTGGTTGCTTTGAAATATTTCACAAGTGTGATGATGACTGTATGGCCAGTATTAGAAATAACACCTATGATCACAGCAAATACAGGGAAGAGGCAATGCAAAATAGAATACAGATTGACCCAGTCAAACTAAGCAGCGGCTACAAAGATGTGATACTTTGGTTTAGCTTCGGGGCATCATGTTTCATACTTCTAGCCATTGTAATGGGCCTTGTCTTCATATGTGTAAAGAATGGAAACATGCGGTGCACTATTTGTATATAAGTTTGGAAAAAACACCCTTGTTTCTAC</TSeq_sequence>\n" +
                                         "    </TSeq>\n" + "    <TSeq>\n" +
                                         "        <TSeq_seqtype value=\"nucleotide\"/>\n" +
                                         "        <TSeq_accver>JX080746.1</TSeq_accver>\n" +
                                         "        <TSeq_taxid>1192939</TSeq_taxid>\n" +
                                         "        <TSeq_orgname>Influenza A virus (A/emperor goose/Alaska/44063-061/2006(H7N9))</TSeq_orgname>\n" +
                                         "        <TSeq_defline>Influenza A virus (A/emperor goose/Alaska/44063-061/2006(H7N9)) segment 4 hemagglutinin (HA) gene, partial cds</TSeq_defline>\n" +
                                         "        <TSeq_length>1678</TSeq_length>\n" +
                                         "        <TSeq_sequence>CTCAAATTTTGGCATTCATTGCTTGTATGCTGATTGGAGCTAAAGGAGACAAAATATGTCTTGGGCACCATGCTGTGGCAAATGGAACGAAAGTGAACACATTAACAGAGAGGGGAATTGAAGTAGTAAATGCCACGGAGACGGTGGAGACTGTAAATATTAAGAAAATATGCACTCAAGGGAAAAGGCCAACAGATCTGGGACAATGTGGACTTCTAGGAACCCTAATAGGACCTCCCCAATGCGATCAATTTCTGGAGTTTGACGCTGATTTGATAATCGAACGAAGAAAAGGAACCGATGTGTGCTACCCCGGGAAGTTCACAAATGAAGAATCACTGAGGCAGATCCTTCGAGGGTCAGGAGGAATTGATAAGGAGTCAATGGGTTTCACCTATAGTGGAATAAGAACCAATGGGGCGACGAGTGCCTGCAGAAGATCAGGTTCTTCTTTCTATGCGGAGATGAAGTGGTTACTGTCGAATTCAGACAATGCGGCATTTCCCCAAATGACTAAGTCGTATAGAAATCCCAGGAACAAACCAGCTCTGATAATTTGGGGAGTGCATCACTCTGGATCGGCTACTGAGCAGACCAAACTCTATGGAAGTGGAAACAAGTTGATAACAGTAGGAAGCTCGAAATACCAGCAATCATTCGTCCCAAGTCCGGGAGCACGGCCACAGGTGAATGGACAATCAGGAAGGATTGATTTTCACTGGCTACTCCTTGATCCCAACGACACAGTGACCTTCACTTTCAATGGGGCATTCATAGCTCCTGAAAGGGCAAGTTTCTTTAGAGGAGAATCGCTAGGAGTCCAGAGTGATGTTCCTTTAGATTCTGGTTGTGAGGGGGATTGCTTCCACAGCGGGGGTACGATAGTAAGCTCCCTGCCATTCCAGAACATCAACCCTAGAACAGTGGGGAAATGCCCTCGATATGTCAAACAGACAAGCCTCCTTTTGGCTACAGGAATGAGAAACGTCCCAGAGAACCCCAAGACCAGAGGCCTTTTTGGGGCGATTGCTGGATTCATAGAGAATGGATGGGAAGGTCTCATTGATGGATGGTATGGTTTCAGACATCAAAATGCACAAGGAGAAGGAACTGCAGCTGACTACAAAAGCACCCAATCTGCAATAGATCAGATCACAGGTAAATTGAATCGTCTAATTGACAAAACAAATCAGCAGTTTGAACTTATAGACAATGAATTCAGTGAAATAGAACAACAAATTGGGAATGTCATTAACTGGACACGAGACTCAATGACTGAGGTATGGTCATACAATGCTGAGTTGCTGGTAGCAATGGAAAATCAGCACACAATAGATCTTGCAGACTCAGAAATGAACAAACTTTACGAGCGTGTCAGAAAACAATTAAGGGAGAATGCTGAAGAAGATGGGACTGGATGCTTTGAGATATTTCATAAGTGTGATGATCAGTGTATGGAGAGCATAAGGAATAACACTTATGACCATACCCAATACAGAACAGAGTCACTGCAGAATAGAATACAGATAAACCCAGTGAAATTGAGTAGTGGATACAAAGACATAATCTTATGGTTTAGCTTCGGGGCATCATGTTTTCTTCTTCTAGCCATTGCAATGGGATTGGTTTTCATTTGCATAAAGAATGGAAACATGCGGTGCACTATTTGTATATAGTT</TSeq_sequence>\n" +
                                         "    </TSeq>\n" + "    <TSeq>\n" +
                                         "        <TSeq_seqtype value=\"nucleotide\"/>\n" +
                                         "        <TSeq_accver>KC853766.1</TSeq_accver>\n" +
                                         "        <TSeq_taxid>1314758</TSeq_taxid>\n" +
                                         "        <TSeq_orgname>Influenza A virus (A/Hangzhou/1/2013(H7N9))</TSeq_orgname>\n" +
                                         "        <TSeq_defline>Influenza A virus (A/Hangzhou/1/2013(H7N9)) segment 4 hemagglutinin (HA) gene, complete cds</TSeq_defline>\n" +
                                         "        <TSeq_length>1683</TSeq_length>\n" +
                                         "        <TSeq_sequence>ATGAACACTCAAATCCTGGTATTCGCTCTGATTGCGATCATTCCAACAAATGCAGACAAAATCTGCCTCGGACATCATGCCGTGTCAAACGGAACCAAAGTAAACACATTAACTGAAAGAGGAGTGGAAGTCGTCAATGCAACTGAAACAGTGGAACGAACAAACATCCCCAGGATCTGCTCAAAAGGGAAAAGGACAGTTGACCTCGGTCAATGTGGACTCCTGGGGACAATCACTGGACCACCTCAATGTGACCAATTCCTAGAATTTTCAGCCGATTTAATTATTGAGAGGCGAGAAGGAAGTGATGTCTGTTATCCTGGGAAATTCGTGAATGAAGAAGCTCTGAGGCAAATTCTCAGAGAATCAGGCGGAATTGACAAGGAAGCAATGGGATTCACATACAGTGGAATAAGAACTAATGGAGCAACCAGTGCATGTAGGAGATCAGGATCTTCATTCTATGCAGAAATGAAATGGCTCCTGTCAAACACAGATAATGCTGCATTCCCGCAGATGACTAAGTCATATAAAAATACAAGAAAAAGCCCAGCTCTAATAGTATGGGGGATCCATCATTCCGTATCAACTGCAGAGCAAACCAAGCTATATGGGAGTGGAAACAAACTGGTGACAGTTGGGAGTTCTAATTATCAACAATCTTTTGTACCGAGTCCAGGAGCGAGACCACAAGTTAATGGTATATCTGGAAGAATTGACTTTCATTGGCTAATGCTAAATCCCAATGATACAGTCACTTTCAGTTTCAATGGGGCTTTCATAGCTCCAGACCGTGCAAGCTTCCTGAGAGGAAAATCTATGGGAATCCAGAGTGGAGTACAGGTTGATGCCAATTGTGAAGGGGACTGCTATCATAGTGGAGGGACAATAATAAGTAACTTGCCATTTCAGAACATAGATAGCAGGGCAGTTGGAAAATGTCCGAGATATGTTAAGCAAAGGAGTCTGCTGCTAGCAACAGGGATGAAGAATGTTCCTGAGATTCCAAAGGGAAGAGGCCTATTTGGTGCTATAGCGGGTTTCATTGAAAATGGATGGGAAGGCCTAATTGATGGTTGGTATGGTTTCAGACACCAGAATGCACAGGGAGAGGGAACTGCTGCAGATTACAAAAGCACTCAATCGGCAATTGATCAAATAACAGGAAAATTAAACCGGCTTATAGAAAAAACCAACCAACAATTTGAGTTGATCGACAATGAATTCAATGAGGTAGAGAAGCAAATCGGTAATGTGATAAATTGGACCAGAGATTCTATAACAGAAGTGTGGTCATACAATGCTGAACTCTTGGTAGCAATGGAGAACCAGCATACAATTGATCTGGCTGATTCAGAAATGGACAAACTGTACGAACGAGTGAAAAGACAGCTGAGAGAGAATGCTGAAGAAGATGGCACTGGTTGCTTTGAAATATTTCACAAGTGTGATGATGACTGTATGGCCAGTATTAGAAATAACACCTATGATCACAGCAAATACAGGGAAGAGGCAATGCAAAATAGAATACAGATTGACCCAGTCAAACTAAGCAGCGGCTACAAAGATGTGATACTTTGGTTTAGCTTCGGGGCATCATGTTTCATACTTCTAGCCATTGTAATGGGCCTTGTCTTCATATGTGTAAAGAATGGAAACATGCGGTGCACTATTTGTATATAA</TSeq_sequence>\n" +
                                         "    </TSeq>\n" + "    <TSeq>\n" +
                                         "        <TSeq_seqtype value=\"nucleotide\"/>\n" +
                                         "        <TSeq_accver>AB813056.1</TSeq_accver>\n" +
                                         "        <TSeq_taxid>1316119</TSeq_taxid>\n" +
                                         "        <TSeq_orgname>Influenza A virus (A/duck/Gunma/466/2011(H7N9))</TSeq_orgname>\n" +
                                         "        <TSeq_defline>Influenza A virus (A/duck/Gunma/466/2011(H7N9)) HA gene for hemagglutinin, complete cds</TSeq_defline>\n" +
                                         "        <TSeq_length>1683</TSeq_length>\n" +
                                         "        <TSeq_sequence>ATGAACACTCAAGTCCTGGTATTCGCCCTGATGGCGATCATTCCAACAAATGCAGACAAGATCTGCCTTGGGCATCATGCCGTGTCAAATGGAACCAAAGTAAACACATTAACTGAAAGGGGAGTGGAAGTCGTTAATGCAACTGAAACGGTGGAACGAACAAATGTCCCCAGGATCTGCTCAAAAGGAAAAAGAACGGTTGACCTCGGTCAATGTGGGCTTCTGGGGACGATCACTGGACCACCCCAATGTGACCAATTCCTAGAATTTTCAGCCGATCTAATCATTGAAAGGCGAGAAGGAAGTGATGTTTGTTATCCTGGGAAATTCGTGAATGAAGAAGCTCTGAGGCAGATTCTCCGGGAGTCTGGCGGAATTGACAAGGAGACAATGGGATTCACATATAGCGGGATAAGAACTAATGGAACAACCAGTGCATGTAGGAGATCAGGATCTTCATTCTATGCAGAGATGAAATGGCTCCTTTCAAACACAGACAATGCTGCTTTCCCGCAGATGACTAAGTCATACAAAAACACAAGGAGAGACCCAGCTCTGATAGCATGGGGAATCCACCATTCCGGATCAACCACAGAACAGACCAAGCTATATGGGAGTGGAAGCAAGTTGATAACAGTTGGGAGTTCTAATTACCAACAGTCCTTTGTACCGAGCCCAGGAGCGAGGCCACAAGTGAATGGCCAATCTGGAAGGATTGACTTTCATTGGCTGATACTAAATCCCAATGACACAGTTACTTTCAGTTTCAATGGGGCCTTCATAGCTCCAGACCGCGCAAGCTTTTTGAGAGGGAAGTCTATGGGAATTCAGAGTGGAGTACAGGTTGATGCAAGTTGTGAAGGAGATTGTTATCATAGTGGAGGAACAATAATAAGCAATTTGCCCTTTCAGAACATAAATAGCAGAGCAGTAGGGAAATGCCCGAGATATGTCAAGCAAGAGAGTTTGATGCTGGCAACAGGGATGAAAAATGTTCCCGAACTCCCAAAGGGAAGAGGCCTATTTGGTGCTATAGCGGGTTTCATTGAGAATGGATGGGAAGGTCTGATTGACGGGTGGTATGGCTTCAGACACCAAAATGCACAAGGGGAGGGAACTGCTGCAGATTACAAAAGCACCCAATCTGCAATTGATCAAATAACAGGGAAATTAAACCGGCTTATAGAAAAAACCAACCAACAATTTGAGTTGATAGACAATGAATTCACTGAGGTTGAAAAGCAAATTGGCAATGTGATAAACTGGACCAGAGATTCCATGACAGAAGTGTGGTCCTATAATGCTGAACTCTTGGTAGCAATGGAGAATCAACACACAATTGATCTGGCTGACTCAGAAATGAATAAACTATACGAGCGGGTGAGAAGGCAACTGAGAGAGAACGCTGAAGAAGACGACACTGGCTGTTTTGAAATATTCCACAAGTGTGATGACGACTGCATGGCCAGTATCAGAAACAACACTTATGATCACAGCAAATACAGGGAGGAGGCGATGCAAAATAGAATACAGATTGACCCGGTCAAACTAAGCAGTGGTTATAAAGATGTGATACTTTGGTTTAGCTTCGGGGCATCATGTTTCATACTTCTTGCCATTGCAATGGGCCTTGTCTTCATATGTGTGAAGAATGGAAACATGCGGTGCACTATTTGTATATAA</TSeq_sequence>\n" +
                                         "    </TSeq>\n" + "    <TSeq>\n" +
                                         "        <TSeq_seqtype value=\"nucleotide\"/>\n" +
                                         "        <TSeq_accver>CY133649.1</TSeq_accver>\n" +
                                         "        <TSeq_sid>gnl|NIGSP|NIGSP_CEIRS_UMN033_COH2_00131.HA</TSeq_sid>\n" +
                                         "        <TSeq_taxid>1267730</TSeq_taxid>\n" +
                                         "        <TSeq_orgname>Influenza A virus (A/northern shoverl/Mississippi/11OS145/2011(H7N9))</TSeq_orgname>\n" +
                                         "        <TSeq_defline>Influenza A virus (A/northern shoverl/Mississippi/11OS145/2011(H7N9)) hemagglutinin (HA) gene, complete cds</TSeq_defline>\n" +
                                         "        <TSeq_length>1706</TSeq_length>\n" +
                                         "        <TSeq_sequence>GGATACAAAATGAACACTCAAATTTTGACACTCATTGCTTGTATGCTGATTGGAGCTAAAGGAGATAAAATATGTCTTGGGCACCATGCTGTGGCAAATGGAACAAAAGTGAACACATTAACAGAGAGAGGAATCGAAGTAGTAAATGCCACAGAAACGGTGGAGACTGCAAATATTAAGAAAATATGCACTCAGGGGAAAAGACCAACAGATCTGGGACAATGCGGACTCCTAGGAACCCTAATAGGACCTCCCCAATGCGATCAATTTCTGGAGTTTGACGCTGATTTAATAATTGAACGAAGAGAAGGAACCGATGTGTGTTATCCCGGGAAGTTCACAAATGAAGAATCACTGAGGCAGATCCTTCGAGGGTCAGGAGGAATTGATAAGGAGTCAATGGGTTTCACCTATAGTGGAATAAGAACCAATGGGGCGACGAGTGCTTGCAGAAGATCAGGTTCTTCCTTCTATGCGGAGATGAAGTGGTTACTGTCGAATTCAGACAATGCGGCTTTTCCCCAAATGACTAAGTCGTACAGAAATCCCAGGAACAAACCAGCTCTGATAATTTGGGGAGTGCATCACTCTGGATCGGCTACTGAGCAGACCAAACTCTATGGGAGTGGAAATAAGTTGATAACAGTAGGAAGCTCGAAATACCAGCAGTCATTCACCCCAAGCCCGGGGGCACGACCACAGGTGAATGGGCAATCAGGAAGGATTGATTTTCACTGGCTACTCCTTGATCCCAATGACACAGTGACCTTCACTTTCAATGGGGCATTCATAGCTCCTGACAGAGCAAGTTTCTTTAGAGGAGAGTCACTAGGAGTTCAGAGTGATGTTCCTTTGGATTCTGGTTGTGAGGGGGATTGCTTCCACAATGGGGGTACGATAGTGAGCTCCCTGCCATTCCAGAACATCAACCCTAGAACAGTGGGAAAATGCCCTCGATATGTCAAACAGACAAGCCTCCTTTTGGCTACAGGGATGAGAAACGTCCCAGAGAACCCCAAGACCAGAGGCCTTTTTGGAGCGATTGCTGGATTCATAGAGAATGGATGGGAAGGTCTCATTGATGGATGGTATGGTTTCAGACATCAAAATGCACAAGGAGAAGGAACTGCAGCTGATTATAAAAGCACTCAATCTGCAATAGATCAGATCACAGGCAAATTGAATCGTCTAATCGACAAAACAAATCAGCAGTTTGAACTGATAGACAACGAATTCAGTGAAATAGAACAACAAATTGGGAATGTCATTAACTGGACACGAGATTCAATGACTGAGGTATGGTCGTACAATGCTGAATTGCTGGTAGCTATGGAAAATCAGCACACAATAGATCTTGCAGACTCAGAAATGAACAAACTTTATGAGCGTGTAAGGAAACAACTGAGGGAGAATGCTGAAGAGGATGGGACTGGATGCTTTGAGATATTTCATAAGTGTGATGATCAGTGCATGGAGAGCATAAGGAACAACACTTATGACCATACTCAATACAGAGCGGAGTCATTGCAGAATAGAATACAGATAGACCCAGTGAAATTGAGTAGTGGATACAAAGACATAATCTTATGGTTTAGCTTCGGGGCATCATGTTTTCTTCTTCTAGCCATTGCAATGGGATTGGTTTTCATTTGCATAAAGAATGGAAACATGCGGTGCACTATTTGTATATAGTTTGAGAAAAACAC</TSeq_sequence>\n" +
                                         "    </TSeq>\n" + "</TSeqSet>";
}
