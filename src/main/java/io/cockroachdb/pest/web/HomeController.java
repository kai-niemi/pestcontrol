package io.cockroachdb.pest.web;

import java.util.concurrent.Callable;

import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.cockroachdb.pest.ProfileNames;

@WebController
@Profile(ProfileNames.ONLINE)
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public Callable<String> indexPage(Model model) {
        WebUtils.getAuthenticatedClusterProperties().ifPresent(cluster -> {
            model.addAttribute("clusterProperties", cluster);
            model.addAttribute("clusterVersion", "Unable to get version");
        });
        return () -> "home";
    }

    @GetMapping("/notice")
    public String noticePage(Model model) {
        return "notice";
    }
}
