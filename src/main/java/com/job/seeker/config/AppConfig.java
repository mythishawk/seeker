package com.job.seeker.config;

import com.job.seeker.discovery.DiscoveryProperties;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on scheduling and settings binding, and provides the clock (so tests can fake time). */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({DiscoveryProperties.class, SeekerSecurityProperties.class})
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
