package io.cockroachdb.pestcontrol.web;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
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

import io.cockroachdb.pestcontrol.api.cluster.ClusterController;
import io.cockroachdb.pestcontrol.manager.ClusterManager;
import io.cockroachdb.pestcontrol.manager.CommandException;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.web.support.ClusterHelper;
import io.cockroachdb.pestcontrol.web.support.SimpMessagePublisher;
import io.cockroachdb.pestcontrol.web.support.TopicName;
import io.cockroachdb.pestcontrol.web.support.WebUtils;

@WebController
@RequestMapping("/cluster")
public class ClusterDashboardController extends AbstractSessionController {
    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ClusterController clusterController;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void scheduledStatusUpdate() {
        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_MODEL_UPDATE);
    }

    private Optional<ClusterModel> newClusterModel() {
        ClusterProperties clusterProperties = WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        try {
            ClusterModel clusterModel = clusterController
                    .getCluster(clusterProperties.getClusterId())
                    .getBody();
            return Optional.ofNullable(clusterModel);
        } catch (Exception e) {
            logger.warn("Error creating cluster model", e);
            return Optional.empty();
        }
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @RequestParam(name = "level", defaultValue = "1", required = false) Integer level,
            Model model) {

        model.addAttribute("helper", clusterHelper);
        model.addAttribute("level", level);

        newClusterModel().ifPresentOrElse(x -> {
            clusterHelper.setClusterModel(x);
            clusterHelper.setAvailable(true);
        }, () -> {
            clusterHelper.setAvailable(false);
        });

        return () -> "cluster";
    }

    @PostMapping(params = "action=disrupt-node")
    public Callable<String> disruptNode(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @ModelAttribute("node-id") Integer nodeId,
            RedirectAttributes redirectAttributes) {
        final String clusterId = clusterHelper.getId();

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

        redirectAttributes.addFlashAttribute("helper");

        return () -> "redirect:/cluster";
    }

    @PostMapping(params = "action=recover-node")
    public Callable<String> recoverNode(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @ModelAttribute("node-id") Integer nodeId,
            RedirectAttributes redirectAttributes) {
        final String clusterId = clusterHelper.getId();

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

        redirectAttributes.addFlashAttribute("helper");

        return () -> "redirect:/cluster";
    }

    @PostMapping(params = "action=disrupt-locality")
    public Callable<String> disruptLocality(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @ModelAttribute("locality") String locality) {
        final String clusterId = clusterHelper.getId();

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
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @ModelAttribute("locality") String locality) {
        final String clusterId = clusterHelper.getId();

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
            @SessionAttribute(value = "helper") ClusterHelper clusterHelper) {
        logger.debug("Performing cluster update (clusterId: %s)".formatted(clusterHelper.getId()));

        newClusterModel().ifPresentOrElse(x -> {
            logger.debug("Cluster update successful (clusterId: %s)".formatted(clusterHelper.getId()));

            if (clusterHelper.isDifferent(x) || !clusterHelper.isAvailable()) {
                logger.warn("Node count differs - forcing refresh");

                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Cluster topology updated").setMessageType(MessageType.information));
                messagePublisher.convertAndSendNow(TopicName.DASHBOARD_REFRESH_PAGE);
            } else {
                x.getNodes().forEach(nodeModel ->
                        messagePublisher.convertAndSendNow(TopicName.DASHBOARD_NODE_STATUS, nodeModel));
            }

            clusterHelper.setAvailable(true);
            clusterHelper.setClusterModel(x);
        }, () -> {
            logger.warn("Cluster update failed (clusterId: %s)".formatted(clusterHelper.getId()));

            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Cluster update failed - check log")
                            .setMessageType(MessageType.error));
            messagePublisher.convertAndSendNow(TopicName.DASHBOARD_REFRESH_PAGE);

            clusterHelper.setAvailable(false);
        });

        return ResponseEntity.ok().build();
    }
}
