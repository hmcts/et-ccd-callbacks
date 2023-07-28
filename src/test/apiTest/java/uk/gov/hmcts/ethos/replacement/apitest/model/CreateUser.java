package uk.gov.hmcts.ethos.replacement.apitest.model;

import lombok.Data;

import java.util.List;

@Data
public class CreateUser {
    private final String email;
    private final String forename;
    private final String surname;
    private final String password;
    private final List<Role> roles;
}
