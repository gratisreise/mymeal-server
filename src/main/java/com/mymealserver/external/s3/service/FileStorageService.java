package com.mymealserver.external.s3.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.external.s3.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;
    private final S3Config s3Config;

    private static final long MAX_FILE_SIZE = 10_000_000; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp"};

    /**
     * 식사 사진 S3 업로드
     *
     * @param photo  업로드할 사진 파일
     * @param memberId 회원 ID (경로 생성용)
     * @return S3 URL
     */
    public String uploadMealPhoto(MultipartFile photo, Long memberId) {
        // 입력 검증
        validateMealPhoto(photo);

        try {
            String key = generatePhotoKey(photo.getOriginalFilename(), memberId);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(key)
                    .contentType(photo.getContentType())
                    .contentLength(photo.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(photo.getBytes()));

            String s3Url = s3Client.utilities().getUrl(builder ->
                    builder.bucket(getBucketName()).key(key)).toExternalForm();

            log.info("Uploaded meal photo for member {}: {}", memberId, key);
            return s3Url;

        } catch (S3Exception e) {
            log.error("S3 upload failed - Code: {}, Message: {}, Member: {}",
                    e.awsErrorDetails().errorCode(),
                    e.awsErrorDetails().errorMessage(),
                    memberId, e);

            // S3 에러 타입별 처리
            String errorCode = e.awsErrorDetails().errorCode();
            if (errorCode != null) {
                if (errorCode.contains("AccessDenied") || errorCode.contains("Forbidden")) {
                    throw new BusinessException(ErrorCode.FILE_STORAGE_ACCESS_DENIED);
                } else if (errorCode.contains("NoSuchBucket")) {
                    throw new BusinessException(ErrorCode.FILE_STORAGE_BUCKET_NOT_FOUND);
                }
            }
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);

        } catch (BusinessException e) {
            // 이미 처리된 비즈니스 예외는 그대로 전파
            throw e;

        } catch (Exception e) {
            log.error("Failed to upload meal photo for member {}", memberId, e);
            throw new BusinessException(ErrorCode.FILE_READ_ERROR);
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
                    .bucket(getBucketName())
                    .key(photoKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted meal photo: {}", photoKey);

        } catch (S3Exception e) {
            log.error("S3 deletion failed for photoKey: {} - Code: {}, Message: {}",
                    photoKey,
                    e.awsErrorDetails().errorCode(),
                    e.awsErrorDetails().errorMessage(),
                    e);

            // S3 에러 타입별 처리
            String errorCode = e.awsErrorDetails().errorCode();
            if (errorCode != null) {
                if (errorCode.contains("AccessDenied") || errorCode.contains("Forbidden")) {
                    throw new BusinessException(ErrorCode.FILE_STORAGE_ACCESS_DENIED);
                } else if (errorCode.contains("NoSuchBucket")) {
                    throw new BusinessException(ErrorCode.FILE_STORAGE_BUCKET_NOT_FOUND);
                }
            }
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);

        } catch (BusinessException e) {
            throw e;

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
     * 식사 사진 입력 검증
     *
     * @param photo 업로드할 사진 파일
     */
    private void validateMealPhoto(MultipartFile photo) {
        // 파일 존재 검증
        if (photo == null || photo.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        // 파일 크기 검증 (10MB 제한)
        if (photo.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // Content-Type 검증
        String contentType = photo.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") &&
                        !contentType.equals("application/octet-stream"))) {
            throw new BusinessException(ErrorCode.FILE_INVALID_TYPE);
        }

        // 파일 확장자 검증
        String filename = photo.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename);
            if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)) {
                throw new BusinessException(ErrorCode.FILE_INVALID_TYPE);
            }
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

    /**
     * 버킷 이름 반환
     *
     * @return S3 버킷 이름
     */
    private String getBucketName() {
        return s3Config.getBucket();
    }
}
