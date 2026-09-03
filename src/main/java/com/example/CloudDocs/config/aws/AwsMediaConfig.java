package com.example.CloudDocs.config.aws;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsMediaConfig {

    public Object s3Presigner(AwsMediaProperties properties) {
        return null;
    }

    public Object s3Client(AwsMediaProperties properties) {
        return null;
    }

    public Object sqsClient(AwsMediaProperties properties) {
        return null;
    }
}

