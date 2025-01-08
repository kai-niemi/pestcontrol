package io.cockroachdb.pc.web;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.cockroachdb.pc.web.push.SimpMessagePublisher;
import io.cockroachdb.pc.web.push.TopicName;

@Controller
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
