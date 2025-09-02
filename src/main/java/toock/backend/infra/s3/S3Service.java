package toock.backend.infra.s3;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Util s3Util;

    public String uploadImage(MultipartFile file, String folderName) {
        String extension = s3Util.extractExtension(file);
        String fileName = folderName + "/" + UUID.randomUUID() + extension;
        return s3Util.uploadFile(fileName, file);
    }

    public void removeImage(String fileUrl) {
        s3Util.deleteFile(fileUrl);
    }


    //음성파일 업로드를 위한 메서드들 -> 이미지랑 코드 중복이지만 나중에 음성에서 추가 작업할게 있을 수 있기 때문에 중복이더라도 메서드 생성

    /**
     * 음성 파일을 S3에 업로드합니다. 파일 이름은 '폴더명/타임스탬프_UUID.확장자' 형식으로 생성됩니다.
     * This ensures chronological order and uniqueness.
     * @param file 음성 파일
     * @param folderName 저장할 폴더명 (예: "audio-recordings")
     * @return 업로드된 파일의 S3 URL
     */
    public String uploadAudio(MultipartFile file, String folderName) {
        String extension = s3Util.extractExtension(file);

        // 타임스탬프를 yyyyMMddHHmmssSSS 형식으로 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        // 파일 이름 형식: folderName/timestamp_randomUUID.extension
        // UUID는 8자리만 사용하여 간결하게 만듭니다.
        String fileName = String.format("%s/%s_%s%s",
                folderName,
                timestamp,
                UUID.randomUUID().toString().substring(0, 8), // 동일 시간 업로드 충돌 방지
                extension);

        return s3Util.uploadFile(fileName, file);
    }

    /**
     * S3에서 음성 파일을 삭제합니다.
     * @param fileUrl 삭제할 파일의 S3 URL
     */
    public void removeAudio(String fileUrl) {
        s3Util.deleteFile(fileUrl);
    }
}