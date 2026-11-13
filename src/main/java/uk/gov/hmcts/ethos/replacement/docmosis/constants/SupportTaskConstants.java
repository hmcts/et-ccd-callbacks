package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class SupportTaskConstants {
    public static final String CONFIGURATION_PREFIX = "support-tasks";
    public static final String EVENT_CREATE_FLAG = "createFlag";
    public static final String EVENT_MANAGE_FLAGS = "manageFlags";
    public static final String EVENT_SUBMIT_CASE_DRAFT = "SUBMIT_CASE_DRAFT";
    public static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";
    public static final String EVENT_SUBMIT_ET3_FORM = "SUBMIT_ET3_FORM";
    public static final String EVENT_UPDATE_ET3_FORM = "UPDATE_ET3_FORM";
    public static final String EVENT_REQUEST_SUPPORT = "requestSupport";
    public static final String EVENT_MANAGE_SUPPORT = "manageSupport";
    public static final String EVENT_REVIEW_ADMIN_SUPPORT_REQUEST = "reviewAdminSupportRequest";
    public static final String EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST = "reviewLOSupportRequest";
    public static final String EVENT_REVIEW_JUDGE_SUPPORT_REQUEST = "reviewJudgeSupportRequest";
    public static final String EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK = "closeAdminReviewSupportTask";
    public static final String EVENT_CLOSE_LEGAL_OFFICER_REVIEW_SUPPORT_TASK = "closeLOReviewSupportTask";
    public static final String EVENT_CLOSE_JUDGE_REVIEW_SUPPORT_TASK = "closeJudgeReviewSupportTask";
    public static final String EVENT_CREATE_ARRANGE_SUPPORT_TASK = "createArrangeSupportTask";
    public static final String FLAG_STATUS_ACTIVE = "Active";
    public static final String FLAG_STATUS_REQUESTED = "Requested";
    public static final String TASK_TYPE_REVIEW_SUPPORT_ADMIN = "ReviewSupportRequestAdmin";
    public static final String TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER = "ReviewSupportRequestLegalOfficer";
    public static final String TASK_TYPE_REVIEW_SUPPORT_JUDGE = "ReviewSupportRequestJudge";

    private SupportTaskConstants() {
    }
}
