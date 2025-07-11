package io.cockroachdb.pest.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import io.cockroachdb.pest.model.ClusterProperties;

@SessionAttributes("model")
public abstract class AbstractSessionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ModelAttribute("model")
    public ClusterModel clusterModel() {
        ClusterProperties clusterProperties = WebUtils.getAuthenticatedClusterProperties()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Expected authentication token"));
        return new ClusterModel(clusterProperties);
    }
}
