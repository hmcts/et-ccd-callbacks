package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/v3")
@RestController
@RequiredArgsConstructor
public class TestController {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;

    /**
     * JavaDocs.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/add", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "idklol")
    public ResponseEntity<Object> add(
            @RequestBody Object ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        log.error("url is: " + url);
        log.error("user is: " + user);
        log.error("password is: " + password);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Call add_work function
            try (CallableStatement addWork = conn.prepareCall("{ call add_work(?, ?) }")) {
                addWork.setString(1, "1");

                PGobject jsonObject = new PGobject();
                jsonObject.setType("json");
                jsonObject.setValue("{\"key\": \"case1\"}");
                addWork.setObject(2, jsonObject);

                addWork.execute();
            } catch (SQLException ex) {
                log.error(ex.getMessage());
                ex.printStackTrace();
            }

            try (CallableStatement addWork = conn.prepareCall("{ call add_work(?, ?) }")) {
                addWork.setString(1, "2");

                PGobject jsonObject = new PGobject();
                jsonObject.setType("json");
                jsonObject.setValue("{\"key\": \"case2\"}");
                addWork.setObject(2, jsonObject);

                addWork.execute();
            } catch (SQLException ex) {
                log.error(ex.getMessage());
                ex.printStackTrace();
            }

            // Call pick_up_work function
            try (CallableStatement pickUpWork = conn.prepareCall("{ call pick_up_work(?) }")) {
                pickUpWork.setInt(1, 2);
                boolean hasResults = pickUpWork.execute();
                if (hasResults) {
                    try (ResultSet rs = pickUpWork.getResultSet()) {
                        while (rs.next()) {
                            System.out.println("id: " + rs.getInt("id"));
                            // process other columns
                        }
                    }
                }
            } catch (SQLException ex) {
                log.error(ex.getMessage());
                ex.printStackTrace();
            }

            // Call complete_work function
            try (CallableStatement completeWork = conn.prepareCall("{ call complete_work(?) }")) {
                Array array = conn.createArrayOf("integer", new Integer[] { 1 });
                completeWork.setArray(1, array);
                completeWork.execute();
            }

            // Call errored_work function
            try (CallableStatement erroredWork = conn.prepareCall("{ call errored_work(?) }")) {
                Array array = conn.createArrayOf("integer", new Integer[] { 2 });
                erroredWork.setArray(1, array);
                erroredWork.execute();
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }

        // Code here
        return ResponseEntity.ok(new Object());
    }
}
