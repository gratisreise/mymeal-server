package com.mymealserver.service.storage;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:mymeal-meal-photos}")
    private final String bucketName;

    /**
     * 식사 사진 S3 업로드
     *
     * @param photo  업로드할 사진 파일
     * @param memberId 회원 ID (경로 생성용)
     * @return S3 URL
     */
    public String uploadMealPhoto(MultipartFile photo, Long memberId) {
        try {
            String key = generatePhotoKey(photo.getOriginalFilename(), memberId);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(photo.getContentType())
                    .contentLength(photo.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(photo.getBytes()));

            String s3Url = s3Client.utilities().getUrl(builder ->
                    builder.bucket(bucketName).key(key)).toExternalForm();

            log.info("Uploaded meal photo for member {}: {}", memberId, key);
            return s3Url;

        } catch (S3Exception e) {
            log.error("S3 operation failed for member {}", memberId, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        } catch (Exception e) {
            log.error("Failed to upload meal photo for member {}", memberId, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    /**
     * 사진 S3에서 삭제
     *
     * @param photoKey S3 객체 키
     */
    public void deletePhoto(String photoKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(photoKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted meal photo: {}", photoKey);

        } catch (S3Exception e) {
            log.error("S3 deletion failed for photoKey: {}", photoKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        } catch (Exception e) {
            log.error("Failed to delete meal photo: {}", photoKey, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    /**
     * S3 URL에서 photoKey 추출
     *
     * @param photoUrl S3 URL
     * @return S3 객체 키
     */
    public String extractPhotoKey(String photoUrl) {
        try {
            URI uri = new URI(photoUrl);
            String path = uri.getPath();
            // path starts with /, so we need to remove it
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            log.error("Invalid photo URL: {}", photoUrl, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    /**
     * 고유한 S3 키 생성
     * 포맷: meals/{memberId}/{year}/{month}/{uuid}.{ext}
     *
     * @param originalFilename 원본 파일명
     * @param memberId 회원 ID
     * @return S3 키
     */
    private String generatePhotoKey(String originalFilename, Long memberId) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        return String.format("meals/%d/%d/%02d/%s.%s",
                memberId,
                now.getYear(),
                now.getMonthValue(),
                uuid,
                extension
        );
    }

    /**
     * 파일명에서 확장자 추출
     *
     * @param filename 파일명
     * @return 확장자 (소문자)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg"; // 기본값
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "jpg"; // 기본값
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
