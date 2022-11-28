package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;

@Component
@Slf4j
public class NoticeOfChangeAnswersConverter {
    public NoticeOfChangeAnswers generateForSubmission(RepresentedTypeRItem representedTypeRItem) {
        String respRepName = representedTypeRItem.getValue().getRespRepName();
        return NoticeOfChangeAnswers.builder().respondentName(respRepName)
            .build();
    }
}