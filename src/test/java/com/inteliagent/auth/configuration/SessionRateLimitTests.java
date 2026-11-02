package com.inteliagent.auth.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionRateLimitTests {
    private int request(AuthRateLimitFilter filter, String path) throws Exception {
        var request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("127.0.0.1");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {});
        return response.getStatus();
    }
    @Test void sessionRestorationAllowsMultipleTabsButRemainsBounded() throws Exception {
        var filter = new AuthRateLimitFilter();
        for (int i=0;i<120;i++) assertEquals(200,request(filter,"/api/user/refresh/web"));
        assertEquals(429,request(filter,"/api/user/refresh/web"));
    }
    @Test void loginProtectionRemainsUnchanged() throws Exception {
        var filter = new AuthRateLimitFilter();
        for (int i=0;i<10;i++) assertEquals(200,request(filter,"/api/user/login/web"));
        assertEquals(429,request(filter,"/api/user/login/web"));
    }
}
