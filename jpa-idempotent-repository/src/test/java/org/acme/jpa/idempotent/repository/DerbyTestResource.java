package org.acme.jpa.idempotent.repository;

import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.camel.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * Derby test resource starts derby container in case that SQL_USE_DERBY_DOCKER is set to true. It uses fixed port
 * number obtained from SQL_USE_DERBY_PORT.
 */
public class DerbyTestResource<T extends GenericContainer> implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DerbyTestResource.class);
    private static final String DERBY_IMAGE_NAME = "az82/docker-derby:10.16";

    private GenericContainer container;

    @Override
    public Map<String, String> start() {

        LOGGER.info(TestcontainersConfiguration.getInstance().toString());

        try {
            container = new GenericContainer(DERBY_IMAGE_NAME)
                    .withExposedPorts(1527)
                    .withCopyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/init.sql")
                    .waitingFor(Wait.forListeningPort());
            container.start();

            container.execInContainer("java", "-Djdbc.drivers=org.apache.derbbc.EmbeddedDriver",
                    "org.apache.derby.tools.ij", "/init.sql");

            return CollectionHelper.mapOf("quarkus.datasource.jdbc.url",
                    "jdbc:derby://localhost:" + container.getMappedPort(1527) + "/my-db", "timer.period", "100", "timer.delay",
                    "0", "timer.repeatCount", "4");
        } catch (Exception e) {
            LOGGER.error("Container does not start", e);
            throw new RuntimeException(e);
        }
    }

    protected void startContainer() throws Exception {
        container.start();
    }

    @Override
    public void stop() {
        try {
            if (container != null) {
                container.stop();
            }
        } catch (Exception e) {
            // ignored
        }
    }
}
