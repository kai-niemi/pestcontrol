package io.cockroachdb.pestcontrol.web.api.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.ApplicationProfiles;
import io.cockroachdb.pestcontrol.model.AgentProperties;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.web.front.MessageModel;

@RestController
@RequestMapping("/api/agent")
public class AgentRestController {
    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping
    public ResponseEntity<CollectionModel<AgentForm>> index() {
return CollectionModel.of()
    }
}
