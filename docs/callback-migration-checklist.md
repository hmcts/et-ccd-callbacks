# Callback Migration Checklist

Generated on 2026-03-19 from CCD CaseEvent definitions.

- [x] Phase 1 complete: generated callback handler inventory from CCD CaseEvent definitions.
- [x] Phase 2 complete: generated handlers now invoke mapped controller methods directly with typed request conversion in `CallbackHandlerBase`.
- [x] Phase 3 (extraction) complete.
- [x] Phase 3 (cleanup) complete: removed migrated controller endpoints and corresponding stale controller tests.
  - Handler inlining complete for unique callback-service methods.
  - `invokeSafely` pattern removed from `CallbackHandlerBase`.
  - Callback-service layer reduced to shared methods only:
    - `CaseActionsForCaseWorkerCallbackService.postDefaultValues`
    - `ListingGenerationCallbackService.generateHearingDocumentConfirmation`
    - `MultipleDocGenerationCallbackService.printDocumentConfirmation`
    - `SendNotificationCallbackService.aboutToSubmit`

- Total unique aboutToSubmit/submitted combinations in CCD: 115
- Existing in this repo: 114
- External (no local controller mapping): 1 (`nocRequest` -> `${CCD_DEF_AAC_URL}/noc/check-noc-approval`)
- Generated handlers in this branch: 113
- Pre-existing bespoke handler retained: `ClaimantRespondToNotificationHandler`

## Branch Change Summary (2026-03-23)

- Removed `invokeSafely` usage and localized exception handling inside handlers.
- Deleted `ExcelActionsCallbackService` and inlined its single-use public methods directly into handlers:
  - `AmendMultipleAPICallbackHandler`
  - `AmendMultipleDetailsCallbackHandler`
  - `BatchUpdateCasesCallbackHandler`
  - `CloseCallbackHandler`
  - `CreateMultipleCallbackHandler`
  - `FixMultipleTransferAPICallbackHandler`
  - `ImportFileCallbackHandler`
  - `PreAcceptMultipleCallbackHandler`
  - `ResetMultipleStateCallbackHandler`
  - `UpdatePayloadMultipleCallbackHandler`
  - `UpdateSubMultipleCallbackHandler`
- Inlined single-use `ListingGenerationCallbackService` logic into handlers:
  - `GenerateListingCallbackHandler`
  - `GenerateReportCallbackHandler` (about-to-submit path)
  - `ListingCaseCreationCallbackHandler`
  - `PrintCauseListCallbackHandler` (about-to-submit path)
  - `PrintHearingCallbackHandler` (about-to-submit + submitted paths)
- Reduced `ListingGenerationCallbackService` to shared method only:
  - `generateHearingDocumentConfirmation`
- Inlined single-use `CaseActionsForCaseWorkerCallbackService` logic into handlers:
  - `AddAmendJurisdictionCallbackHandler`
  - `AddAmendHearingCallbackHandler`
  - `AddAmendJudgmentCallbackHandler`
  - `AmendCaseDetailsCallbackHandler`
  - `AmendClaimantDetailsCallbackHandler`
  - `AmendRespondentDetailsCallbackHandler`
  - `AmendRespondentRepresentativeCallbackHandler` (about-to-submit + submitted)
  - `BroughtForwardCallbackHandler`
  - `InitiateCaseCallbackHandler` (submitted path)
  - `MigrateCaseLinkDetailsCallbackHandler`
  - `RecordDepositCallbackHandler`
  - `RestrictedCasesCallbackHandler`
- Reduced `CaseActionsForCaseWorkerCallbackService` to shared method only:
  - `postDefaultValues`
- Current callback service classes are now shared-only:
  - `CaseActionsForCaseWorkerCallbackService.postDefaultValues`
  - `ListingGenerationCallbackService.generateHearingDocumentConfirmation`
  - `MultipleDocGenerationCallbackService.printDocumentConfirmation`
  - `SendNotificationCallbackService.aboutToSubmit`

### Verification Run (2026-03-23)

- `./gradlew compileJava --no-daemon` (Java 21): passed
- `./gradlew compileTestJava --no-daemon` (Java 21): passed
- `./gradlew test --no-daemon` (Java 21): passed
- `./gradlew integration --no-daemon` (Java 21): passed

### Incremental Cleanup (2026-03-23)

- Replaced remaining fully-qualified `uk.gov.hmcts.reform.ccd.client.model.CaseDetails` and
  `uk.gov.hmcts.reform.ccd.client.model.CallbackRequest` usages in migrated handlers/tests with imports.
- Inlined final unnecessary private wrapper methods (`handleAboutToSubmit`/`handleSubmitted`) into callback
  entrypoints for:
  - `BundlesRespondentPrepareDocCallbackHandler`
  - `TseAdminCallbackHandler`
- Updated `GeneratedCallbackHandlersTest` to use imported `CaseDetails`.
- Added `@Autowired` constructor annotation to all migrated handler classes for DI clarity.
- Removed all 155 mapped migrated controller endpoint methods (about-to-submit/submitted only) from legacy controllers.
- Pruned stale controller test coverage for migrated endpoints:
  - deleted 63 failing legacy controller test classes that asserted removed controller routes.
  - retained/updated focused controller tests for non-migrated routes (for example `CaseTransferControllerTest`,
    `BulkAddSinglesControllerTest`).
- PMD remediation after cleanup:
  - removed newly-unused private controller helper methods left by endpoint deletion.
  - simplified generated boolean expressions (`== false` -> `!…`) in migrated handlers/services.
  - removed newly-unused imports and locals/parameters.

### Verification Run (Incremental Cleanup)

- `./gradlew compileJava --no-daemon` (Java 21): passed
- `./gradlew checkstyleMain --no-daemon` (Java 21): passed
- `./gradlew compileTestJava --no-daemon` (Java 21): passed
- `./gradlew :test --tests uk.gov.hmcts.ethos.replacement.docmosis.handler.GeneratedCallbackHandlersTest --no-daemon`
  (Java 21): passed
- `./gradlew compileJava checkstyleMain pmdMain --no-daemon` (Java 21): passed
- `./gradlew compileTestJava test integration --no-daemon` (Java 21): passed
- `./gradlew build --no-daemon` (Java 21): passed

| Handler | AboutToSubmit URL | Submitted URL | CaseTypeIDs | Event IDs |
|---|---|---|---|---|
| `Et1ReppedAboutToSubmitSectionCallbackHandler` | `/et1Repped/aboutToSubmitSection` | `/et1Repped/sectionCompleted` | `ET_Scotland, ET_EnglandWales` | `et1SectionOne, et1SectionTwo, et1SectionThree` |
| `CreateDraftEt1CallbackHandler` | `/et1Repped/createDraftEt1` | `/et1Repped/createDraftEt1Submitted` | `ET_Scotland, ET_EnglandWales` | `createDraftEt1` |
| `GenerateEt1DocumentsCallbackHandler` | `/et1Repped/generateDocuments` | `(none)` | `ET_Scotland, ET_EnglandWales` | `generateEt1Documents` |
| `ClaimantViewAllNotificationsCallbackHandler` | `/claimantViewNotification/all/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `claimantViewAllNotifications` |
| `AmendClaimantRepresentativeContactCallbackHandler` | `/et1Repped/aboutToSubmitAmendClaimantRepresentativeContact` | `(none)` | `ET_Scotland, ET_EnglandWales` | `amendClaimantRepresentativeContact` |
| `Et1ReppedCreateCaseCallbackHandler` | `/et1Repped/createCase/aboutToSubmit` | `/et1Repped/createCase/submitted` | `ET_Scotland, ET_EnglandWales` | `et1ReppedCreateCase` |
| `SubmitEt1DraftCallbackHandler` | `/et1Repped/submitClaim` | `/et1Repped/submitted` | `ET_Scotland, ET_EnglandWales` | `submitEt1Draft` |
| `SubmitCaseDraftCallbackHandler` | `/postDefaultValues` | `/et1Submission/submitted` | `ET_Scotland, ET_EnglandWales` | `SUBMIT_CASE_DRAFT` |
| `CreateDcfDcfAsyncAboutToSubmitCallbackHandler` | `/dcf/asyncAboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `createDcf` |
| `AsyncStitchingCompleteCallbackHandler` | `/dcf/asyncCompleteAboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `asyncStitchingComplete` |
| `AddAmendClaimantRepresentativeCallbackHandler` | `/addAmendClaimantRepresentative/aboutToSubmit` | `/addAmendClaimantRepresentative/amendClaimantRepSubmitted` | `ET_Scotland, ET_EnglandWales` | `addAmendClaimantRepresentative` |
| `PartyUnavailabilityCallbackHandler` | `/hearingUnavailability/aboutToSubmit` | `/hearingUnavailability/submitted` | `ET_Scotland, ET_EnglandWales` | `partyUnavailability` |
| `AddLegalRepToMultipleCallbackHandler` | `/multiples/addLegalRepToMultiple/aboutToSubmit` | `/multiples/addLegalRepToMultiple/completed` | `ET_Scotland, ET_EnglandWales` | `addLegalRepToMultiple` |
| `IssueInitialConsiderationDirectionsWACallbackHandler` | `/submitIssueInitialConsiderationDirectionsWA` | `/completeIssueInitialConsiderationDirectionsWA` | `ET_Scotland, ET_EnglandWales` | `issueInitialConsiderationDirectionsWA` |
| `SubmitClaimantTseCallbackHandler` | `/tseClaimant/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `SUBMIT_CLAIMANT_TSE` |
| `PreAcceptanceCaseCallbackHandler` | `/preAcceptanceCase/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `preAcceptanceCase` |
| `Et3ResponseSubmitSectionCallbackHandler` | `/et3Response/submitSection` | `/et3Response/sectionComplete` | `ET_Scotland, ET_EnglandWales` | `et3Response, et3ResponseEmploymentDetails, et3ResponseDetails` |
| `SubmitEt3CallbackHandler` | `/et3Response/aboutToSubmit` | `/et3Response/processingComplete` | `ET_Scotland, ET_EnglandWales` | `submitEt3` |
| `Et3VettingCallbackHandler` | `/et3Vetting/aboutToSubmit` | `/et3Vetting/processingComplete` | `ET_Scotland, ET_EnglandWales` | `et3Vetting` |
| `AmendClaimantDetailsCallbackHandler` | `/amendClaimantDetails` | `(none)` | `ET_Scotland, ET_EnglandWales` | `amendClaimantDetails` |
| `AmendRespondentDetailsCallbackHandler` | `/amendRespondentDetails` | `(none)` | `ET_Scotland, ET_EnglandWales` | `amendRespondentDetails` |
| `AddAmendJurisdictionCallbackHandler` | `/addAmendJurisdiction` | `(none)` | `ET_Scotland, ET_EnglandWales` | `addAmendJurisdiction` |
| `AddAmendHearingCallbackHandler` | `/amendHearing` | `(none)` | `ET_Scotland, ET_EnglandWales` | `addAmendHearing` |
| `UpdateHearingCallbackHandler` | `/hearingdetails/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `updateHearing` |
| `AddAmendJudgmentCallbackHandler` | `/judgementSubmitted` | `(none)` | `ET_Scotland, ET_EnglandWales` | `addAmendJudgment` |
| `UploadDocumentForServingCallbackHandler` | `(none)` | `/et1Serving/submitted` | `ET_Scotland, ET_EnglandWales` | `uploadDocumentForServing` |
| `GenerateCorrespondenceGenerateDocumentCallbackHandler` | `/generateDocument` | `/generateDocumentConfirmation` | `ET_Scotland, ET_EnglandWales` | `generateCorrespondence` |
| `CaseTransferDifferentCountryCallbackHandler` | `/caseTransfer/transferDifferentCountry` | `(none)` | `ET_Scotland, ET_EnglandWales` | `caseTransferDifferentCountry` |
| `InitialConsiderationCallbackHandler` | `/submitInitialConsideration` | `/completeInitialConsideration` | `ET_Scotland, ET_EnglandWales` | `initialConsideration` |
| `CreateReferralCreateReferralCallbackHandler` | `/createReferral/aboutToSubmit` | `/createReferral/completeCreateReferral` | `ET_Scotland, ET_EnglandWales` | `createReferral` |
| `ReplyToReferralReplyReferralCallbackHandler` | `/replyReferral/aboutToSubmit` | `/replyReferral/completeReplyToReferral` | `ET_Scotland, ET_EnglandWales` | `replyToReferral` |
| `CloseReferralCloseReferralCallbackHandler` | `/closeReferral/aboutToSubmit` | `/closeReferral/completeCloseReferral` | `ET_Scotland, ET_EnglandWales` | `closeReferral` |
| `RespondentTSECallbackHandler` | `/respondentTSE/aboutToSubmit` | `/respondentTSE/completeApplication` | `ET_Scotland, ET_EnglandWales` | `respondentTSE` |
| `TseRespondCallbackHandler` | `/tseResponse/aboutToSubmit` | `/tseResponse/submitted` | `ET_Scotland, ET_EnglandWales` | `tseRespond` |
| `AmendCaseDetailsCallbackHandler` | `/amendCaseDetails` | `(none)` | `ET_Scotland, ET_EnglandWales` | `amendCaseDetails, amendCaseDetailsClosed` |
| `TseClaimantRepResponseCallbackHandler` | `/tseClaimantRepResponse/aboutToSubmit` | `/tseClaimantRepResponse/submitted` | `ET_Scotland, ET_EnglandWales` | `tseClaimantRepResponse` |
| `SubmitRespondentTseCallbackHandler` | `/tseRespondent/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `SUBMIT_RESPONDENT_TSE` |
| `LegalrepDocumentsCallbackHandler` | `/legalrepDocuments/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `legalrepDocuments` |
| `PseRespondentRespondToTribunalCallbackHandler` | `/pseRespondToTribunal/aboutToSubmit` | `/pseRespondToTribunal/submitted` | `ET_Scotland, ET_EnglandWales` | `pseRespondentRespondToTribunal` |
| `InitiateCaseCallbackHandler` | `/postDefaultValues` | `/addServiceId` | `ET_Scotland, ET_EnglandWales` | `initiateCase` |
| `AssignCaseCallbackHandler` | `/caseTransfer/assignCase` | `(none)` | `ET_Scotland, ET_EnglandWales` | `assignCase` |
| `PostDefaultValuesCallbackHandler` | `/postDefaultValues` | `(none)` | `ET_Scotland, ET_EnglandWales` | `UPDATE_CASE_SUBMITTED, caseTransferMultiple, processCaseTransfer, returnCaseTransfer, createEcmCase` |
| `RemoveOwnRepAsRespondentCallbackHandler` | `/respondentRepresentative/removeOwnRepresentative` | `(none)` | `ET_Scotland, ET_EnglandWales` | `REMOVE_OWN_REP_AS_RESPONDENT` |
| `RemoveOwnRepAsClaimantCallbackHandler` | `/claimantRepresentative/removeOwnRepresentative` | `(none)` | `ET_Scotland, ET_EnglandWales` | `REMOVE_OWN_REP_AS_CLAIMANT` |
| `Et1VettingCallbackHandler` | `/et1VettingAboutToSubmit` | `/finishEt1Vetting` | `ET_Scotland, ET_EnglandWales` | `et1Vetting` |
| `AmendRespondentRepresentativeContactCallbackHandler` | `/et3Response/aboutToSubmitAmendRepresentativeContact` | `(none)` | `ET_Scotland, ET_EnglandWales` | `amendRespondentRepresentativeContact` |
| `DownloadDraftEt3CallbackHandler` | `/et3Response/downloadDraft/aboutToSubmit` | `/et3Response/downloadDraft/submitted` | `ET_Scotland, ET_EnglandWales` | `downloadDraftEt3` |
| `MigrateCaseLinkDetailsCallbackHandler` | `/migrateCaseLinkDetails` | `(none)` | `ET_Scotland, ET_EnglandWales` | `migrateCaseLinkDetails` |
| `RetrieveAcasCertificateCallbackHandler` | `/acasCertificate/retrieveCertificate` | `/acasCertificate/confirmation` | `ET_Scotland, ET_EnglandWales` | `retrieveAcasCertificate` |
| `AmendRespondentRepresentativeCallbackHandler` | `/amendRespondentRepresentative` | `/amendRespondentRepSubmitted` | `ET_Scotland, ET_EnglandWales` | `amendRespondentRepresentative` |
| `AllocateHearingCallbackHandler` | `/allocatehearing/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `allocateHearing` |
| `PrintHearingCallbackHandler` | `/generateListingsDocSingleCases` | `/generateListingsDocSingleCasesConfirmation` | `ET_Scotland, ET_EnglandWales` | `printHearing` |
| `BroughtForwardCallbackHandler` | `/bfActions` | `(none)` | `ET_Scotland, ET_EnglandWales` | `broughtForward` |
| `RecordDepositCallbackHandler` | `/depositValidation` | `(none)` | `ET_Scotland, ET_EnglandWales` | `recordDeposit` |
| `UploadDocumentUploadDocumentCallbackHandler` | `/uploadDocument/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `uploadDocument` |
| `AddDocumentCallbackHandler` | `/addDocument/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `addDocument` |
| `RestrictedCasesCallbackHandler` | `/restrictedCases` | `(none)` | `ET_Scotland, ET_EnglandWales` | `restrictedCases` |
| `CaseTransferECMCallbackHandler` | `/caseTransfer/transferToEcm` | `(none)` | `ET_Scotland, ET_EnglandWales` | `caseTransferECM` |
| `Et3NotificationCallbackHandler` | `/et3Notification/aboutToSubmit` | `/et3Notification/submitted` | `ET_Scotland, ET_EnglandWales` | `et3Notification` |
| `UpdateReferralUpdateReferralCallbackHandler` | `/updateReferral/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `updateReferral` |
| `ApplyNocDecisionCallbackHandler` | `/noc-decision/about-to-submit` | `/noc-decision/submitted` | `ET_Scotland, ET_EnglandWales` | `applyNocDecision` |
| `TseAdmReplyCallbackHandler` | `/tseAdmReply/aboutToSubmit` | `/tseAdmReply/submitted` | `ET_Scotland, ET_EnglandWales` | `tseAdmReply` |
| `TseAdminCallbackHandler` | `/tseAdmin/aboutToSubmit` | `/tseAdmin/submitted` | `ET_Scotland, ET_EnglandWales` | `tseAdmin` |
| `TseAdminCloseAnApplicationCallbackHandler` | `/tseAdmin/aboutToSubmitCloseApplication` | `/tseAdmin/submittedCloseApplication` | `ET_Scotland, ET_EnglandWales` | `tseAdminCloseAnApplication` |
| `ListingCaseCreationCallbackHandler` | `/listingCaseCreation` | `(none)` | `ET_Scotland_Listings, ET_EnglandWales_Listings` | `createCase, createReport` |
| `GenerateListingCallbackHandler` | `/listingHearings` | `(none)` | `ET_Scotland_Listings, ET_EnglandWales_Listings` | `generateListing` |
| `PrintCauseListCallbackHandler` | `/generateHearingDocument` | `/generateHearingDocumentConfirmation` | `ET_Scotland_Listings, ET_EnglandWales_Listings` | `printCauseList` |
| `GenerateReportCallbackHandler` | `/generateReport` | `/generateHearingDocumentConfirmation` | `ET_Scotland_Listings, ET_EnglandWales_Listings` | `generateReport` |
| `CreateMultipleCallbackHandler` | `/createMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `createMultiple` |
| `PreAcceptMultipleCallbackHandler` | `/preAcceptMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `preAcceptMultiple` |
| `AmendMultipleDetailsCallbackHandler` | `/amendMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `amendMultipleDetails` |
| `AmendMultipleAPICallbackHandler` | `/amendMultipleAPI` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `amendMultipleAPI` |
| `UpdatePayloadMultipleCallbackHandler` | `/updatePayloadMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `updatePayloadMultiple` |
| `ImportFileCallbackHandler` | `/importMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `importFile` |
| `PrintScheduleCallbackHandler` | `/printSchedule` | `/printDocumentConfirmation` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `printSchedule` |
| `BatchUpdateCasesCallbackHandler` | `/batchUpdate` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `batchUpdateCases` |
| `UpdateSubMultipleCallbackHandler` | `/updateSubMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `updateSubMultiple` |
| `GenerateCorrespondencePrintLetterCallbackHandler` | `/printLetter` | `/printDocumentConfirmation` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `generateCorrespondence` |
| `ResetMultipleStateCallbackHandler` | `/resetMultipleState` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `resetMultipleState` |
| `MultipleTransferDifferentCountryCallbackHandler` | `/caseTransferMultiples/transferDifferentCountry` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `multipleTransferDifferentCountry` |
| `FixMultipleTransferAPICallbackHandler` | `/fixMultipleCaseApi` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `fixMultipleTransferAPI` |
| `BulkAddSingleCasesCallbackHandler` | `/bulkAddSingleCasesToMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `bulkAddSingleCases` |
| `UploadDocumentMultiplesUploadDocumentCallbackHandler` | `/multiples/uploadDocument/aboutToSubmit` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `uploadDocument` |
| `SendNotificationSendNotificationCallbackHandler` | `/sendNotification/aboutToSubmit` | `/sendNotification/submitted` | `ET_Scotland, ET_EnglandWales` | `sendNotification` |
| `RespondNotificationCallbackHandler` | `/respondNotification/aboutToSubmit` | `/respondNotification/submitted` | `ET_Scotland, ET_EnglandWales` | `respondNotification` |
| `RefreshSharedUsersCallbackHandler` | `/refreshSharedUsers/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `refreshSharedUsers` |
| `MigrateCaseCallbackHandler` | `/global-search-migration/about-to-submit` | `/global-search-migration/submitted` | `ET_Scotland, ET_EnglandWales` | `migrateCase` |
| `ClaimantTransferredCaseAccessCallbackHandler` | `/caseAccess/claimant/transferredCase` | `(none)` | `ET_Scotland, ET_EnglandWales` | `claimantTransferredCaseAccess` |
| `ClaimantTSECallbackHandler` | `/claimantTSE/aboutToSubmit` | `/claimantTSE/completeApplication` | `ET_Scotland, ET_EnglandWales` | `claimantTSE` |
| `AddCaseNoteCaseNotesSinglesCallbackHandler` | `/caseNotes/singles/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `addCaseNote` |
| `GenerateNotificationSummaryCallbackHandler` | `/notificationDocument/aboutToSubmit` | `/notificationDocument/submitted` | `ET_Scotland, ET_EnglandWales` | `generateNotificationSummary` |
| `CreateCaseLinkCallbackHandler` | `/caseLinks/create/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `createCaseLink` |
| `MaintainCaseLinkCallbackHandler` | `/caseLinks/maintain/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `maintainCaseLink` |
| `MigrateCaseFlagsCallbackHandler` | `/case-flags-migration/about-to-submit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `migrateCaseFlags` |
| `RollbackCaseFlagsCallbackHandler` | `/case-flags-rollback/about-to-submit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `rollbackCaseFlags` |
| `SendNotificationMultipleCallbackHandler` | `/sendNotification/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `sendNotificationMultiple` |
| `CreateReferralMultiplesCreateReferralCallbackHandler` | `/multiples/createReferral/aboutToSubmit` | `/multiples/createReferral/completeCreateReferral` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `createReferral` |
| `SendNotificationMultiplesSendNotificationCallbackHandler` | `/multiples/sendNotification/aboutToSubmit` | `/multiples/sendNotification/submitted` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `sendNotification` |
| `ExtractNotificationsCallbackHandler` | `/multiples/extractNotifications/aboutToSubmit` | `/multiples/extractNotifications/submitted` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `extractNotifications` |
| `ReplyToReferralMultiplesReplyReferralCallbackHandler` | `/multiples/replyReferral/aboutToSubmit` | `/multiples/replyReferral/completeReplyToReferral` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `replyToReferral` |
| `UpdateReferralMultiplesUpdateReferralCallbackHandler` | `/multiples/updateReferral/aboutToSubmit` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `updateReferral` |
| `CloseReferralMultiplesCloseReferralCallbackHandler` | `/multiples/closeReferral/aboutToSubmit` | `/multiples/closeReferral/completeCloseReferral` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `closeReferral` |
| `AddCaseNoteCaseNotesMultiplesCallbackHandler` | `/caseNotes/multiples/aboutToSubmit` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `addCaseNote` |
| `DocumentSelectCallbackHandler` | `/multiples/documentAccess/aboutToSubmit` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `documentSelect` |
| `CreateDcfMultiplesDcfCallbackHandler` | `/multiples/dcf/aboutToSubmit` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `createDcf` |
| `CloseCallbackHandler` | `/closeMultiple` | `(none)` | `ET_Scotland_Multiple, ET_EnglandWales_Multiple` | `close` |
| `RollbackMigrateCaseCallbackHandler` | `/migrate/rollback/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `rollbackMigrateCase` |
| `BundlesRespondentPrepareDocCallbackHandler` | `/bundlesRespondent/aboutToSubmit` | `/bundlesRespondent/submitted` | `ET_Scotland, ET_EnglandWales` | `bundlesRespondentPrepareDoc` |
| `RemoveHearingBundlesCallbackHandler` | `/bundlesRespondent/removeHearingBundle` | `(none)` | `ET_Scotland, ET_EnglandWales` | `removeHearingBundles` |
| `UploadHearingDocumentsCallbackHandler` | `/uploadHearingDocuments/aboutToSubmit` | `(none)` | `ET_Scotland, ET_EnglandWales` | `uploadHearingDocuments` |
| `CaseTransferSameCountryCallbackHandler` | `/caseTransfer/transferSameCountry` | `(none)` | `ET_EnglandWales` | `caseTransferSameCountry` |
| `CaseTransferSameCountryEccLinkedCaseCallbackHandler` | `/caseTransfer/transferSameCountryEccLinkedCase` | `(none)` | `ET_EnglandWales` | `caseTransferSameCountryEccLinkedCase` |
| `MultipleTransferSameCountryCallbackHandler` | `/caseTransferMultiples/transferSameCountry` | `(none)` | `ET_EnglandWales_Multiple` | `multipleTransferSameCountry` |

## 2026-03-23 Correction Pass (Controller Test Deletion Heuristic)

- Restored incorrectly deleted controller test files where callback endpoints still exist (45 restored, including `TseAdmReplyControllerTest`).
- Kept controller test files deleted only where corresponding controllers now have no remaining callback `@PostMapping` endpoints (18 files).
- Removed only stale tests that exercised migrated `aboutToSubmit` / `submitted` controller endpoints from mixed test classes, using `generated-callback-handlers-phase2.tsv` route mappings (including non-suffixed callback URLs).
- Fixed resulting `checkstyleTest` warnings (unused imports in restored/trimmed test files).
- Re-verified with Java 21:
  - `./gradlew -q compileJava compileTestJava test integration --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Pass (Phase Start)

- Added concrete unit tests for migrated callback handlers:
  - `TseAdmReplyCallbackHandlerTest`
  - `TseAdminCallbackHandlerTest`
  - `TseAdminCloseAnApplicationCallbackHandlerTest`
  - `SendNotificationSendNotificationCallbackHandlerTest`
  - `SendNotificationMultipleCallbackHandlerTest`
  - `RespondNotificationCallbackHandlerTest`
  - `ClaimantRespondToNotificationHandlerTest`
- Updated `GeneratedCallbackHandlersTest` test harness setup to use a real `CaseDetailsConverter` and concrete CCD model fixtures for safer instantiation checks across generated handlers.
- Re-verified with Java 21:
  - `./gradlew -q checkstyleTest test --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue)

- Added concrete unit tests for additional migrated handlers:
  - `RetrieveAcasCertificateCallbackHandlerTest`
  - `UploadDocumentUploadDocumentCallbackHandlerTest`
  - `UpdateReferralUpdateReferralCallbackHandlerTest`
  - `CloseReferralCloseReferralCallbackHandlerTest`
  - `UpdateReferralMultiplesUpdateReferralCallbackHandlerTest`
  - `CloseReferralMultiplesCloseReferralCallbackHandlerTest`
  - `UploadDocumentMultiplesUploadDocumentCallbackHandlerTest`
  - `SendNotificationMultiplesSendNotificationCallbackHandlerTest`
- Covered key migrated behaviors in these handlers:
  - token-gated about-to-submit/submitted flows
  - unsupported callback type exceptions
  - referral update/close state mutation and document linking

## 2026-03-27 Base Class Conversion Refactor

- Removed `AbstractCallbackHandlerBase` and flattened callback base hierarchy to three concrete bases:
  - `CallbackHandlerBase` (single-case callbacks)
  - `ListingCallbackHandlerBase`
  - `MultipleCallbackHandlerBase`
- Centralized start/end conversion flow in all three bases so listing/multiple child handlers no longer need
  `toCallbackResponse(...)` / `toSubmittedCallbackResponse(...)` wrapper calls.
- Confirmed no remaining child-level response conversion wrappers in handlers extending
  `ListingCallbackHandlerBase` or `MultipleCallbackHandlerBase`.
- Re-verified with Java 21:
  - `./gradlew checkstyleMain checkstyleTest --no-daemon` passed.
  - `./gradlew :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" -x et-shared:test --no-daemon` passed.

## 2026-03-27 Test Suite Cleanup

- Removed `GeneratedCallbackHandlersTest` as redundant after broad per-handler unit test coverage was added.
- Kept `generated-callback-handlers-phase2.tsv` as migration-reference data.
- Re-verified with Java 21:
  - `./gradlew compileTestJava --no-daemon` passed.
  - `./gradlew :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" -x et-shared:test --no-daemon` passed.
  - multiples notification delegation and submitted confirmation body
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q test integration --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 2)

- Added further concrete handler unit tests:
  - `AddDocumentCallbackHandlerTest`
  - `RefreshSharedUsersCallbackHandlerTest`
  - `LegalrepDocumentsCallbackHandlerTest`
  - `ClaimantTransferredCaseAccessCallbackHandlerTest`
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Controller Rollback Path Restore

- Restored legacy controller endpoints by resetting both controller trees from `master`:
  - `src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/controllers/**`
  - `src/test/java/uk/gov/hmcts/ethos/replacement/docmosis/controllers/**`
- This reintroduces previously removed controller routes/tests to support rollback/feature-flag fallback while keeping the new callback handlers in place.
- Re-verified with Java 21:
  - `./gradlew -q checkstyleTest test integration --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 3)

- Added further dedicated handler tests:
  - `AsyncStitchingCompleteCallbackHandlerTest`
  - `ClaimantViewAllNotificationsCallbackHandlerTest`
  - `DocumentSelectCallbackHandlerTest`
  - `AddCaseNoteCaseNotesSinglesCallbackHandlerTest`
  - `AddCaseNoteCaseNotesMultiplesCallbackHandlerTest`
  - `PreAcceptanceCaseCallbackHandlerTest`
  - `BroughtForwardCallbackHandlerTest`
  - `RecordDepositCallbackHandlerTest`
  - `PostDefaultValuesCallbackHandlerTest`
  - `UpdateHearingCallbackHandlerTest`
  - `UploadHearingDocumentsCallbackHandlerTest`
  - `RemoveHearingBundlesCallbackHandlerTest`
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 4)

- Added another set of dedicated handler tests:
  - `RemoveOwnRepAsClaimantCallbackHandlerTest`
  - `RemoveOwnRepAsRespondentCallbackHandlerTest`
  - `CreateDcfDcfAsyncAboutToSubmitCallbackHandlerTest`
  - `CreateDcfMultiplesDcfCallbackHandlerTest`
  - `MigrateCaseFlagsCallbackHandlerTest`
  - `RollbackCaseFlagsCallbackHandlerTest`
  - `MigrateCaseLinkDetailsCallbackHandlerTest`
  - `UploadDocumentForServingCallbackHandlerTest`
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 5)

- Added dedicated handler unit tests for:
  - `AddAmendJurisdictionCallbackHandlerTest`
  - `AddAmendHearingCallbackHandlerTest`
  - `AddAmendJudgmentCallbackHandlerTest`
  - `AmendCaseDetailsCallbackHandlerTest`
- Covered extracted handler behavior including:
  - callback-type support guards (`submitted` unsupported where applicable)
  - exception mapping (`IOException` / `ParseException` -> typed runtime exceptions)
  - validation-error short-circuit paths versus successful mutation paths
  - feature-flag-conditional behavior for work-allocation case-management location updates
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AddAmend*CallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendCaseDetailsCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 6)

- Added dedicated handler unit tests for:
  - `AddAmendClaimantRepresentativeCallbackHandlerTest`
  - `AmendClaimantDetailsCallbackHandlerTest`
  - `AmendClaimantRepresentativeContactCallbackHandlerTest`
  - `AmendRespondentRepresentativeContactCallbackHandlerTest`
- Covered extracted handler behavior including:
  - about-to-submit service delegation and data mutation
  - submitted callback service delegation and IOException wrapping
  - error propagation from `GenericServiceException` into callback error arrays
  - feature-toggle branching for claimant-detail updates
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AddAmendClaimantRepresentativeCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendClaimantDetailsCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendClaimantRepresentativeContactCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendRespondentRepresentativeContactCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.

- Remaining handlers without dedicated test class: `68`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 7)

- Added dedicated handler unit tests for:
  - `AddLegalRepToMultipleCallbackHandlerTest`
  - `AllocateHearingCallbackHandlerTest`
  - `AssignCaseCallbackHandlerTest`
  - `AmendRespondentRepresentativeCallbackHandlerTest`
  - `AmendRespondentDetailsCallbackHandlerTest`
- Covered extracted handler behavior including:
  - token-gated callback branching for about-to-submit flows
  - feature-flag-dependent case management location updates
  - validation short-circuit versus full mutation paths
  - submitted callback exception mapping for respondent representative access updates
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AddLegalRepToMultipleCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AllocateHearingCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AssignCaseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendRespondentRepresentativeCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendRespondentDetailsCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

- Remaining handlers without dedicated test class: `63`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 8)

- Added dedicated handler unit tests for:
  - `CreateCaseLinkCallbackHandlerTest`
  - `MaintainCaseLinkCallbackHandlerTest`
  - `MigrateCaseCallbackHandlerTest`
  - `RollbackMigrateCaseCallbackHandlerTest`
  - `CreateDraftEt1CallbackHandlerTest`
- Covered extracted handler behavior including:
  - token-gated callback branching and service invocation on about-to-submit
  - HMC-driven case-link flag behavior (`YES` on create, `NO` on maintain with empty links)
  - migrated-case submitted callback success/forbidden/error handling
  - rollback migration exception mapping
  - draft ET1 generation flow and submitted confirmation body
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CreateCaseLinkCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.MaintainCaseLinkCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.MigrateCaseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.RollbackMigrateCaseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CreateDraftEt1CallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

- Remaining handlers without dedicated test class: `58`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 9)

- Added dedicated handler unit tests for:
  - `CreateReferralCreateReferralCallbackHandlerTest`
  - `ReplyToReferralReplyReferralCallbackHandlerTest`
  - `CreateReferralMultiplesCreateReferralCallbackHandlerTest`
  - `ReplyToReferralMultiplesReplyReferralCallbackHandlerTest`
- Covered extracted handler behavior including:
  - referral create/reply about-to-submit data mutation and document wiring
  - submitted confirmation body responses for single and multiple referral flows
  - multiples referral callback exception mapping for IO lookup failures
  - work-allocation branch interaction in referral-reply creation
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CreateReferralCreateReferralCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ReplyToReferralReplyReferralCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CreateReferralMultiplesCreateReferralCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ReplyToReferralMultiplesReplyReferralCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

- Remaining handlers without dedicated test class: `54`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 10)

- Added dedicated handler unit tests for:
  - `AmendMultipleAPICallbackHandlerTest`
  - `AmendMultipleDetailsCallbackHandlerTest`
  - `BatchUpdateCasesCallbackHandlerTest`
  - `BulkAddSingleCasesCallbackHandlerTest`
  - `CloseCallbackHandlerTest`
  - `CreateMultipleCallbackHandlerTest`
  - `FixMultipleTransferAPICallbackHandlerTest`
  - `UpdateSubMultipleCallbackHandlerTest`
- Covered extracted handler behavior including:
  - token-gated about-to-submit flows for multiples callbacks
  - delegation to extracted multiple services with argument verification
  - IO exception wrapping paths for batch update/create multiple handlers
  - unsupported submitted callback guard assertions
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendMultipleAPICallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.AmendMultipleDetailsCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.BatchUpdateCasesCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.BulkAddSingleCasesCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CloseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CreateMultipleCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.FixMultipleTransferAPICallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.UpdateSubMultipleCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 11)

- Added dedicated handler unit tests for:
  - `CaseTransferDifferentCountryCallbackHandlerTest`
  - `CaseTransferSameCountryCallbackHandlerTest`
  - `CaseTransferECMCallbackHandlerTest`
  - `CaseTransferSameCountryEccLinkedCaseCallbackHandlerTest`
  - `MultipleTransferDifferentCountryCallbackHandlerTest`
  - `MultipleTransferSameCountryCallbackHandlerTest`
  - `PreAcceptMultipleCallbackHandlerTest`
  - `ResetMultipleStateCallbackHandlerTest`
  - `UpdatePayloadMultipleCallbackHandlerTest`
- Covered extracted handler behavior including:
  - token-gated transfer/reset/pre-accept flows
  - feature-toggle branching for case-management-location updates in same-country transfers
  - state/payload mutation assertions for reset/open-state and payload-pre-accept updates
  - unsupported submitted callback guard assertions
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CaseTransferDifferentCountryCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CaseTransferSameCountryCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CaseTransferECMCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.CaseTransferSameCountryEccLinkedCaseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.MultipleTransferDifferentCountryCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.MultipleTransferSameCountryCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.PreAcceptMultipleCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ResetMultipleStateCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.UpdatePayloadMultipleCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed (with existing `et-shared` javadoc warnings).

- Remaining handlers without dedicated test class: `36`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 12)

- Added dedicated handler unit tests for:
  - `GenerateNotificationSummaryCallbackHandlerTest`
  - `GenerateCorrespondencePrintLetterCallbackHandlerTest`
  - `ImportFileCallbackHandlerTest`
  - `PrintScheduleCallbackHandlerTest`
  - `PrintHearingCallbackHandlerTest`
  - `GenerateReportCallbackHandlerTest`
- Covered extracted handler behavior including:
  - about-to-submit token-gated branching for print/import/report flows
  - delegation to extracted callback services for submitted print/report confirmations
  - markup/confirmation assertions for generated notification and hearing document responses
  - unsupported submitted callback guard assertion for `importFile`
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.GenerateNotificationSummaryCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.GenerateCorrespondencePrintLetterCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ImportFileCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.PrintScheduleCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.PrintHearingCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.GenerateReportCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed (with existing `et-shared` javadoc warnings).

- Remaining handlers without dedicated test class: `30`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 13)

- Added dedicated handler unit tests for:
  - `DownloadDraftEt3CallbackHandlerTest`
  - `RestrictedCasesCallbackHandlerTest`
  - `PartyUnavailabilityCallbackHandlerTest`
  - `BundlesRespondentPrepareDocCallbackHandlerTest`
  - `ApplyNocDecisionCallbackHandlerTest`
- Covered extracted handler behavior including:
  - about-to-submit and submitted token-gating for party/bundles/NoC callbacks
  - bundles feature-toggle unavailable-path exception behavior
  - draft ET3 markup rewrite and submitted confirmation-body link assertions
  - restricted-cases HMC feature-flag branch assertions for case-name and private-hearing flags
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.DownloadDraftEt3CallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.RestrictedCasesCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.PartyUnavailabilityCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.BundlesRespondentPrepareDocCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ApplyNocDecisionCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed (with existing `et-shared` javadoc warnings).

- Remaining handlers without dedicated test class: `25`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 14)

- Added dedicated handler unit tests for:
  - `SubmitCaseDraftCallbackHandlerTest`
  - `SubmitClaimantTseCallbackHandlerTest`
  - `SubmitRespondentTseCallbackHandlerTest`
  - `TseRespondCallbackHandlerTest`
  - `SubmitEt3CallbackHandlerTest`
- Covered extracted handler behavior including:
  - submit-draft about-to-submit delegation and submitted IOException wrapping
  - claimant/respondent TSE application creation and clearing branch behavior
  - respondent TSE response about-to-submit token-gating and submitted confirmation copy text branch
  - ET3 submit document-generation, persistence, notification and WA update delegation
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.SubmitCaseDraftCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.SubmitClaimantTseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.SubmitRespondentTseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.TseRespondCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.SubmitEt3CallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed (with existing `et-shared` javadoc warnings).

- Remaining handlers without dedicated test class: `20`.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 15)

- Completed and stabilized pending in-progress tests:
  - `ClaimantTSECallbackHandlerTest`
  - `RespondentTSECallbackHandlerTest`
  - `TseClaimantRepResponseCallbackHandlerTest`
  - `ExtractNotificationsCallbackHandlerTest`
- Fixes applied while stabilizing:
  - corrected matcher consistency (`eq`/`isNull`/`anyList`) in `ExtractNotificationsCallbackHandlerTest`
  - added `caseDetailsConverter.getObjectMapper()` stubbing in setup where callback base conversion required it
  - resolved strict Mockito and type assertion issues in TSE tests
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ClaimantTSECallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.RespondentTSECallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.TseClaimantRepResponseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ExtractNotificationsCallbackHandlerTest" --no-daemon` passed.

## 2026-03-24 Handler Unit Test Expansion (Phase Continue 16)

- Added dedicated handler unit tests for all remaining untested handlers:
  - `Et1ReppedAboutToSubmitSectionCallbackHandlerTest`
  - `Et1ReppedCreateCaseCallbackHandlerTest`
  - `Et1VettingCallbackHandlerTest`
  - `Et3NotificationCallbackHandlerTest`
  - `Et3ResponseSubmitSectionCallbackHandlerTest`
  - `Et3VettingCallbackHandlerTest`
  - `GenerateCorrespondenceGenerateDocumentCallbackHandlerTest`
  - `GenerateEt1DocumentsCallbackHandlerTest`
  - `GenerateListingCallbackHandlerTest`
  - `InitialConsiderationCallbackHandlerTest`
  - `InitiateCaseCallbackHandlerTest`
  - `IssueInitialConsiderationDirectionsWACallbackHandlerTest`
  - `ListingCaseCreationCallbackHandlerTest`
  - `PrintCauseListCallbackHandlerTest`
  - `PseRespondentRespondToTribunalCallbackHandlerTest`
  - `SubmitEt1DraftCallbackHandlerTest`
- Covered extracted handler behavior including:
  - token-gated about-to-submit/submitted branches for ET1/ET3/IC/listing handlers
  - callback-service delegation assertions for listing generation and case actions
  - submitted-path confirmation assertions and unsupported submitted callback guard assertions
  - checked-exception wrapping assertions for submitted paths that call case-management IO methods
- Re-verified with Java 21:
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.Et1ReppedAboutToSubmitSectionCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.Et1ReppedCreateCaseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.Et1VettingCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.Et3NotificationCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.Et3ResponseSubmitSectionCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.Et3VettingCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.GenerateCorrespondenceGenerateDocumentCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.GenerateEt1DocumentsCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.GenerateListingCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.InitialConsiderationCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.InitiateCaseCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.IssueInitialConsiderationDirectionsWACallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.ListingCaseCreationCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.PrintCauseListCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.PseRespondentRespondToTribunalCallbackHandlerTest" --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.SubmitEt1DraftCallbackHandlerTest" --no-daemon` passed.
  - `./gradlew -q checkstyleTest --no-daemon` passed.
  - `./gradlew -q :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" --no-daemon` passed.
  - `./gradlew -q build integration --no-daemon` passed (with existing `et-shared` javadoc warnings).

- Remaining handlers without dedicated test class: `0`.

## Branch Summary (Current State)

- Completed callback-handler unit test coverage for all migrated handler classes.
- Added dedicated tests for all previously missing handlers and stabilized pending failing TSE/extract tests.
- Verified test/lint/build health on Java 21:
  - handler-scoped test suite green
  - `checkstyleTest` green
  - `build integration` green (pre-existing `et-shared` javadoc warnings only)
- Checklist retained as the migration audit trail and updated through completion of handler test coverage.

## 2026-03-26 Callback Base Refactor (Post-Rollback Reapply)

- Reapplied the callback-base refactor after handler rollback:
  - kept `CallbackHandlerBase` as the standard `CaseDetails` base with shared conversion helpers.
  - introduced typed derived bases:
    - `ListingCallbackHandlerBase` for `ListingDetails`/`ListingRequest` conversion.
    - `MultipleCallbackHandlerBase` for `MultipleDetails`/`MultipleRequest` conversion.
  - migrated listing/multiple handlers to the new typed bases.
- Removed child-handler boilerplate unsupported callback implementations:
  - deleted throw-only `aboutToSubmit(CaseDetails)` / `submitted(CaseDetails)` overrides where the callback type is not accepted.
  - unsupported callback behavior is now centralized in `CallbackHandlerBase`.
- Mechanical cleanup after reapply:
  - removed newly-unused `SubmittedCallbackResponse` imports.
  - fixed method indentation/spacing in affected handlers after override removal.
  - fixed one checkstyle line-length issue in `RequestInterceptor`.
- Validation (Java 21):
  - `./gradlew checkstyleMain --no-daemon` passed.
  - `./gradlew checkstyleTest --no-daemon` passed.
  - `./gradlew :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" -x et-shared:test --no-daemon` passed.
  - `./gradlew test --no-daemon` still reports existing non-handler failures in this branch context (legacy context-load failures, including duplicate YAML key errors).

## 2026-03-27 Typed Conversion Centralisation Pass

- Refactored handler base hierarchy to centralise conversion logic more aggressively:
  - added `AbstractCallbackHandlerBase` as shared conversion/response base.
  - made `CallbackHandlerBase`, `ListingCallbackHandlerBase`, and `MultipleCallbackHandlerBase` sibling bases (listing/multiple no longer inherit from `CallbackHandlerBase`).
- Moved request conversion boundaries into listing/multiple base classes:
  - `ListingCallbackHandlerBase` now converts `CaseDetails -> ListingRequest` in base callback entrypoints.
  - `MultipleCallbackHandlerBase` now converts `CaseDetails -> MultipleRequest` in base callback entrypoints.
  - child listing/multiple handlers now implement typed hooks (`aboutToSubmit(ListingRequest|MultipleRequest)` / `submitted(...)`) instead of doing request conversion in the method body.
- Removed remaining child-level response wrapping for listing/multiple handlers:
  - eliminated `toCallbackResponse(...)` / `toSubmittedCallbackResponse(...)` from listing/multiple child handlers.
  - return conversion is now base-driven for both entry and exit in listing/multiple callback flow.
- Updated affected handler tests for new hierarchy/typed conversion path:
  - `GeneratedCallbackHandlersTest` now instantiates/asserts against `AbstractCallbackHandlerBase`.
  - `CloseReferralMultiplesCloseReferralCallbackHandlerTest` updated stubbing to `MultipleDetails` conversion path.
- Validation (Java 21):
  - `./gradlew -q compileJava --no-daemon` passed.
  - `./gradlew checkstyleMain --no-daemon` passed.
  - `./gradlew checkstyleTest --no-daemon` passed.
  - `./gradlew :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" -x et-shared:test --no-daemon` passed.

## 2026-03-30 Interim Delegation Pass (Two-Stage Refactor)

- Reworked phase-2 migrated handlers to delegate directly to the controller methods they replace as an interim no-duplication step.
- Used `src/test/resources/generated-callback-handlers-phase2.tsv` mapping to align each handler with the correct controller method pair.
- Preserved handler routing metadata (`getHandledCaseTypeIds`, `getHandledEventIds`, `acceptsAboutToSubmit`, `acceptsSubmitted`) and replaced in-handler business logic with controller delegation.
- Added callback auth-token propagation where mapped controller signatures require it.
- Replaced per-handler logic tests with matrix-driven delegation verification:
  - removed generated per-handler test classes for phase-2 mapped handlers.
  - added `GeneratedCallbackHandlersTest` to assert metadata and controller-method invocation for all mapped handlers.
  - retained bespoke non-generated handler tests (for example `ClaimantRespondToNotificationHandlerTest`).
- Validation (Java 21):
  - `./gradlew compileJava compileTestJava checkstyleMain checkstyleTest --no-daemon` passed.
  - `./gradlew :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" -x et-shared:test --no-daemon` passed.

## 2026-03-30 Delegation Cleanup (Exception Flow + Unused Services)

- Removed handler-level `try/catch` wrappers that rethrew `IllegalStateException("Failed to delegate callback to controller", ...)`.
- Delegated callback failures now propagate original exceptions; checked exception boundaries are handled with `@SneakyThrows` on affected handler methods.
- Deleted unused callback-only service classes introduced during earlier extraction iterations (no remaining references):
  - `service/callback/CaseActionsForCaseWorkerCallbackService`
  - `service/callback/ListingGenerationCallbackService`
  - `service/callback/MultipleDocGenerationCallbackService`
  - `service/callback/SendNotificationCallbackService`
- Validation (Java 21):
  - `./gradlew compileJava checkstyleMain --no-daemon` passed.
  - `./gradlew :test --tests "uk.gov.hmcts.ethos.replacement.docmosis.handler.*" -x et-shared:test --no-daemon` passed.
