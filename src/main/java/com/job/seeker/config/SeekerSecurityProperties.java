package com.job.seeker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Login for the REST API. Leave the password empty to get a random one printed at startup. */
@ConfigurationProperties(prefix = "seeker.security")
public record SeekerSecurityProperties(
        @DefaultValue("seeker") String username,
        String password) {
}
