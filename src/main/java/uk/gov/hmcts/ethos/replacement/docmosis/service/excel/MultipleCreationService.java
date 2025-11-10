package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.LegalRepDataModel;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersIdamUser;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.et.common.model.multiples.items.CaseMultipleTypeItem;
import uk.gov.hmcts.et.common.model.multiples.items.SubMultipleTypeItem;
import uk.gov.hmcts.et.common.model.multiples.types.MultipleObjectType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service("multipleCreationService")
public class MultipleCreationService {

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    private final ExcelDocManagementService excelDocManagementService;
    private final MultipleReferenceService multipleReferenceService;
    private final MultipleHelperService multipleHelperService;
    private final SubMultipleUpdateService subMultipleUpdateService;
    private final MultipleTransferService multipleTransferService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final FeatureToggleService featureToggleService;
    private final CcdClient ccdClient;
    private final AdminUserService adminUserService;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final CreateUpdatesBusSender createUpdatesBusSender;

    public void bulkCreationLogic(String userToken, MultipleDetails multipleDetails, List<String> errors)
        throws IOException {

        log.info("Add data to the multiple");

        addDataToMultiple(multipleDetails.getCaseData());

        log.info("Add state to the multiple");

        multipleDetails.getCaseData().setState(OPEN_STATE);

        MultipleData multipleData = multipleDetails.getCaseData();
        if (featureToggleService.isMultiplesEnabled()) {
            log.info("Setting Case Management Location");
            caseManagementLocationService.setCaseManagementLocation(multipleData);
        }

        log.info("Check if creation is coming from Case Transfer");

        multipleTransferService.populateDataIfComingFromCT(userToken, multipleDetails, errors);

        log.info("Get lead case link and add to the collection case Ids");

        setLeadMarkUpAndAddLeadToCaseIds(userToken, multipleDetails);
        if (featureToggleService.isMul2Enabled()) {
            addLegalRepsFromSinglesCases(multipleDetails);
        }

        if (multipleData.getMultipleSource().equals(ET1_ONLINE_CASE_SOURCE)
                || multipleData.getMultipleSource().equals(MIGRATION_CASE_SOURCE)) {

            multipleCreationET1OnlineMigration(userToken, multipleDetails);

        } else {

            log.info("Multiple Creation UI");

            multipleCreationUI(userToken, multipleDetails, errors);

        }

        log.info("Clearing the payload");

        clearingMultipleCreationPayload(multipleDetails);

    }

    private void addLegalRepsFromSinglesCases(MultipleDetails multipleDetails) throws IOException {
        MultipleData multipleData = multipleDetails.getCaseData();
        List<String> caseIds = multipleData.getCaseIdCollection().stream()
            .map(o -> o.getValue().getEthosCaseReference())
            .collect(Collectors.toList());

        if (caseIds.isEmpty()) {
            // No cases linked to this multiple means no legal reps to add
            return;
        }

        String token = adminUserService.getAdminUserToken();
        String multipleCaseTypeId = multipleDetails.getCaseTypeId();
        String singleCaseTypeId = MultiplesHelper.removeMultipleSuffix(multipleCaseTypeId);
        
        List<SubmitEvent> cases = ccdClient.retrieveCasesElasticSearch(token, singleCaseTypeId, caseIds);
        log.info("Retrieved {} cases from ES when adding legalreps to newly created multiple", cases.size());

        Map<Long, List<String>> emails = getUniqueLegalRepEmails(cases);

        if (emails.isEmpty()) {
            // No need to ask message-handler to update permissions if all cases have no legal reps
            return;
        }
        List<String> orgIds = getUniqueOrganisations(cases);
        List<OrganisationUsersIdamUser> users = orgIds.stream()
                .map(o -> organisationClient.getOrganisationUsers(token, authTokenGenerator.generate(), o))
                .flatMap(o -> Objects.requireNonNull(o.getBody()).getUsers().stream())
                .toList();

        Map<String, String> legalrepMap = buildEmailIdMap(users);

        HashMap<String, List<String>> legalRepsByCaseId = new HashMap<>();

        for (Entry<Long, List<String>> byCase : emails.entrySet()) {
            
            legalRepsByCaseId.put(byCase.getKey().toString(), new ArrayList<>());

            for (String userEmail : byCase.getValue()) {
                String legalRepId = legalrepMap.get(userEmail);

                legalRepsByCaseId.get(byCase.getKey().toString()).add(legalRepId);
            }
        }

        var ethosList = List.of(cases.get(0).getCaseData().getEthosCaseReference());

        CreateUpdatesDto createUpdatesDto = CreateUpdatesDto.builder()
            .caseTypeId(multipleCaseTypeId)
            .jurisdiction(multipleDetails.getJurisdiction())
            .multipleRef(multipleDetails.getCaseId())
            .ethosCaseRefCollection(ethosList)
            .build();

        DataModelParent legalRepDto = LegalRepDataModel.builder()
            .legalRepIdsByCase(legalRepsByCaseId)
            .caseType(multipleCaseTypeId)
            .multipleName(multipleDetails.getCaseData().getMultipleName())
            .build();

        createUpdatesBusSender.sendUpdatesToQueue(createUpdatesDto, legalRepDto, ethosList, "1");
    }

    private Map<String, String> buildEmailIdMap(List<OrganisationUsersIdamUser> users) {
        HashMap<String, String> emailIdMap = new HashMap<>();
        for (OrganisationUsersIdamUser user : users) {
            emailIdMap.put(user.getEmail(), user.getUserIdentifier());
        }
        return emailIdMap;
    }

    private Map<Long, List<String>> getUniqueLegalRepEmails(List<SubmitEvent> cases) {
        HashMap<Long, List<String>> caseRepEmails = new HashMap<>();

        for (SubmitEvent caseData : cases) {
            if (caseData.getCaseData().getRepCollection() == null) {
                continue;
            }
            List<String> repEmails = caseData.getCaseData().getRepCollection().stream()
                .map(o -> o.getValue().getRepresentativeEmailAddress())
                .filter(Objects::nonNull)
                .toList();

            caseRepEmails.put(caseData.getCaseId(), repEmails);
        }

        return caseRepEmails;
    }

    private List<String> getUniqueOrganisations(List<SubmitEvent> cases) {
        return cases.stream()
            .filter(o -> o.getCaseData().getRepCollection() != null)
            .flatMap(o -> o.getCaseData().getRepCollection().stream())
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespondentOrganisation)
            .filter(Objects::nonNull)
            .map(o -> o.getOrganisationID())
            .distinct()
            .toList();
    }

    private void multipleCreationUI(String userToken, MultipleDetails multipleDetails, List<String> errors) {

        MultipleData multipleData = multipleDetails.getCaseData();

        multipleData.setPreAcceptDone(YES);

        log.info("Filter duplicated and empty caseIds");

        multipleData.setCaseIdCollection(MultiplesHelper.filterDuplicatedAndEmptyCaseIds(multipleData));

        log.info("Create multiple reference number");

        multipleData.setMultipleReference(generateMultipleRef(multipleDetails));

        log.info("Create the EXCEL");

        List<String> ethosCaseRefCollection = MultiplesHelper.getCaseIds(multipleData);

        excelDocManagementService.generateAndUploadExcel(ethosCaseRefCollection, userToken, multipleDetails);

        log.info("Send updates to single cases");

        sendUpdatesToSingles(userToken, multipleDetails, errors, ethosCaseRefCollection);

    }

    private void multipleCreationET1OnlineMigration(String userToken, MultipleDetails multipleDetails) {

        if (multipleDetails.getCaseData().getMultipleSource().equals(MIGRATION_CASE_SOURCE)) {

            log.info("Multiple Creation Migration Logic");

            multipleDetails.getCaseData().setPreAcceptDone(YES);

            List<MultipleObject> multipleObjectList = new ArrayList<>();

            HashSet<String> subMultipleNames = new HashSet<>();

            multipleCreationMigrationLogic(multipleDetails, multipleObjectList, subMultipleNames);

            log.info("Generating the excel document for Migration");

            excelDocManagementService.writeAndUploadExcelDocument(
                    multipleObjectList,
                    userToken,
                    multipleDetails,
                    new ArrayList<>(subMultipleNames));

        } else {

            multipleDetails.getCaseData().setPreAcceptDone(NO);

            log.info("Generating the excel document for ET1 Online");

            excelDocManagementService.writeAndUploadExcelDocument(
                    MultiplesHelper.getCaseIds(multipleDetails.getCaseData()),
                    userToken,
                    multipleDetails,
                    new ArrayList<>());

        }

        log.info("Resetting creation fields");

        multipleDetails.getCaseData().setCaseMultipleCollection(null);

    }

    private void multipleCreationMigrationLogic(MultipleDetails multipleDetails,
                                                           List<MultipleObject> multipleObjectList,
                                                           Set<String> subMultipleNames) {

        List<CaseMultipleTypeItem> caseMultipleTypeItemList = multipleDetails.getCaseData().getCaseMultipleCollection();

        Set<SubMultipleTypeItem> subMultipleTypeItems = new HashSet<>();

        if (caseMultipleTypeItemList != null) {

            for (CaseMultipleTypeItem caseMultipleTypeItem : caseMultipleTypeItemList) {

                MultipleObjectType multipleObjectType = caseMultipleTypeItem.getValue();

                if (StringUtils.isNotBlank(multipleObjectType.getSubMultiple())
                        && !subMultipleNames.contains(multipleObjectType.getSubMultiple())) {

                    subMultipleNames.add(multipleObjectType.getSubMultiple());

                    log.info("Generating subMultiple type: " + multipleObjectType.getSubMultiple());

                    subMultipleTypeItems.add(
                            subMultipleUpdateService.createSubMultipleTypeItemWithReference(
                                    multipleDetails, multipleObjectType.getSubMultiple()));

                }

                multipleObjectList.add(generateMultipleObjectFromMultipleObjectType(multipleObjectType));

            }

        }

        log.info("Adding the subMultipleCollection coming from Migration");

        multipleDetails.getCaseData().setSubMultipleCollection(new ArrayList<>(subMultipleTypeItems));

    }

    private MultipleObject generateMultipleObjectFromMultipleObjectType(MultipleObjectType multipleObjectType) {

        return MultipleObject.builder()
                .ethosCaseRef(multipleObjectType.getEthosCaseRef())
                .subMultiple(multipleObjectType.getSubMultiple() != null ? multipleObjectType.getSubMultiple() : "")
                .flag1(multipleObjectType.getFlag1() != null ? multipleObjectType.getFlag1() : "")
                .flag2(multipleObjectType.getFlag2() != null ? multipleObjectType.getFlag2() : "")
                .flag3(multipleObjectType.getFlag3() != null ? multipleObjectType.getFlag3() : "")
                .flag4(multipleObjectType.getFlag4() != null ? multipleObjectType.getFlag4() : "")
                .build();

    }

    private String generateMultipleRef(MultipleDetails multipleDetails) {

        MultipleData multipleData = multipleDetails.getCaseData();

        if (StringUtils.isBlank(multipleData.getMultipleReference())) {

            log.info("Case Type: {}", multipleDetails.getCaseTypeId());

            return multipleReferenceService.createReference(multipleDetails.getCaseTypeId());

        } else {

            return multipleData.getMultipleReference();

        }
    }

    private void addDataToMultiple(MultipleData multipleData) {
        if (StringUtils.isBlank(multipleData.getMultipleSource())) {
            multipleData.setMultipleSource(MANUALLY_CREATED_POSITION);
        }
    }

    private void setLeadMarkUpAndAddLeadToCaseIds(String userToken, MultipleDetails multipleDetails) {

        MultipleData multipleData = multipleDetails.getCaseData();

        String leadCase;

        if (isNullOrEmpty(multipleData.getLeadCase())) {

            if (multipleDetails.getCaseData().getMultipleSource().equals(MIGRATION_CASE_SOURCE)) {

                log.info("Getting lead case from caseMultipleCollection");

                leadCase = MultiplesHelper.getLeadFromCaseMultipleCollection(multipleData);

            } else {

                log.info("Getting lead case from the case ids collection");

                leadCase = MultiplesHelper.getLeadFromCaseIds(multipleData);

            }

        } else {

            log.info("Adding lead case introduced by user: " + multipleData.getLeadCase());

            MultiplesHelper.addLeadToCaseIds(multipleData, multipleData.getLeadCase());

            leadCase = multipleData.getLeadCase();

        }

        multipleHelperService.addLeadMarkUp(userToken, multipleDetails.getCaseTypeId(),
                multipleData, leadCase, "");

    }

    private void sendUpdatesToSingles(String userToken, MultipleDetails multipleDetails,
                                      List<String> errors, List<String> ethosCaseRefCollection) {

        log.info("Ethos case ref collection: " + ethosCaseRefCollection);

        String refMarkup = MultiplesHelper.generateMarkUp(ccdGatewayBaseUrl, multipleDetails.getCaseId(),
                multipleDetails.getCaseData().getMultipleReference());

        if (ethosCaseRefCollection.isEmpty()) {

            log.info("Empty case ref collection");

        } else {

            multipleHelperService.sendCreationUpdatesToSinglesWithoutConfirmation(userToken,
                    multipleDetails.getCaseTypeId(),
                    multipleDetails.getJurisdiction(),
                    multipleDetails.getCaseData(),
                    errors,
                    ethosCaseRefCollection,
                    ethosCaseRefCollection.get(0),
                    refMarkup);

        }
    }

    private void clearingMultipleCreationPayload(MultipleDetails multipleDetails) {

        multipleDetails.getCaseData().setCaseIdCollection(null);

        if (multipleDetails.getCaseData().getMultipleSource().equals(MIGRATION_CASE_SOURCE)
                && multipleDetails.getCaseData().getLinkedMultipleCT() != null) {

            multipleDetails.getCaseData().setMultipleSource(MANUALLY_CREATED_POSITION);

        }

    }

}
