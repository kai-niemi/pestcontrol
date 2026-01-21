package io.cockroachdb.pest.web.spa;

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
        WebUtils.getAuthenticatedClusterModel()
                .ifPresent(clusterModel -> {
                    model.addAttribute("clusterModel", clusterModel);
                });
        return () -> "home";
    }

    @GetMapping("/notice")
    public String noticePage(Model model) {
        return "notice";
    }
}
