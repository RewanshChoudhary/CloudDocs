package com.example.CloudDocs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:clouddocs;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.flyway.enabled=false",
		"aws.region=us-east-1",
		"aws.s3.bucket-name=test-bucket",
		"aws.s3.endpoint=",
		"aws.credentials.access-key=test",
		"aws.credentials.secret-key=test",
		"spring.cloud.aws.region.static=us-east-1",
		"spring.cloud.aws.credentials.access-key=test",
		"spring.cloud.aws.credentials.secret-key=test"
})
class CloudDocsApplicationTests {

	@Test
	void contextLoads() {
	}

}
