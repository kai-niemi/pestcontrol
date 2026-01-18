package io.cockroachdb.pest.web;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.web.api.ClusterModel;
import io.cockroachdb.pest.web.api.MessageModel;
import io.cockroachdb.pest.web.api.MessageType;
import io.cockroachdb.pest.web.api.cluster.NodeController;
import io.cockroachdb.pest.web.simp.SimpMessagePublisher;
import io.cockroachdb.pest.web.simp.TopicName;

@WebController
@Profile(ProfileNames.ONLINE)
@RequestMapping("/cluster")
public class ClusterDashboardController extends AbstractSessionController {
    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Autowired
    private NodeController nodeController;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void scheduledStatusUpdate() {
        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_MODEL_UPDATE);
    }

    private Optional<Collection<NodeModel>> newClusterNodes() {
        Cluster cluster = WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        try {
            CollectionModel<NodeModel> statusModel = nodeController
                    .index(cluster.getClusterId())
                    .getBody();
            return Optional.ofNullable(statusModel.getContent());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @RequestParam(name = "level", defaultValue = "1", required = false) Integer level,
            Model model) {

        model.addAttribute("model", clusterModel);
        model.addAttribute("level", level);

        newClusterNodes().ifPresentOrElse(x -> {
            clusterModel.setNodeModels(x);
        }, () -> {
            clusterModel.setAvailable(false);
        });

        return () -> "cluster";
    }

    //
    // JSON endpoints below called from javascript triggered by STOMP messages.
    //

    @GetMapping("/update")
    public @ResponseBody ResponseEntity<Void> modelUpdate(
            @SessionAttribute(value = "model") ClusterModel clusterModel) {
        logger.debug("Performing cluster update (clusterId: %s)".formatted(clusterModel.getClusterId()));

        newClusterNodes().ifPresentOrElse(x -> {
            logger.debug("Cluster update successful (clusterId: %s)".formatted(clusterModel.getClusterId()));

            if (clusterModel.isDifferent(x) || !clusterModel.isAvailable()) {
                logger.warn("Node count differs - forcing refresh");

                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Cluster topology changed!").setMessageType(MessageType.information));
                messagePublisher.convertAndSendNow(TopicName.DASHBOARD_REFRESH_PAGE);
            } else {
                x.forEach(nodeModel ->
                        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_NODE_STATUS, nodeModel));
            }

            clusterModel.setNodeModels(x);
            clusterModel.setAvailable(true);
        }, () -> {
            logger.warn("Cluster update failed (clusterId: %s)".formatted(clusterModel.getClusterId()));

            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Cluster status refresh failed!").setMessageType(MessageType.warning));
            messagePublisher.convertAndSendNow(TopicName.DASHBOARD_REFRESH_PAGE);

            clusterModel.setAvailable(false);
        });

        return ResponseEntity.ok().build();
    }
}
