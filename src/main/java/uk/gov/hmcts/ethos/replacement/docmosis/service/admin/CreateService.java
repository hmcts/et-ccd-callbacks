package uk.gov.hmcts.ethos.replacement.docmosis.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;

@Slf4j
@RequiredArgsConstructor
@Service
public class CreateService {

    public static final String ECM_ADMIN_CASE_TYPE_ID = "ET_Admin";
    public static final String CREATE_EXIST_ERROR_MESSAGE = "ECM Admin already exists.";

    private final CcdClient ccdClient;

    public List<String> initCreateAdmin(String userToken) {
        List<String> errors = new ArrayList<>();
        if (existEcmAdminCaseTypeId(userToken)) {
            errors.add(CREATE_EXIST_ERROR_MESSAGE);
        }
        return errors;
    }

    private boolean existEcmAdminCaseTypeId(String userToken) {
        List<SubmitEvent> listSubmitEvents;
        try {
            String query = boolQueryCreate();
            listSubmitEvents = ccdClient.executeElasticSearch(userToken, ECM_ADMIN_CASE_TYPE_ID, query);
        } catch (Exception ex) {
            log.error("Error retrieving case for Create ECM Admin");
            throw new RuntimeException("Error retrieving case for Create ECM Admin", ex);
        }
        return !listSubmitEvents.isEmpty();
    }

    private String boolQueryCreate() {
        var boolQueryBuilder = boolQuery();
        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }

}
