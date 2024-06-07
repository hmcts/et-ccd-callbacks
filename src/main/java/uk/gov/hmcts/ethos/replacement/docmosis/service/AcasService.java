package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.acas.AcasCertificate;
import uk.gov.hmcts.ecm.common.model.acas.AcasCertificateRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@Service
@Slf4j
public class AcasService {

    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String NOT_FOUND = "not found";
    private final TornadoService tornadoService;
    private final RestTemplate restTemplate;

    private final String acasApiUrl;
    private final String acasApiKey;

    public AcasService(TornadoService tornadoService, RestTemplate restTemplate,
                       @Value("${acas.api.url}") String acasApiUrl,
                       @Value("${acas.api.key}") String acasApiKey) {
        this.tornadoService = tornadoService;
        this.restTemplate = restTemplate;
        this.acasApiUrl = acasApiUrl;
        this.acasApiKey = acasApiKey;
    }

    public List<String> getAcasCertificate(CaseData caseData, String authToken, String caseTypeId)
            throws JsonProcessingException {
        if (isNullOrEmpty(caseData.getAcasCertificate())) {
            return List.of("ACAS Certificate cannot be null or empty");
        }

        Object acasCertificateObject;
        try {
            acasCertificateObject = fetchAcasCertificates(caseData.getAcasCertificate()).getBody();
            if (ObjectUtils.isEmpty(acasCertificateObject)) {
                return List.of("Error reading ACAS Certificate");
            }
        } catch (Exception errorException) {
            log.error("Error retrieving ACAS Certificate with exception : " + errorException.getMessage());
            return List.of("Error retrieving ACAS Certificate");
        }

        AcasCertificate acasCertificate = convertAcasResponse((ArrayList) acasCertificateObject);
        if (NOT_FOUND.equals(acasCertificate.getCertificateDocument())) {
            return List.of("No ACAS Certificate found");
        }

        DocumentInfo documentInfo;
        try {
            documentInfo = convertCertificateToPdf(caseData, acasCertificate, authToken, caseTypeId);
        } catch (Exception exception) {
            log.error("Error converting ACAS Certificate with exception : " + exception.getMessage());
            return List.of("Error uploading ACAS Certificate");
        }

        documentInfo.setMarkUp(documentInfo.getMarkUp().replace("Document", documentInfo.getDescription()));
        caseData.setDocMarkUp(documentInfo.getMarkUp());
        caseData.setAcasCertificate(null);
        return new ArrayList<>();
    }

    private static AcasCertificate convertAcasResponse(ArrayList acasCertificate) throws JsonProcessingException {
        Object cert = acasCertificate.get(0);
        ObjectMapper objectMapper = new ObjectMapper();
        String certificate = objectMapper.writeValueAsString(cert);
        return objectMapper.readValue(certificate, AcasCertificate.class);
    }

    private DocumentInfo convertCertificateToPdf(CaseData caseData, AcasCertificate acasCertificate, String authToken,
                                                 String caseTypeId) {
        Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
            .filter(r -> acasCertificate.getCertificateNumber().equals(
                    defaultIfEmpty(r.getValue().getRespondentAcas(), "")))
            .findFirst();
        String acasName = "";
        if (respondent.isPresent()) {
            acasName = " - " + respondent.get().getValue().getRespondentName();
        }
        byte[] pdfData = Base64.getDecoder().decode(acasCertificate.getCertificateDocument());
        return tornadoService.createDocumentInfoFromBytes(authToken, pdfData,
                "ACAS Certificate" + acasName + " - " + acasCertificate.getCertificateNumber() + ".pdf",
                caseTypeId);
    }

    private ResponseEntity<Object> fetchAcasCertificates(String... acasCertificate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(OCP_APIM_SUBSCRIPTION_KEY, acasApiKey);
        AcasCertificateRequest acasCertificateRequest = new AcasCertificateRequest();
        acasCertificateRequest.setCertificateNumbers(acasCertificate);
        HttpEntity<AcasCertificateRequest> request = new HttpEntity<>(acasCertificateRequest, headers);
        return restTemplate.exchange(
                acasApiUrl,
                HttpMethod.POST,
                request,
                Object.class
        );
    }

    public DocumentInfo getAcasCertificates(CaseData caseData, String acasNumber, String authToken,
                                            String caseTypeId)
            throws JsonProcessingException {
        Object acasCertificateObject;
        try {
            acasCertificateObject = fetchAcasCertificates(acasNumber).getBody();
            AcasCertificate acasCertificate = convertAcasResponse((ArrayList) acasCertificateObject);
            DocumentInfo documentInfo = convertCertificateToPdf(caseData, acasCertificate, authToken, caseTypeId);
            documentInfo.setMarkUp(documentInfo.getMarkUp().replace("Document", documentInfo.getDescription()));
            return documentInfo;
        } catch (HttpClientErrorException errorException) {
            log.error("Error retrieving ACAS Certificate with exception : " + errorException.getMessage());
            throw errorException;
        } catch (JsonProcessingException e) {
            log.error("Error converting ACAS Certificate with exception : " + e.getMessage());
            throw e;
        }
    }

}
