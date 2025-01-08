package io.cockroachdb.pc.model;

/**
 * Connection properties for connecting to a remote Pest Control service.
 */
public class AgentProperties {
    private String url;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
