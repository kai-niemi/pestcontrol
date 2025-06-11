package io.cockroachdb.pestcontrol.web;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.cockroachdb.pestcontrol.cluster.ClusterManager;
import io.cockroachdb.pestcontrol.web.support.WebUtils;

@WebController
@RequestMapping("/")
public class HomeController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping
    public Callable<String> indexPage(Model model) {
        WebUtils.getAuthenticatedClusterProperties().ifPresent(properties -> {
            model.addAttribute("clusterProperties", properties);

            try {
                model.addAttribute("clusterVersion",
                        clusterManager.getClusterVersion(properties.getClusterId()));
            } catch (Exception e) {
                model.addAttribute("clusterVersion", "Unable to get version");
            }
        });
        return () -> "home";
    }

    @GetMapping("/notice")
    public String noticePage(Model model) {
        return "notice";
    }
}
