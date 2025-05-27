package io.cockroachdb.pestcontrol.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import io.cockroachdb.pestcontrol.api.cluster.ClusterModelAssembler;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.web.support.ClusterHelper;
import io.cockroachdb.pestcontrol.web.support.WebUtils;

@SessionAttributes("helper")
public abstract class AbstractSessionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClusterModelAssembler clusterModelAssembler = new ClusterModelAssembler();

    @ModelAttribute("helper")
    public ClusterHelper clusterHelper() {
        ClusterProperties clusterProperties = WebUtils.getAuthenticatedClusterProperties()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Expected authentication token"));
        return new ClusterHelper(false).setClusterModel(
                clusterModelAssembler.toModel(ClusterModel.from(clusterProperties)));
    }
}
