package io.cockroachdb.pest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ActiveProfiles;

import io.cockroachdb.pest.domain.ApplicationProperties;

@SpringBootTest(classes = {Application.class}, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration-test")
@ClusterName("local-secure")
@ActiveProfiles({"default", "test"})
public abstract class AbstractIntegrationTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ApplicationProperties applicationProperties;

    protected String getTagValue() {
        ClusterName tag = AnnotationUtils.findAnnotation(getClass(), ClusterName.class);
        Assertions.assertNotNull(tag, "Missing @ClusterName");
        return tag.value();
    }
}
