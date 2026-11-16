package com.job.seeker.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Creates the plain-Java filtering pieces as Spring beans. */
@Configuration
public class FilterConfig {

    @Bean
    public LocationNormalizer locationNormalizer() {
        return LocationNormalizer.fromClasspath();
    }

    @Bean
    public ExperienceParser experienceParser() {
        return new ExperienceParser();
    }

    @Bean
    public FilterEngine filterEngine() {
        return FilterEngine.standard();
    }
}
