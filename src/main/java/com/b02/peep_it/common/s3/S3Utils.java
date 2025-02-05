package com.b02.peep_it.common.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

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

    public String uploadFile(MultipartFile file) throws IOException {
        // 1. AWS S3 Client 생성
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        // 2. 파일명 랜덤화 (UUID)
        String fileName = UUID.randomUUID() + "." + file.getOriginalFilename();

        // 3. S3 업로드 요청
        PutObjectRequest putOpjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        PutObjectResponse response = s3Client.putObject(putOpjectRequest, Paths.get(file.getOriginalFilename()));

        // 4. 업로드된 파일의 S3 URL 반환
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
