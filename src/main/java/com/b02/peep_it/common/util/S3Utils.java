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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
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

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 1. AWS S3 Client 생성
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        // 2. 파일명 랜덤화
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileName = folder + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 3. S3 업로드 요청
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        // 4. 업로드된 파일의 S3 URL 반환
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
    }

    public void deleteFile(String fileKey) {
        // 1. AWS S3 Client 생성
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        // 2. 삭제 요청 생성
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        // 3. 삭제 실행
        s3Client.deleteObject(deleteRequest);

        log.info("S3 파일 삭제 완료: {}", fileKey);
    }

    public String extractKeyFromUrl(String s3Url) {
        String baseUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
        return s3Url.replace(baseUrl, "");
    }
}