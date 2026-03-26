package com.mymealserver.external.s3;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

  private static final long MAX_FILE_SIZE = 10_000_000; // 10MB
  private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp"};

  public String uploadMealPhoto(MultipartFile photo, Long memberId) {
    return null;
  }

  public void deletePhoto(String photoKey) {}

  public String extractPhotoKey(String photoUrl) {
    try {
      URI uri = new URI(photoUrl);
      String path = uri.getPath();
      // 패스경로 제거처리
      return path.startsWith("/") ? path.substring(1) : path;
    } catch (URISyntaxException e) {
      log.error("Invalid photo URL: {}", photoUrl, e);
      throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
    }
  }

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
    if (contentType == null
        || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
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

  private String generatePhotoKey(String originalFilename, Long memberId) {
    String extension = getFileExtension(originalFilename);
    String uuid = UUID.randomUUID().toString();
    LocalDateTime now = LocalDateTime.now();

    return String.format(
        "meals/%d/%d/%02d/%s.%s", memberId, now.getYear(), now.getMonthValue(), uuid, extension);
  }

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
