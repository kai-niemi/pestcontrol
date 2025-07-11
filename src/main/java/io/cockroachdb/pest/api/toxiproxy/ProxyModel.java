package io.cockroachdb.pest.api.toxiproxy;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import io.cockroachdb.pest.api.LinkRelations;

@Relation(value = LinkRelations.PROXY_REL)
//        collectionRelation = LinkRelations.PROXY_COLL_REL)
public class ProxyModel extends RepresentationModel<ProxyModel> {
    private String path;

    private String name;

    private String listen;

    private String upstream;

    private boolean enabled;

    private int toxics;

    public int getToxics() {
        return toxics;
    }

    public void setToxics(int toxics) {
        this.toxics = toxics;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getListen() {
        return listen;
    }

    public void setListen(String listen) {
        this.listen = listen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUpstream() {
        return upstream;
    }

    public void setUpstream(String upstream) {
        this.upstream = upstream;
    }
}
