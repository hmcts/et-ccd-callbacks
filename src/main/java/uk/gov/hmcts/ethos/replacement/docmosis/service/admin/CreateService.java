package uk.gov.hmcts.ethos.replacement.docmosis.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.CreateServiceException;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;

@Service
@Slf4j
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField", "PMD.UnusedFormalParameter", "PMD.UnusedLocalVariable", "PMD.UnnecessaryLocalBeforeReturn"})
public class CreateService {

    public static final String ECM_ADMIN_CASE_TYPE_ID = "ET_Admin";
    public static final String CREATE_EXIST_ERROR_MESSAGE = "ECM Admin already exists.";

        private final CcdClient ccdClient;

        public CreateService(CcdClient ccdClient) {
            this.ccdClient = ccdClient;
        }

    public List<String> initCreateAdmin(String userToken) {
        List<String> errors = new ArrayList<>();
        if (existEcmAdminCaseTypeId(userToken)) {
            log.info(ECM_ADMIN_CASE_TYPE_ID);
            //            errors.add(CREATE_EXIST_ERROR_MESSAGE);
        }
        return errors;
    }

    private boolean existEcmAdminCaseTypeId(String userToken) {
        List<SubmitEvent> listSubmitEvents = new ArrayList<>();
        try {
            String query = boolQueryCreate();
            log.info(listSubmitEvents.toString());
        //            listSubmitEvents = ccdClient.executeElasticSearch(userToken, ECM_ADMIN_CASE_TYPE_ID, query);
        } catch (Exception ex) {
            throw new CreateServiceException("Error retrieving case for Create ECM Admin", ex);
        }
        return !listSubmitEvents.isEmpty();
    }

    private String boolQueryCreate() {
        BoolQueryBuilder boolQueryBuilder = boolQuery();
        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }

}
