package uk.gov.hmcts.ethos.replacement.apiTest.BaseFunctionalTest.model;

import java.util.List;
import lombok.Data;

@Data
public class CreateUser {
    private final String email;
    private final String forename;
    private final String surname;
    private final String password;
    private final List<Role> roles;
}
