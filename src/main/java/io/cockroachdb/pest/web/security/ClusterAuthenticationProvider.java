package io.cockroachdb.pest.web.security;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;

@Component
public class ClusterAuthenticationProvider implements AuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final ClusterAuthenticationDetails authenticationDetails
                = (ClusterAuthenticationDetails) authentication.getDetails();

        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException("Missing credentials");
        }

        try {
            // Test login
            Cluster cluster = applicationProperties.getClusterById(authenticationDetails.getClusterId());
            ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(
                    authenticationDetails.getClusterId());
            try (StatusOperator statusOperator = clusterOperator.statusOperator(cluster)) {
                statusOperator.queryClusterVersion();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            authenticationDetails.setClusterProperties(cluster);

            // The principal needs to be of type 'User' or the thymeleaf taglib won't work
            final UserDetails principal = User.withUserDetails(createUserDetails("unascribed")).build();
            return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        } catch (Exception exception) {
            logger.warn("Authentication failed", exception);
            throw new AuthenticationServiceException("Authentication failed", exception);
        }
    }

    private UserDetails createUserDetails(String username) {
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getUsername() {
                return username;
            }
        };
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
