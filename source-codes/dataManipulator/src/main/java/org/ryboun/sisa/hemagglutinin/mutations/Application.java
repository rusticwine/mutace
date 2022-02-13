package org.ryboun.sisa.hemagglutinin.mutations;

import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@DataMongoTest
@EnableMongoRepositories(basePackageClasses = SequenceRepository.class)
@ComponentScan(basePackages = "org.ryboun.sisa")
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println("continue");
	}
}
