package com.salesforce.mce.awesolog.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

public class S3ClientConstructor {

    static final String ASSUME_ROLE_SESSION_NAME = "AwesologSession";

    private String awsAccessKey;

    private String awsSecretKey;

    private String awsAssumeRoleArn;

    private String s3Region;

    public S3ClientConstructor(
        String awsAccessKey,
        String awsSecretKey,
        String awsAssumeRoleArn,
        String s3Region
    ) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.awsAssumeRoleArn = awsAssumeRoleArn;
        this.s3Region = s3Region;
    }

    public S3Client construct() {

        S3Client s3Client;
        StsClient stsClient = null;

        S3ClientBuilder s3ClientBuilder;
        StsClientBuilder stsClientBuilder;

        StaticCredentialsProvider staticCredentialsProvider = null;

        if (s3Region != null) {
            Region region = Region.of(s3Region);
            s3ClientBuilder = S3Client.builder().region(region);
            stsClientBuilder = StsClient.builder().region(region);
        } else {
            s3ClientBuilder = S3Client.builder();
            stsClientBuilder = StsClient.builder();
        }

        if (awsAccessKey != null && awsSecretKey != null) {
            staticCredentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
            );
        }

        if (staticCredentialsProvider != null) {
            stsClient = stsClientBuilder
                .credentialsProvider(staticCredentialsProvider)
                .build();
        }

        if (awsAssumeRoleArn != null) {

            AssumeRoleRequest assumeRoleRequest =
                AssumeRoleRequest
                    .builder()
                    .roleArn(awsAssumeRoleArn)
                    .roleSessionName(ASSUME_ROLE_SESSION_NAME)
                    .build();

            StsAssumeRoleCredentialsProvider stsAssumeRoleCredentials =
                StsAssumeRoleCredentialsProvider
                    .builder()
                    .refreshRequest(assumeRoleRequest)
                    .stsClient(stsClient)
                    .build();

            s3Client = s3ClientBuilder
                .credentialsProvider(stsAssumeRoleCredentials)
                .build();

        } else {

            if (staticCredentialsProvider != null) {
                s3Client = s3ClientBuilder
                    .credentialsProvider(staticCredentialsProvider)
                    .build();
            } else {
                s3Client = s3ClientBuilder.build();
            }
        }
        return s3Client;
    }
}
