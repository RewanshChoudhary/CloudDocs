package com;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.CloudDocs.service.aws.S3Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Testcontainers
@SpringBootTest
class S3ServiceIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.7"))
            .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("aws.region", () -> localstack.getRegion());
        registry.add("aws.s3.bucket-name", () -> "test-bucket");
        registry.add("aws.credentials.access-key", () -> localstack.getAccessKey());
        registry.add("aws.credentials.secret-key", () -> localstack.getSecretKey());
        registry.add("aws.s3.endpoint", () -> localstack.getEndpointOverride(
                LocalStackContainer.Service.S3).toString());
    }

    @Autowired
    private S3Service s3Service;

    @Autowired
    private S3Client s3Client;

    @BeforeAll
    static void createBucket() throws Exception {
        // Bucket must exist before tests run
        localstack.execInContainer("awslocal", "s3", "mb", "s3://test-bucket");
    }

    @Test
    void shouldUploadAndDownloadFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello S3".getBytes());

        String key = s3Service.uploadFile(file);
        byte[] downloaded = s3Service.downloadFile(key);

        assertThat(new String(downloaded)).isEqualTo("Hello S3");
    }

    @Test
    void shouldListUploadedFiles() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "list-test.txt", "text/plain", "data".getBytes());
        s3Service.uploadFile(file);

        List<String> files = s3Service.listFiles();

        assertThat(files).isNotEmpty();
    }

    @Test
    void shouldDeleteFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "delete-test.txt", "text/plain", "temp".getBytes());
        String key = s3Service.uploadFile(file);

        s3Service.deleteFile(key);

        assertThatThrownBy(() -> s3Service.downloadFile(key))
                .isInstanceOf(NoSuchKeyException.class);
    }
}
