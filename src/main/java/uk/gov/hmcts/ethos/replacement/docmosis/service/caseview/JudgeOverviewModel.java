package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import lombok.Value;

import java.util.List;

@Value
public class JudgeOverviewModel {
    String caption;
    String heading;
    List<Tag> tags;
    boolean warning;
    String todayMessage;
    List<AttentionItem> attentionItems;
    boolean hasAttentionItems;
    boolean hasOverdueBfActions;
    String overdueBfActions;
    Hearing nextHearing;
    List<Fact> keyFacts;
    List<Action> quickActions;

    @Value
    public static class Tag {
        String cssClass;
        String text;
    }

    @Value
    public static class AttentionItem {
        String cssClass;
        String status;
        String item;
        String raised;
        String due;
        String actionUrl;
        String actionLabel;
    }

    @Value
    public static class Hearing {
        boolean present;
        String type;
        String date;
        String venue;
        String judge;
    }

    @Value
    public static class Fact {
        String label;
        String value;
    }

    @Value
    public static class Action {
        String url;
        String label;
    }
}
