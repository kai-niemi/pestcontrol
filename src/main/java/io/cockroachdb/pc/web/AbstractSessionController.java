package io.cockroachdb.pc.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import io.cockroachdb.pc.schema.ClusterModel;
import io.cockroachdb.pc.model.ClusterProperties;
import io.cockroachdb.pc.web.api.cluster.ClusterHelper;
import io.cockroachdb.pc.web.api.cluster.ClusterModelAssembler;

@SessionAttributes("helper")
public abstract class AbstractSessionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClusterModelAssembler clusterModelAssembler;

    @ModelAttribute("helper")
    public ClusterHelper clusterHelper() {
        ClusterProperties clusterProperties = WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));
        return new ClusterHelper(false)
                .setClusterModel(clusterModelAssembler.toModel(ClusterModel.from(clusterProperties)));
    }

}
