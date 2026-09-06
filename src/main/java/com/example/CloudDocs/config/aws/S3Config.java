package com.example.CloudDocs.config.aws;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;



@Configuration
public class S3Config {
    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${aws.s3.presign-endpoint:${aws.s3.endpoint}}")
    private String s3PresignEndpoint;

    @Value("${aws.credentials.access-key:}")
    private String accessKey;

    @Value("${aws.credentials.secret-key:}")
    private String secretKey;
    

    @Bean
    public S3Client s3Client(){
        S3ClientBuilder builder=S3Client.builder()
        // DefaultCredProd checks the .envs automatically even in prod holy
        .credentialsProvider(credentialsProvider())
        .region(Region.of(region));

         if (!s3Endpoint.isBlank()) {
            builder.endpointOverride(URI.create(s3Endpoint))
                   .serviceConfiguration(pathStyleConfiguration());
        }

        return builder.build();

        
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .credentialsProvider(credentialsProvider())
                .region(Region.of(region));

        if (!s3PresignEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(s3PresignEndpoint))
                    .serviceConfiguration(pathStyleConfiguration());
        }

        return builder.build();
    }

    private S3Configuration pathStyleConfiguration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    private AwsCredentialsProvider credentialsProvider() {
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }
}
