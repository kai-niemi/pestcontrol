package io.cockroachdb.pest.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationService {
    public Authentication getAuthentication(String clusterId) {
        return new ApiAuthenticationToken(clusterId, AuthorityUtils.NO_AUTHORITIES);
    }
}
