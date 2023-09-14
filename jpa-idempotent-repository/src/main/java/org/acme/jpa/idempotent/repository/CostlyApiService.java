package org.acme.jpa.idempotent.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.camel.Handler;
import org.jboss.logging.Logger;

@ApplicationScoped
@RegisterForReflection
@Named("costlyApiService")
public class CostlyApiService {

    private static final Logger LOG = Logger.getLogger(CostlyApiService.class);

    private static Set<String> ALREADY_USED_CONTENT = ConcurrentHashMap.newKeySet();

    /**
     * The content parameter is populated with the incoming HTTP body sent to the API.
     */
    @Handler
    void invoke(String content) {
        if (ALREADY_USED_CONTENT.contains(content)) {
            LOG.info("Costly API has been called two times with the same content => TOO MUCH EXPENSIVE !");
        } else {
            ALREADY_USED_CONTENT.add(content);
            LOG.info("Costly API has been called with new content => GOOD");
        }
    }

    String getContentSet() {
        return ALREADY_USED_CONTENT.stream().sorted().reduce("", (a, b) -> a + "," + b);
    }
}
