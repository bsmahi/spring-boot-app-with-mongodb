package com.springapp.springbootappwithmongodb.health;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Triggers a {@link org.springframework.boot.actuate.health.Status#DOWN DOWN} health status
 * for the application when a specific environment variable is present.
 * This mechanism can be used to intentionally fail application health checks within a
 * deployment pipeline (e.g., in Harness) to test and trigger automatic rollback procedures.
 * To activate this failure state:
 * <ul>
 *     <li>Set the environment variable {@code TRIGGER_HEALTH_FAILURE} in the target deployment environment
 *     (e.g., within a Kubernetes manifest, Helm values file, or the Harness environment configuration).</li>
 * </ul>
 */

@Component
public class CanaryHealthIndicator implements HealthIndicator {

    private static final String FAILURE_ENV_VAR = "TRIGGER_HEALTH_FAILURE";
    private final Environment environment;

    public CanaryHealthIndicator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public @Nullable Health health() {
        if (environment.containsProperty(FAILURE_ENV_VAR)) {
            // If the environment variable is present, report DOWN
            return Health.down()
                    .withDetail("reason", "Manual failure triggered by env variable" + FAILURE_ENV_VAR)
                    .build();
        }
        // Otherwise, Report UP
        return Health.up().build();
    }
}
