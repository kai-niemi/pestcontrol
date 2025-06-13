package io.cockroachdb.pest.web;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.cockroachdb.pest.api.MessageModel;
import io.cockroachdb.pest.api.MessageType;
import io.cockroachdb.pest.api.cluster.NodeController;
import io.cockroachdb.pest.api.cluster.NodeModel;
import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.CommandException;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.web.simp.SimpMessagePublisher;
import io.cockroachdb.pest.web.simp.TopicName;

@WebController
@RequestMapping("/cluster")
public class ClusterDashboardController extends AbstractSessionController {
    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private NodeController nodeController;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void scheduledStatusUpdate() {
        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_MODEL_UPDATE);
    }

    private Optional<Collection<NodeModel>> newClusterNodes() {
        ClusterProperties clusterProperties = WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        try {
            CollectionModel<NodeModel> statusModel = nodeController
                    .getNodes(clusterProperties.getClusterId())
                    .getBody();
            return Optional.ofNullable(statusModel.getContent());
        } catch (Exception e) {
            logger.warn("Error creating cluster model", e);
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
            clusterModel.setAvailable(true);
        }, () -> {
            clusterModel.setAvailable(false);
        });

        return () -> "cluster";
    }

    @PostMapping(params = "action=disrupt-node")
    public Callable<String> disruptNode(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @ModelAttribute("node-id") Integer nodeId,
            RedirectAttributes redirectAttributes) {
        final String clusterId = clusterModel.getId();

        logger.debug(">> Performing 'disrupt-node' action: clusterId=%s, nodeId=%s"
                .formatted(clusterId, nodeId));

        try {
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Disrupt Node " + nodeId)
                            .setMessageType(MessageType.warning), Ordered.HIGHEST_PRECEDENCE);

            clusterManager.getClusterOperator(clusterId)
                    .disruptNode(clusterManager.getClusterProperties(clusterId), nodeId);
        } catch (CommandException e) {
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Command error - check log")
                            .setMessageType(MessageType.error), Ordered.HIGHEST_PRECEDENCE);
            logger.error("", e);
        } finally {
            logger.debug(">> Completed 'disrupt-node' action: clusterId=%s, nodeId=%s"
                    .formatted(clusterId, nodeId));
        }

        redirectAttributes.addFlashAttribute("model");

        return () -> "redirect:/cluster";
    }

    @PostMapping(params = "action=recover-node")
    public Callable<String> recoverNode(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @ModelAttribute("node-id") Integer nodeId,
            RedirectAttributes redirectAttributes) {
        final String clusterId = clusterModel.getId();

        logger.debug(">> Performing 'recover-node' action: clusterId=%s, nodeId=%s"
                .formatted(clusterId, nodeId));

        try {
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Recover Node " + nodeId)
                            .setMessageType(MessageType.information), Ordered.HIGHEST_PRECEDENCE);

            clusterManager.getClusterOperator(clusterId)
                    .recoverNode(clusterManager.getClusterProperties(clusterId), nodeId);
        } catch (CommandException e) {
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Command error - check log")
                            .setMessageType(MessageType.error), Ordered.HIGHEST_PRECEDENCE);
        } finally {
            logger.debug(">> Completed 'recover-node' action: clusterId=%s, nodeId=%s"
                    .formatted(clusterId, nodeId));
        }

        redirectAttributes.addFlashAttribute("model");

        return () -> "redirect:/cluster";
    }

    @PostMapping(params = "action=disrupt-locality")
    public Callable<String> disruptLocality(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @ModelAttribute("locality") String locality) {
        final String clusterId = clusterModel.getId();

        logger.debug(">> Performing 'disrupt-locality' action: clusterId=%s, locality=%s"
                .formatted(clusterId, locality));

        try {
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Disrupt locality " + locality)
                            .setMessageType(MessageType.warning), Ordered.HIGHEST_PRECEDENCE);

            clusterManager.getClusterOperator(clusterId)
                    .disruptNodes(clusterManager.getClusterProperties(clusterId), locality);
        } catch (Exception e) {
            logger.error("", e);
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Command error - check log")
                            .setMessageType(MessageType.error), Ordered.HIGHEST_PRECEDENCE);
        } finally {
            logger.debug(">> Completed 'disrupt-locality' action: clusterId=%s, locality=%s"
                    .formatted(clusterId, locality));
        }

        return () -> "redirect:/cluster";
    }

    @PostMapping(params = "action=recover-locality")
    public Callable<String> recoverLocality(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @ModelAttribute("locality") String locality) {
        final String clusterId = clusterModel.getId();

        logger.debug(">> Performing 'recover-locality' action: clusterId=%s, locality=%s"
                .formatted(clusterId, locality));

        try {
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Recover locality " + locality)
                            .setMessageType(MessageType.information), Ordered.HIGHEST_PRECEDENCE);

            clusterManager.getClusterOperator(clusterId)
                    .recoverNodes(clusterManager.getClusterProperties(clusterId), locality);
        } catch (Exception e) {
            logger.error("", e);
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Command error - check log")
                            .setMessageType(MessageType.error), Ordered.HIGHEST_PRECEDENCE);
        } finally {
            logger.debug(">> Completed 'recover-locality' action: clusterId=%s, locality=%s"
                    .formatted(clusterId, locality));
        }

        return () -> "redirect:/cluster";
    }

    //
    // JSON endpoints below called from javascript triggered by STOMP messages.
    //

    @GetMapping("/update")
    public @ResponseBody ResponseEntity<Void> modelUpdate(
            @SessionAttribute(value = "model") ClusterModel clusterModel) {
        logger.debug("Performing cluster update (clusterId: %s)".formatted(clusterModel.getId()));

        newClusterNodes().ifPresentOrElse(x -> {
            logger.debug("Cluster update successful (clusterId: %s)".formatted(clusterModel.getId()));

            if (clusterModel.isDifferent(x) || !clusterModel.isAvailable()) {
                logger.warn("Node count differs - forcing refresh");

                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Cluster topology updated").setMessageType(MessageType.information));
                messagePublisher.convertAndSendNow(TopicName.DASHBOARD_REFRESH_PAGE);
            } else {
                x.forEach(nodeModel ->
                        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_NODE_STATUS, nodeModel));
            }

            clusterModel.setAvailable(true);
            clusterModel.setNodeModels(x);
        }, () -> {
            logger.warn("Cluster update failed (clusterId: %s)".formatted(clusterModel.getId()));

            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Cluster update failed - check log")
                            .setMessageType(MessageType.error));
            messagePublisher.convertAndSendNow(TopicName.DASHBOARD_REFRESH_PAGE);

            clusterModel.setAvailable(false);
        });

        return ResponseEntity.ok().build();
    }
}
