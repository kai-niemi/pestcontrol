package io.cockroachdb.pest.web;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.pest.config.ClosableDataSource;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.web.api.ClusterModel;
import io.cockroachdb.pest.web.api.cluster.WorkloadController;
import io.cockroachdb.pest.web.api.cluster.WorkloadForm;
import io.cockroachdb.pest.web.simp.SimpMessagePublisher;
import io.cockroachdb.pest.web.simp.TopicName;
import io.cockroachdb.pest.workload.WorkloadManager;
import io.cockroachdb.pest.workload.model.Workload;
import io.cockroachdb.pest.workload.repository.WorkloadType;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@WebController
@RequestMapping("/workload")
public class WorkloadDashboardController extends AbstractSessionController {
    private static final RepresentationModelAssembler<Workload, Workload> workloadAssembler
            = entity -> {
        entity.add(linkTo(methodOn(WorkloadController.class)
                .getWorker(entity.getClusterId(), entity.getId()))
                .withSelfRel());
        if (entity.isRunning()) {
            entity.add(linkTo(methodOn(WorkloadDashboardController.class)
                    .cancelWorker(null, entity.getId()))
                    .withRel(LinkRelations.CANCEL_REL + "-redirect"));
        } else {
            entity.add(linkTo(methodOn(WorkloadDashboardController.class)
                    .deleteWorker(null, entity.getId()))
                    .withRel(LinkRelations.DELETE_REL + "-redirect"));
        }
        return entity;
    };

    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void modelUpdate() {
        messagePublisher.convertAndSendNow(TopicName.WORKLOAD_MODEL_UPDATE);
    }

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void chartUpdate() {
        messagePublisher.convertAndSendNow(TopicName.WORKLOAD_CHARTS_UPDATE);
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            Model model) {
        WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        WorkloadForm workerForm = new WorkloadForm();
        workerForm.setDuration("00:15");
        workerForm.setWorkloadType(WorkloadType.singleton_insert);

        model.addAttribute("form", workerForm);
        model.addAttribute("workers",
                workloadAssembler.toCollectionModel(workloadManager.getWorkloads(clusterModel.getClusterId())));
        model.addAttribute("aggregatedMetrics",
                workloadManager.getMetricsAggregate(clusterModel.getClusterId()));

        return () -> "workload";
    }

    @PostMapping
    public Callable<String> submitForm(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @ModelAttribute WorkloadForm form,
            Model model) {

        final LocalTime time = LocalTime.parse(form.getDuration(), DateTimeFormatter.ofPattern("HH:mm"));
        final Duration duration = Duration.ofHours(time.getHour()).plusMinutes(time.getMinute());
        final DataSource dataSource = dataSourceFactory.apply(
                applicationProperties.getDataSourceProperties(clusterModel.getClusterId()));

        IntStream.rangeClosed(1, form.getCount())
                .forEach(value -> {
                    final Runnable task = form.getWorkloadType().createTask(dataSource);
                    workloadManager.submitWorkload(clusterModel.getClusterId(), duration, task,
                            form.getWorkloadType().getDisplayValue());
                });

        model.addAttribute("form", form);

        return () -> "redirect:workload";
    }

    @GetMapping("/{id}")
    public Callable<String> getDetails(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @PathVariable("id") Integer id, Model model) {
        return () -> {
            Workload workload = workloadManager.findById(clusterModel.getClusterId(), id);

            model.addAttribute("form", workload);

            return "workload-detail";
        };
    }

    @PostMapping(value = "/cancelAll")
    public RedirectView cancelAllWorkers(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel) {
        workloadManager.cancelAll(clusterModel.getClusterId());
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/deleteAll")
    public RedirectView deleteAllWorkers(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel) {
        workloadManager.deleteAll(clusterModel.getClusterId());
        return new RedirectView("/workload");
    }

    @GetMapping(value = "/cancel/{id}")
    public RedirectView cancelWorker(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @PathVariable("id") Integer id) {
        Workload worker = workloadManager.findById(clusterModel.getClusterId(), id);
        worker.cancel();
        return new RedirectView("/workload");
    }

    @GetMapping(value = "/delete/{id}")
    public RedirectView deleteWorker(
            @ModelAttribute(value = "model", binding = false) ClusterModel clusterModel,
            @PathVariable("id") Integer id) {
        workloadManager.deleteById(clusterModel.getClusterId(), id);
        return new RedirectView("/workload");
    }

    @GetMapping("/data-points/clear")
    public RedirectView clearDataPoints(
            @SessionAttribute(value = "model") ClusterModel clusterModel) {
        workloadManager.clearDataPoints(clusterModel.getClusterId());
        return new RedirectView("/workload");
    }
}
