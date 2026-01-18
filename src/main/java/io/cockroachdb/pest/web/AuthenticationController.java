package io.cockroachdb.pest.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.web.security.AuthenticationRequest;

@WebController
@Profile(ProfileNames.ONLINE)
public class AuthenticationController {
    @Autowired
    private ApplicationProperties applicationProperties;

    @ModelAttribute("authentication")
    public AuthenticationRequest authenticationRequest() {
        return new AuthenticationRequest();
    }

    @GetMapping("/login")
    public String login(
            @ModelAttribute("authentication") AuthenticationRequest authenticationRequest,
            @RequestParam(name = "loginRequired", defaultValue = "false", required = false) Boolean loginRequired,
            @RequestParam(name = "loginError", defaultValue = "false", required = false) Boolean loginError,
            @RequestParam(name = "logoutSuccess", defaultValue = "false", required = false) Boolean logoutSuccess,
            final Model model) {

        List<String> ids = applicationProperties.getClusters()
                .stream()
                .map(Cluster::getClusterId)
                .toList();

        model.addAttribute("authentication", authenticationRequest);
        model.addAttribute("clusterIds", ids);
        model.addAttribute("defaultClusterId", applicationProperties.getDefaultClusterId());

        if (loginRequired) {
            model.addAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("authentication.login.required"));
        } else if (logoutSuccess) {
            model.addAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("authentication.logout.success"));
        }

        if (loginError) {
            model.addAttribute(WebUtils.MSG_ERROR, WebUtils.getMessage("authentication.login.error"));
        }

        return "login";
    }

    @ModelAttribute("requestUri")
    public String getRequestServletPath(final HttpServletRequest request) {
        return request.getRequestURI();
    }
}
