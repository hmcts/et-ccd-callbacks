# et-shared

`et-shared` consolidates code from:
- `et-common`
- `ecm-common`
- `et-data-model`
- `ecm-data-model`

## Package strategy

The default package paths remain unchanged for backward compatibility, for example:
- `uk.gov.hmcts.et.common...`
- `uk.gov.hmcts.ecm.common...`

For classes where `et-common` and `ecm-common` had different implementations, the ECM implementation is also provided under a dedicated compatibility namespace:
- Old namespace: `uk.gov.hmcts.ecm.common...`
- ECM compatibility namespace in `et-shared`: `uk.gov.hmcts.ecm.compat.common...`

This means consumers that require the old ECM-specific behavior can switch imports to `uk.gov.hmcts.ecm.compat.common...` on a class-by-class basis.

## Upgrade path

### Standard migration (no behavior-sensitive ECM usage)

1. Remove old library dependencies (`et-common`, `ecm-common`, `et-data-model`, `ecm-data-model`).
2. Add `et-shared`.
3. Build and run tests.

### Migration for repositories that rely on older ECM implementations

For repositories that currently consume `ecm-common` and depend on ECM-specific behavior:

1. Replace old libraries with `et-shared`.
2. Update imports/usages for affected classes from:
   - `uk.gov.hmcts.ecm.common.<...ClassName...>`
   to:
   - `uk.gov.hmcts.ecm.compat.common.<...ClassName...>`
3. Build and run tests.

Known repositories in this workspace with runtime usage of affected classes:
- `ecm-consumer`
- `et-message-handler`
- `ethos-repl-docmosis-service`

## Affected classes (implementation differences between `et-common` and `ecm-common`)

The following classes had different implementations and may require import updates to the ECM compatibility namespace.

- `uk.gov.hmcts.ecm.common.client.CaseDataBuilder` -> `uk.gov.hmcts.ecm.compat.common.client.CaseDataBuilder`
- `uk.gov.hmcts.ecm.common.client.CcdClient` -> `uk.gov.hmcts.ecm.compat.common.client.CcdClient`
- `uk.gov.hmcts.ecm.common.client.CcdClientConfig` -> `uk.gov.hmcts.ecm.compat.common.client.CcdClientConfig`
- `uk.gov.hmcts.ecm.common.helpers.CreateUpdatesHelper` -> `uk.gov.hmcts.ecm.compat.common.helpers.CreateUpdatesHelper`
- `uk.gov.hmcts.ecm.common.helpers.ESHelper` -> `uk.gov.hmcts.ecm.compat.common.helpers.ESHelper`
- `uk.gov.hmcts.ecm.common.helpers.Partition` -> `uk.gov.hmcts.ecm.compat.common.helpers.Partition`
- `uk.gov.hmcts.ecm.common.helpers.UtilHelper` -> `uk.gov.hmcts.ecm.compat.common.helpers.UtilHelper`
- `uk.gov.hmcts.ecm.common.idam.models.UserDetails` -> `uk.gov.hmcts.ecm.compat.common.idam.models.UserDetails`
- `uk.gov.hmcts.ecm.common.launchdarkly.FeatureToggleApi` -> `uk.gov.hmcts.ecm.compat.common.launchdarkly.FeatureToggleApi`
- `uk.gov.hmcts.ecm.common.launchdarkly.LaunchDarklyConfiguration` -> `uk.gov.hmcts.ecm.compat.common.launchdarkly.LaunchDarklyConfiguration`
- `uk.gov.hmcts.ecm.common.model.helper.BulkCasesPayload` -> `uk.gov.hmcts.ecm.compat.common.model.helper.BulkCasesPayload`
- `uk.gov.hmcts.ecm.common.model.helper.BulkRequestPayload` -> `uk.gov.hmcts.ecm.compat.common.model.helper.BulkRequestPayload`
- `uk.gov.hmcts.ecm.common.model.helper.Constants` -> `uk.gov.hmcts.ecm.compat.common.model.helper.Constants`
- `uk.gov.hmcts.ecm.common.model.helper.ScheduleConstants` -> `uk.gov.hmcts.ecm.compat.common.model.helper.ScheduleConstants`
- `uk.gov.hmcts.ecm.common.model.helper.SchedulePayload` -> `uk.gov.hmcts.ecm.compat.common.model.helper.SchedulePayload`
- `uk.gov.hmcts.ecm.common.model.labels.LabelPayloadES` -> `uk.gov.hmcts.ecm.compat.common.model.labels.LabelPayloadES`
- `uk.gov.hmcts.ecm.common.model.labels.LabelPayloadEvent` -> `uk.gov.hmcts.ecm.compat.common.model.labels.LabelPayloadEvent`
- `uk.gov.hmcts.ecm.common.model.reference.ReferenceCallbackResponse` -> `uk.gov.hmcts.ecm.compat.common.model.reference.ReferenceCallbackResponse`
- `uk.gov.hmcts.ecm.common.model.reference.ReferenceDetails` -> `uk.gov.hmcts.ecm.compat.common.model.reference.ReferenceDetails`
- `uk.gov.hmcts.ecm.common.model.reference.ReferenceRequest` -> `uk.gov.hmcts.ecm.compat.common.model.reference.ReferenceRequest`
- `uk.gov.hmcts.ecm.common.model.reference.ReferenceSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reference.ReferenceSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.reference.types.JudgeType` -> `uk.gov.hmcts.ecm.compat.common.model.reference.types.JudgeType`
- `uk.gov.hmcts.ecm.common.model.reference.types.VenueType` -> `uk.gov.hmcts.ecm.compat.common.model.reference.types.VenueType`
- `uk.gov.hmcts.ecm.common.model.reports.casesawaitingjudgment.CaseData` -> `uk.gov.hmcts.ecm.compat.common.model.reports.casesawaitingjudgment.CaseData`
- `uk.gov.hmcts.ecm.common.model.reports.casesawaitingjudgment.CasesAwaitingJudgmentSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reports.casesawaitingjudgment.CasesAwaitingJudgmentSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueCaseData` -> `uk.gov.hmcts.ecm.compat.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueCaseData`
- `uk.gov.hmcts.ecm.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportCaseData` -> `uk.gov.hmcts.ecm.compat.common.model.reports.eccreport.EccReportCaseData`
- `uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reports.eccreport.EccReportSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeCaseData` -> `uk.gov.hmcts.ecm.compat.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeCaseData`
- `uk.gov.hmcts.ecm.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeSearchResult` -> `uk.gov.hmcts.ecm.compat.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeSearchResult`
- `uk.gov.hmcts.ecm.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.reports.hearingstojudgments.HearingsToJudgmentsCaseData` -> `uk.gov.hmcts.ecm.compat.common.model.reports.hearingstojudgments.HearingsToJudgmentsCaseData`
- `uk.gov.hmcts.ecm.common.model.reports.hearingstojudgments.HearingsToJudgmentsSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reports.hearingstojudgments.HearingsToJudgmentsSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportCaseData` -> `uk.gov.hmcts.ecm.compat.common.model.reports.respondentsreport.RespondentsReportCaseData`
- `uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reports.respondentsreport.RespondentsReportSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysCaseData` -> `uk.gov.hmcts.ecm.compat.common.model.reports.sessiondays.SessionDaysCaseData`
- `uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysSearchResult` -> `uk.gov.hmcts.ecm.compat.common.model.reports.sessiondays.SessionDaysSearchResult`
- `uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysSubmitEvent` -> `uk.gov.hmcts.ecm.compat.common.model.reports.sessiondays.SessionDaysSubmitEvent`
- `uk.gov.hmcts.ecm.common.model.schedule.SchedulePayloadES` -> `uk.gov.hmcts.ecm.compat.common.model.schedule.SchedulePayloadES`
- `uk.gov.hmcts.ecm.common.model.schedule.SchedulePayloadEvent` -> `uk.gov.hmcts.ecm.compat.common.model.schedule.SchedulePayloadEvent`
- `uk.gov.hmcts.ecm.common.model.schedule.types.ScheduleClaimantIndType` -> `uk.gov.hmcts.ecm.compat.common.model.schedule.types.ScheduleClaimantIndType`
- `uk.gov.hmcts.ecm.common.model.schedule.types.ScheduleClaimantType` -> `uk.gov.hmcts.ecm.compat.common.model.schedule.types.ScheduleClaimantType`
- `uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.CreateUpdatesDto`
- `uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.CreateUpdatesMsg`
- `uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.UpdateCaseMsg`
- `uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.datamodel.CloseDataModel`
- `uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.datamodel.CreationSingleDataModel`
- `uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelFactory` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.datamodel.DataModelFactory`
- `uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.datamodel.DataModelParent`
- `uk.gov.hmcts.ecm.common.model.servicebus.datamodel.UpdateDataModel` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.datamodel.UpdateDataModel`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.CaseJudgementUpdate` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.CaseJudgementUpdate`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.CloseDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.CloseDataTask`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.CreationDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.CreationDataTask`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.CreationSingleDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.CreationSingleDataTask`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.DataTaskParent` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.DataTaskParent`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.DetachDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.DetachDataTask`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.PreAcceptDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.PreAcceptDataTask`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.RejectDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.RejectDataTask`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.ResetStateDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.ResetStateDataTask`
- `uk.gov.hmcts.ecm.common.model.servicebus.tasks.UpdateDataTask` -> `uk.gov.hmcts.ecm.compat.common.model.servicebus.tasks.UpdateDataTask`
- `uk.gov.hmcts.ecm.common.service.UserService` -> `uk.gov.hmcts.ecm.compat.common.service.UserService`
- `uk.gov.hmcts.ecm.common.servicebus.MessageBodyRetriever` -> `uk.gov.hmcts.ecm.compat.common.servicebus.MessageBodyRetriever`
- `uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender` -> `uk.gov.hmcts.ecm.compat.common.servicebus.ServiceBusSender`

## Notes

- The ECM compatibility namespace includes the full `ecm-common` source set so internal ECM class dependencies remain consistent.
- One namespace modernization was required for compatibility with Spring Boot 3 / Jakarta (`javax.annotation.PreDestroy` -> `jakarta.annotation.PreDestroy`) in the compatibility copy.
