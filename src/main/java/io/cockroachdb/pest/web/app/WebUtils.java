package io.cockroachdb.pest.web.app;

import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.web.model.ClusterModel;
import io.cockroachdb.pest.web.security.ClusterAuthenticationDetails;

@Component
@Profile(ProfileNames.ONLINE)
public class WebUtils {
    public static final String MSG_INFO = "MSG_INFO";

    public static final String MSG_ERROR = "MSG_ERROR";

    private static MessageSource messageSource;

    private static LocaleResolver localeResolver;

    public WebUtils(final MessageSource messageSource, final LocaleResolver localeResolver) {
        WebUtils.messageSource = messageSource;
        WebUtils.localeResolver = localeResolver;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, code, localeResolver.resolveLocale(getRequest()));
    }

    public static Optional<ClusterModel> getAuthenticatedClusterModel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getClass().isAssignableFrom(UsernamePasswordAuthenticationToken.class)) {
            ClusterAuthenticationDetails authenticationDetails = (ClusterAuthenticationDetails) auth.getDetails();
            return Optional.of(authenticationDetails.getClusterModel());
        }
        return Optional.empty();
    }
}
