package uk.gov.hmcts.ethos.replacement.docmosis.wa.expiredbftask;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ElasticSearchQuery {
    private static final String END_QUERY = "\n}";
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
        """;

    public String getQuery(String fromDate) {
        LocalDate today = LocalDate.now();
        String baseQuery = String.format(START_BF_ACTIONS_QUERY, fromDate, today, size);

        if (!initialSearch && searchAfterValue != null && !searchAfterValue.isEmpty()) {
            baseQuery += String.format(",%n  \"search_after\": [%s]", searchAfterValue);
        }

        return baseQuery + END_QUERY;
    }
}
