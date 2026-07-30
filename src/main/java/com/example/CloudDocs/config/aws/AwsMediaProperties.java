package com.example.CloudDocs.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "media.aws")
public class AwsMediaProperties {

    private String region = "us-east-1";
    private String bucketName;
    private String rawPrefix = "raw";
    private String processedPrefix = "processed";
    private String queueUrl;
    private Duration uploadUrlTtl = Duration.ofMinutes(15);
    private Duration downloadUrlTtl = Duration.ofMinutes(15);

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRawPrefix() {
        return rawPrefix;
    }

    public void setRawPrefix(String rawPrefix) {
        this.rawPrefix = rawPrefix;
    }

    public String getProcessedPrefix() {
        return processedPrefix;
    }

    public void setProcessedPrefix(String processedPrefix) {
        this.processedPrefix = processedPrefix;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public Duration getUploadUrlTtl() {
        return uploadUrlTtl;
    }

    public void setUploadUrlTtl(Duration uploadUrlTtl) {
        this.uploadUrlTtl = uploadUrlTtl;
    }

    public Duration getDownloadUrlTtl() {
        return downloadUrlTtl;
    }

    public void setDownloadUrlTtl(Duration downloadUrlTtl) {
        this.downloadUrlTtl = downloadUrlTtl;
    }
}
