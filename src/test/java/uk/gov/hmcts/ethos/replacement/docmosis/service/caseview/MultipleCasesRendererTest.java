package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultipleCasesRendererTest {

    private static final long MULTIPLE_CASE_REF = 1_234_567_890_123_456L;
    private static final String MULTIPLE_REF = "600001";

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<RowMapper<Object>> rowMapperCaptor;

    private MultipleCasesRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new MultipleCasesRenderer(jdbcTemplate);
    }

    @Test
    void render_whenMultipleReferenceIsNull_returnsNoCasesHtml() {
        String html = renderer.render(MULTIPLE_CASE_REF, null);

        assertThat(html)
            .contains("id=\"multiple-cases-" + MULTIPLE_CASE_REF + "\"")
            .contains("No single cases are linked to this multiple.")
            .doesNotContain("<table");
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void render_whenMultipleReferenceIsBlank_returnsNoCasesHtml() {
        String html = renderer.render(MULTIPLE_CASE_REF, "   ");

        assertThat(html)
            .contains("No single cases are linked to this multiple.")
            .doesNotContain("<table");
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void render_whenNoLinkedCasesFound_returnsNoCasesHtml() {
        when(jdbcTemplate.query(any(String.class), any(SqlParameterSource.class), any(RowMapper.class)))
            .thenReturn(List.of());

        String html = renderer.render(MULTIPLE_CASE_REF, MULTIPLE_REF);

        assertThat(html)
            .contains("No single cases are linked to this multiple.")
            .doesNotContain("<table");
    }

    @Test
    @SuppressWarnings("unchecked")
    void render_whenLinkedCasesExist_rendersTableWithEscapedContentAndFallbacks() throws SQLException {
        ResultSet resultSet1 = mock(ResultSet.class);
        when(resultSet1.getLong("reference")).thenReturn(1_111_222_233_334_444L);
        when(resultSet1.getString("ethos_case_reference")).thenReturn("12345/2024");
        when(resultSet1.getString("claimant")).thenReturn("John <script>alert('xss')</script>");
        when(resultSet1.getString("respondent")).thenReturn("Acme & Co > Ltd");
        when(resultSet1.getString("sub_multiple")).thenReturn("Sub Group A");
        when(resultSet1.getString("state")).thenReturn("Accepted");
        when(resultSet1.getString("lead_claimant")).thenReturn("Yes");

        ResultSet resultSet2 = mock(ResultSet.class);
        when(resultSet2.getLong("reference")).thenReturn(5_555_666_677_778_888L);
        when(resultSet2.getString("ethos_case_reference")).thenReturn(null);
        when(resultSet2.getString("claimant")).thenReturn("Jane Doe");
        when(resultSet2.getString("respondent")).thenReturn("Beta Corp");
        when(resultSet2.getString("sub_multiple")).thenReturn(" ");
        when(resultSet2.getString("state")).thenReturn("Submitted");
        when(resultSet2.getString("lead_claimant")).thenReturn(null);

        when(jdbcTemplate.query(any(String.class), any(SqlParameterSource.class), rowMapperCaptor.capture()))
            .thenAnswer(invocation -> {
                RowMapper<Object> mapper = invocation.getArgument(2);
                return List.of(mapper.mapRow(resultSet1, 0), mapper.mapRow(resultSet2, 1));
            });

        String html = renderer.render(MULTIPLE_CASE_REF, MULTIPLE_REF);

        assertThat(html)
            .contains("<table class=\"govuk-table\">")
            .contains("<a class=\"govuk-link\" href=\"/cases/case-details/1111222233334444\">12345/2024</a>")
            .contains("John &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;")
            .contains("Acme &amp; Co &gt; Ltd")
            .contains("Sub Group A")
            .contains("Accepted")
            .contains("Yes")
            .contains("<a class=\"govuk-link\" href=\"/cases/case-details/5555666677778888\">5555666677778888</a>")
            .contains("Jane Doe")
            .contains("Beta Corp")
            .contains("<td class=\"govuk-table__cell\">-</td>")
            .contains("Submitted")
            .doesNotContain("No single cases are linked to this multiple.");
    }
}
