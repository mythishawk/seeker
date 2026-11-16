package com.job.seeker;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SeekerApplication {

	public static void main(String[] args) {
		// Run in UTC: some JVMs report legacy zone ids such as Asia/Calcutta, which newer Postgres rejects.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(SeekerApplication.class, args);
	}

}
