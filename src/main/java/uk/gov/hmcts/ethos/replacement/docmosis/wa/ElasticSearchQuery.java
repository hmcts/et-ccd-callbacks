package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ElasticSearchQuery {
    private static final String END_QUERY = "\n}";
    private static final String SEARCH_AFTER = "\"search_after\": [%s]";
    private String searchAfterValue;
    private int size;
    private boolean initialSearch;

    private static final String START_BF_ACTIONS_QUERY = """
        {
          "query": {
            "bool": {
              "must": [
                {
                  "exists": {
                    "field": "data.bfActions"
                  }
                },
                {
                  "range": {
                    "data.bfActions.value.bfDate": {
                      "from": "%s",
                      "to": "%s",
                      "include_lower": true,
                      "include_upper": false
                    }
                  }
                }
              ],
              "filter": [
                {
                  "terms": {
                    "state.keyword": [
                      "Accepted",
                      "Rejected",
                      "Vetted"
                    ]
                  }
                }
              ]
            }
          },
          "_source": [
            "reference",
            "data.bfActions"
          ],
          "size": %s,
          "sort": [
            {
              "reference.keyword": "asc"
            }
          ]
        }
        """;

    public String getQuery(String fromDate) {
        LocalDate today = LocalDate.now();
        if (initialSearch) {
            return getInitialQuery(fromDate, today.toString());
        } else {
            return getSubsequentQuery(fromDate, today.toString());
        }
    }

    private String getInitialQuery(String fromDate, String toDate) {
        return String.format(START_BF_ACTIONS_QUERY, fromDate, toDate, size) + END_QUERY;
    }

    private String getSubsequentQuery(String fromDate, String toDate) {
        return String.format(START_BF_ACTIONS_QUERY, fromDate, toDate, size)
                + ","
                + String.format(SEARCH_AFTER, searchAfterValue) + END_QUERY;
    }
}
