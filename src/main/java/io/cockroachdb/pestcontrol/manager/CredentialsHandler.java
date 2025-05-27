package io.cockroachdb.pestcontrol.manager;

import org.springframework.data.util.Pair;

public interface CredentialsHandler {
    default Pair<String, String> getAuthenticationCredentials(String clusterId) {
        throw new InvalidApiUsageException("No session token for cluster id: " + clusterId);
    }
}
