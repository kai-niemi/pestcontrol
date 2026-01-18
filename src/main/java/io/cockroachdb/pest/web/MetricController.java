package io.cockroachdb.pest.web;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.web.simp.SimpMessagePublisher;
import io.cockroachdb.pest.web.simp.TopicName;

@WebController
@Profile(ProfileNames.ONLINE)
@RequestMapping("/metrics")
public class MetricController {
    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void chartUpdate() {
        messagePublisher.convertAndSendNow(TopicName.METRIC_CHARTS_UPDATE, "");
    }

    @GetMapping
    public Callable<String> indexPage(Model model) {
        return () -> "metrics";
    }

}
