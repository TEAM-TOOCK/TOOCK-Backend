package toock.backend.infra.s3;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import toock.backend.infra.s3.exception.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Util {

    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return extension;
    }

    // 파일 업로드
    public String uploadFile(String fileName, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
            log.info("S3 업로드 완료...");
        } catch (IOException e) {
            throw new S3Exception("S3 파일 업로드에 실패했습니다.", e);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // 파일 삭제
    public void deleteFile(String url) {
        URI uri = URI.create(url);
        String filePath = uri.getPath().substring(1);
        amazonS3.deleteObject(bucket, filePath);
    }



}