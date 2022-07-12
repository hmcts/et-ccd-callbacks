package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

/**
 * ET3 vetting helper provides methods to assist with the ET3 vetting pages
 * this includes formatting markdown and querying the state of the ET3 response.
 */
@Slf4j
public class Et3ResponseHelper {

  private static final String CLAIMANT_NAME_TABLE = "<pre> ET1 claimant name&#09&#09&#09&#09 %s</pre><hr>";

  private Et3ResponseHelper() {
    // Access through static methods
  }

  public static String formatClaimantNameForHtml(CaseData caseData) {
    return String.format(CLAIMANT_NAME_TABLE, caseData.getClaimant());
  }
}
