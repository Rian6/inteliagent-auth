package com.inteliagent.auth.configuration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
class SessionStatusTests {
    @Test void revokedSessionRemainsUnauthorizedInsteadOfBecomingServerError() {
        assertEquals(HttpStatus.UNAUTHORIZED,new ApiExceptionHandler().status(new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getStatusCode());
    }
}
