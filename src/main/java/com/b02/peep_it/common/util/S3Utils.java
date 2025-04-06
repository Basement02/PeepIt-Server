package com.b02.peep_it.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Utils {

    @Value("${cloud.aws.credentials.access}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret}")
    private String secretKey;
    @Value("${cloud.aws.region}")
    private String region;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

//    public String uploadFile(MultipartFile file) throws IOException {
//        // 1. AWS S3 Client 생성
//        S3Client s3Client = S3Client.builder()
//                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(accessKey, secretKey)))
//                .build();
//
//        // 2. 파일명 랜덤화
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//        // 3. S3 업로드 요청
//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(fileName)
//                .contentType(file.getContentType())
//                .build();
//
//        s3Client.putObject(
//                putObjectRequest,
//                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
//        );
//
//        // 4. 업로드된 파일의 S3 URL 반환
//        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
//    }

    public String generatePresignedUploadUrl(MultipartFile file) throws IOException {
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        String fileName = UUID.randomUUID().toString();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(5))
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        URL url = presignedRequest.url();

        log.info("📆 Local Time (KST): " + LocalDateTime.now());
        log.info("🕐 UTC Time: " + Instant.now());


        return url.toString();
    }
}