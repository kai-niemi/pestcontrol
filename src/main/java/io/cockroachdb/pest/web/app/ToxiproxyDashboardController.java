package io.cockroachdb.pest.web.app;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.web.api.toxiproxy.ProxyForm;
import io.cockroachdb.pest.web.api.toxiproxy.ToxicForm;
import io.cockroachdb.pest.web.api.toxiproxy.ToxiproxyAccessException;
import io.cockroachdb.pest.web.api.toxiproxy.ToxiproxyController;

@WebController
@Profile(ProfileNames.ONLINE)
@RequestMapping("/proxy")
public class ToxiproxyDashboardController {
    @Autowired
    private ToxiproxyController toxiproxyRestController;

    @GetMapping
    public Callable<String> viewProxies(Model model) {
        model.addAttribute("proxies", toxiproxyRestController.findProxies().getBody());
        model.addAttribute("form", toxiproxyRestController.getProxyForm().getBody());
        return () -> "proxies";
    }

    @PostMapping
    public Callable<String> submitProxyForm(@Valid @ModelAttribute("form") ProxyForm form,
                                            BindingResult bindingResult,
                                            Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                toxiproxyRestController.newProxy(form);
            } catch (ToxiproxyAccessException e) {
                bindingResult.addError(new ObjectError("globalError", e.getLocalizedMessage()));
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("proxies", toxiproxyRestController.findProxies().getBody());
            model.addAttribute("form", form);
            return () -> "proxies";
        }

        return () -> "redirect:proxy";
    }

    @GetMapping("/{name}/enable")
    public Callable<String> enableProxy(@PathVariable("name") String name) {
        toxiproxyRestController.enableProxy(name);
        return () -> "redirect:/proxy";
    }

    @GetMapping("/{name}/disable")
    public Callable<String> disableProxy(
            @PathVariable("name") String name) {
        toxiproxyRestController.disableProxy(name);
        return () -> "redirect:/proxy";
    }

    @GetMapping("/{name}/delete")
    public Callable<String> deleteProxy(@PathVariable("name") String name) {
        toxiproxyRestController.deleteProxy(name);
        return () -> "redirect:/proxy";
    }

    @GetMapping("/{name}/toxic")
    public Callable<String> viewToxics(@PathVariable("name") String name, Model model) {
        model.addAttribute("proxy", toxiproxyRestController.findProxy(name).getBody());
        model.addAttribute("toxics", toxiproxyRestController.findProxyToxics(name).getBody());
        model.addAttribute("form", toxiproxyRestController.getToxicForm(name).getBody());
        return () -> "toxics";
    }

    @PostMapping("/{name}/toxic")
    public Callable<String> submitToxicForm(@PathVariable("name") String name,
                                            @Valid @ModelAttribute("form") ToxicForm form,
                                            BindingResult bindingResult,
                                            Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                toxiproxyRestController.newProxyToxic(name, form);
            } catch (ToxiproxyAccessException e) {
                bindingResult.addError(new ObjectError("globalError", e.getLocalizedMessage()));
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);

            switch (form.getToxicType()) {
                case LATENCY -> model.addAttribute("modal", "#addModalLatency");
                case BANDWIDTH -> model.addAttribute("modal", "#addModalBandwidth");
                case SLOW_CLOSE -> model.addAttribute("modal", "#addModalSlowClose");
                case TIMEOUT -> model.addAttribute("modal", "#addModalTimeout");
                case SLICER -> model.addAttribute("modal", "#addModalSlicer");
                case LIMIT_DATA -> model.addAttribute("modal", "#addModalLimitData");
                case RESET_PEER -> model.addAttribute("modal", "#addModalResetPeer");
            }

            return () -> "toxics";
        }

        return () -> "redirect:/proxy/" + name + "/toxic";
    }

    @GetMapping("/{name}/toxic/{toxic}/delete")
    public Callable<String> deleteProxyToxic(
            @PathVariable("name") String name,
            @PathVariable("toxic") String toxic) {
        toxiproxyRestController.deleteProxyToxic(name, toxic);
        return () -> "redirect:/proxy/" + name + "/toxic";
    }

}
