package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@Service
@Slf4j
public class AcasService {

    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String NOT_FOUND = "not found";
    private final TornadoService tornadoService;
    private final WebClient webClient;

    private final String acasApiUrl;
    private final String acasApiKey;

    public AcasService(TornadoService tornadoService, WebClient webClient,
                       @Value("${acas.api.url}") String acasApiUrl,
                       @Value("${acas.api.key}") String acasApiKey) {
        this.tornadoService = tornadoService;
        this.webClient = webClient;
        this.acasApiUrl = acasApiUrl;
        this.acasApiKey = acasApiKey;
    }

    public List<String> getAcasCertificate(CaseData caseData, String authToken, String caseTypeId) {
        if (isNullOrEmpty(caseData.getAcasCertificate())) {
            return List.of("ACAS Certificate cannot be null or empty");
        }

        List<AcasCertificate> acasCertificateObject;
        try {
            acasCertificateObject = fetchAcasCertificates(caseData.getAcasCertificate());
            if (ObjectUtils.isEmpty(acasCertificateObject)) {
                return List.of("Error reading ACAS Certificate");
            }
        } catch (Exception errorException) {
            log.error("Error retrieving ACAS Certificate with exception : {}", errorException.getMessage());
            return List.of("Error retrieving ACAS Certificate");
        }

        AcasCertificate acasCertificate = acasCertificateObject.getFirst();
        if (NOT_FOUND.equals(acasCertificate.getCertificateDocument())) {
            return List.of("No ACAS Certificate found");
        }

        DocumentInfo documentInfo;
        try {
            documentInfo = convertCertificateToPdf(caseData, acasCertificate, authToken, caseTypeId);
        } catch (Exception exception) {
            log.error("Error converting ACAS Certificate with exception : {}", exception.getMessage());
            return List.of("Error uploading ACAS Certificate");
        }

        documentInfo.setMarkUp(documentInfo.getMarkUp().replace("Document", documentInfo.getDescription()));
        caseData.setDocMarkUp(documentInfo.getMarkUp());
        caseData.setAcasCertificate(null);
        return new ArrayList<>();
    }

    private DocumentInfo convertCertificateToPdf(CaseData caseData, AcasCertificate acasCertificate, String authToken,
                                                 String caseTypeId) {
        Optional<RespondentSumTypeItem> respondent = emptyIfNull(caseData.getRespondentCollection()).stream()
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

    private List<AcasCertificate> fetchAcasCertificates(String... acasCertificate) {
        AcasCertificateRequest acasCertificateRequest = new AcasCertificateRequest();
        acasCertificateRequest.setCertificateNumbers(acasCertificate);
        
        return webClient.post()
                .uri(acasApiUrl)
                .header(OCP_APIM_SUBSCRIPTION_KEY, acasApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(acasCertificateRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AcasCertificate>>() {})
                .block(); // Convert reactive to blocking for existing synchronous API
    }

    public List<DocumentInfo> getAcasCertificates(CaseData caseData, List<String> acasNumber, String authToken,
                                            String caseTypeId) {
        List<AcasCertificate> acasCertificateList;
        try {
            acasCertificateList = fetchAcasCertificates(acasNumber.toArray(new String[0]));
            if (isEmpty(acasCertificateList)) {
                return new ArrayList<>();
            }
            return acasCertificateList.stream()
                    .map(ac -> createAcasCertificate(caseData, authToken, caseTypeId, ac))
                    .toList();
        } catch (Exception errorException) {
            log.error("Error retrieving ACAS Certificate with exception : {}", errorException.getMessage());
            throw errorException;
        }
    }

    private DocumentInfo createAcasCertificate(CaseData caseData, String authToken, String caseTypeId,
                                               AcasCertificate acasCertificate) {
        DocumentInfo documentInfo = convertCertificateToPdf(caseData, acasCertificate, authToken, caseTypeId);
        documentInfo.setMarkUp(documentInfo.getMarkUp().replace("Document", documentInfo.getDescription()));
        return documentInfo;
    }

}
