package com.job.seeker;

import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Starts the whole app against the Postgres from compose.yaml (needs Docker running).
 * This checks that the migrations apply and that Hibernate agrees with the schema.
 */
@SpringBootTest(properties = {
		// Spring Boot skips Docker Compose in tests unless told otherwise.
		"spring.docker.compose.skip.in-tests=false",
		// No real calls to job boards while testing.
		"seeker.discovery.enabled=false"
})
class SeekerApplicationTests {

	@BeforeAll
	static void useUtc() {
		// Same reason as in SeekerApplication.main: avoid legacy zone ids such as Asia/Calcutta.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@Test
	void contextLoads() {
	}

}
