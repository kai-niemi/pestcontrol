package io.cockroachdb.pest.web.spa;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.web.api.cluster.ClusterController;
import io.cockroachdb.pest.web.model.ClusterModel;
import io.cockroachdb.pest.web.model.NodeStatusModel;

@WebController
@Profile(ProfileNames.ONLINE)
@RequestMapping("/cluster")
@SessionAttributes("model")
public class ClusterDashboardController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Autowired
    private ClusterController clusterController;

    @Scheduled(fixedRate = 10, initialDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void scheduledStatusUpdate() {
        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_MODEL_UPDATE);
    }

    @ModelAttribute("model")
    public ClusterModel clusterModel() {
        return WebUtils.getAuthenticatedClusterModel()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Expected authentication token"));
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            Model model) throws IOException {
        CollectionModel<NodeStatusModel> nodeStatusModels
                = clusterController.getNodeStatus(clusterModel.getClusterId()).getBody();
        model.addAttribute("nodes", nodeStatusModels);
        return () -> "cluster";
    }

    @GetMapping("/update")
    public @ResponseBody ResponseEntity<Void> modelUpdate(
            @SessionAttribute(value = "model") ClusterModel clusterModel,
            Model model) throws IOException {

        CollectionModel<NodeStatusModel> nodeStatusModels
                = clusterController.getNodeStatus(clusterModel.getClusterId()).getBody();
        model.addAttribute("nodes", nodeStatusModels);

        logger.info("Update nodes via STOMP");

        nodeStatusModels.forEach(nodeStatusModel ->
                messagePublisher.convertAndSendNow(TopicName.DASHBOARD_NODE_STATUS, nodeStatusModel));

//        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_REFRESH_PAGE);

        return ResponseEntity.ok().build();
    }
}
