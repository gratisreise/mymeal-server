package com.mymealserver.external.s3.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.external.s3.config.S3Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.S3Utilities;

import java.net.URI;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageService 단위 테스트")
class FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Config s3Config;

    @Mock
    private S3Utilities s3Utilities;

    @InjectMocks
    private FileStorageService fileStorageService;

    private static final String TEST_BUCKET = "test-bucket";
    private static final Long TEST_MEMBER_ID = 1L;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(s3Config.getBucket()).thenReturn(TEST_BUCKET);
        lenient().when(s3Client.utilities()).thenReturn(s3Utilities);
        lenient().when(s3Utilities.getUrl(any(java.util.function.Consumer.class)))
                .thenReturn(new java.net.URL("https://" + TEST_BUCKET + ".s3.ap-northeast-2.amazonaws.com/meals/1/2025/02/test.jpg"));
    }

    @Nested
    @DisplayName("uploadMealPhoto - 식사 사진 업로드")
    class UploadMealPhotoTests {

        @Test
        @DisplayName("성공적으로 식사 사진을 업로드한다")
        void uploadMealPhoto_Success_ReturnsS3Url() {
            // Given
            MultipartFile photo = createValidPhoto("test.jpg", "image/jpeg", new byte[1000]);

            // When
            String url = fileStorageService.uploadMealPhoto(photo, TEST_MEMBER_ID);

            // Then
            assertThat(url).contains("amazonaws.com");
            assertThat(url).contains(TEST_BUCKET);
            then(s3Client).should().putObject(any(PutObjectRequest.class), any(RequestBody.class));
            then(s3Utilities).should().getUrl(any(java.util.function.Consumer.class));
        }

        @Test
        @DisplayName("빈 파일 업로드 시 FILE_EMPTY 에러 발생")
        void uploadMealPhoto_EmptyFile_ThrowsFileEmptyError() {
            // Given
            MultipartFile emptyPhoto = mock(MultipartFile.class);
            given(emptyPhoto.isEmpty()).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(emptyPhoto, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_EMPTY);

            then(s3Client).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("null 파일 업로드 시 FILE_EMPTY 에러 발생")
        void uploadMealPhoto_NullFile_ThrowsFileEmptyError() {
            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(null, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_EMPTY);
        }

        @Test
        @DisplayName("파일 크기 초과(10MB) 시 FILE_SIZE_EXCEEDED 에러 발생")
        void uploadMealPhoto_FileTooLarge_ThrowsSizeExceededError() {
            // Given
            MultipartFile largePhoto = mock(MultipartFile.class);
            given(largePhoto.isEmpty()).willReturn(false);
            given(largePhoto.getSize()).willReturn(15_000_000L); // 15MB

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(largePhoto, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED);

            then(s3Client).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("지원하지 않는 Content-Type 시 FILE_INVALID_TYPE 에러 발생")
        void uploadMealPhoto_InvalidContentType_ThrowsInvalidTypeError() {
            // Given
            MultipartFile invalidPhoto = mock(MultipartFile.class);
            given(invalidPhoto.isEmpty()).willReturn(false);
            given(invalidPhoto.getSize()).willReturn(1_000_000L);
            given(invalidPhoto.getContentType()).willReturn("application/pdf");

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(invalidPhoto, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_INVALID_TYPE);

            then(s3Client).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("지원하지 않는 파일 확장자 시 FILE_INVALID_TYPE 에러 발생")
        void uploadMealPhoto_InvalidExtension_ThrowsInvalidTypeError() {
            // Given
            MultipartFile invalidFile = mock(MultipartFile.class);
            given(invalidFile.isEmpty()).willReturn(false);
            given(invalidFile.getSize()).willReturn(1_000_000L);
            given(invalidFile.getContentType()).willReturn("image/gif");
            given(invalidFile.getOriginalFilename()).willReturn("photo.gif");

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(invalidFile, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_INVALID_TYPE);

            then(s3Client).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("S3 AccessDenied 시 FILE_STORAGE_ACCESS_DENIED 에러 발생")
        void uploadMealPhoto_S3AccessDenied_ThrowsAccessDeniedError() throws Exception {
            // Given
            MultipartFile photo = createValidPhoto("test.jpg", "image/jpeg", new byte[1000]);

            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .willThrow(S3Exception.builder()
                            .message("Access Denied")
                            .awsErrorDetails(AwsErrorDetails.builder()
                                    .errorCode("AccessDenied")
                                    .build())
                            .build());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(photo, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_STORAGE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("S3 NoSuchBucket 시 FILE_STORAGE_BUCKET_NOT_FOUND 에러 발생")
        void uploadMealPhoto_S3NoSuchBucket_ThrowsBucketNotFoundError() throws Exception {
            // Given
            MultipartFile photo = createValidPhoto("test.jpg", "image/jpeg", new byte[1000]);

            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .willThrow(S3Exception.builder()
                            .message("No such bucket")
                            .awsErrorDetails(AwsErrorDetails.builder()
                                    .errorCode("NoSuchBucket")
                                    .build())
                            .build());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(photo, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_STORAGE_BUCKET_NOT_FOUND);
        }

        @Test
        @DisplayName("파일 읽기 실패 시 FILE_READ_ERROR 에러 발생")
        void uploadMealPhoto_IOException_ThrowsReadError() throws Exception {
            // Given
            MultipartFile photo = mock(MultipartFile.class);
            given(photo.isEmpty()).willReturn(false);
            given(photo.getSize()).willReturn(1_000_000L);
            given(photo.getContentType()).willReturn("image/jpeg");
            given(photo.getOriginalFilename()).willReturn("test.jpg");
            given(photo.getBytes()).willThrow(new java.io.IOException("Read failed"));

            // When & Then
            assertThatThrownBy(() -> fileStorageService.uploadMealPhoto(photo, TEST_MEMBER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_READ_ERROR);
        }

        @Test
        @DisplayName("지원되는 모든 이미지 확장자 업로드에 성공한다")
        void uploadMealPhoto_AllSupportedExtensions_ShouldSucceed() throws Exception {
            // Given
            String[] extensions = {"jpg", "jpeg", "png", "webp"};

            for (String ext : extensions) {
                MultipartFile photo = createValidPhoto("photo." + ext, "image/" + ext, new byte[1000]);

                // When
                String url = fileStorageService.uploadMealPhoto(photo, TEST_MEMBER_ID);

                // Then
                assertThat(url).contains("amazonaws.com");
            }

            then(s3Client).should(times(4)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("확장자가 없는 파일은 기본 확장자 jpg로 처리한다")
        void uploadMealPhoto_NoExtension_ShouldUseDefaultJpg() {
            // Given
            MultipartFile photo = createValidPhoto("photo", "image/jpeg", new byte[1000]);

            // When
            String url = fileStorageService.uploadMealPhoto(photo, TEST_MEMBER_ID);

            // Then
            assertThat(url).isNotNull();
            then(s3Client).should().putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    @Nested
    @DisplayName("deletePhoto - 사진 삭제")
    class DeletePhotoTests {

        @Test
        @DisplayName("성공적으로 S3 객체를 삭제한다")
        void deletePhoto_Success_DeletesObject() {
            // Given
            String photoKey = "meals/1/2025/02/test.jpg";

            // When
            fileStorageService.deletePhoto(photoKey);

            // Then
            then(s3Client).should().deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("삭제 시 AccessDenied 에러가 발생하면 FILE_STORAGE_ACCESS_DENIED 에러로 변환")
        void deletePhoto_AccessDenied_ThrowsAccessDeniedError() {
            // Given
            String photoKey = "meals/1/2025/02/test.jpg";

            given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .willThrow(S3Exception.builder()
                            .message("Access Denied")
                            .awsErrorDetails(AwsErrorDetails.builder()
                                    .errorCode("AccessDenied")
                                    .build())
                            .build());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.deletePhoto(photoKey))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_STORAGE_ACCESS_DENIED);
        }

        @Test
        @DisplayName("삭제 시 NoSuchBucket 에러가 발생하면 FILE_STORAGE_BUCKET_NOT_FOUND 에러로 변환")
        void deletePhoto_NoSuchBucket_ThrowsBucketNotFoundError() {
            // Given
            String photoKey = "meals/1/2025/02/test.jpg";

            given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .willThrow(S3Exception.builder()
                            .message("No such bucket")
                            .awsErrorDetails(AwsErrorDetails.builder()
                                    .errorCode("NoSuchBucket")
                                    .build())
                            .build());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.deletePhoto(photoKey))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_STORAGE_BUCKET_NOT_FOUND);
        }

        @Test
        @DisplayName("일반 S3 에러가 발생하면 FILE_STORAGE_ERROR 에러로 변환")
        void deletePhoto_GeneralS3Error_ThrowsStorageError() {
            // Given
            String photoKey = "meals/1/2025/02/test.jpg";

            given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .willThrow(S3Exception.builder()
                            .message("S3 error")
                            .awsErrorDetails(AwsErrorDetails.builder()
                                    .errorCode("InternalError")
                                    .build())
                            .build());

            // When & Then
            assertThatThrownBy(() -> fileStorageService.deletePhoto(photoKey))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    @Nested
    @DisplayName("extractPhotoKey - S3 URL에서 photoKey 추출")
    class ExtractPhotoKeyTests {

        @Test
        @DisplayName("S3 URL에서 photoKey를 추출한다")
        void extractPhotoKey_ValidUrl_ReturnsPhotoKey() {
            // Given
            String url = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/meals/1/2025/02/test.jpg";

            // When
            String key = fileStorageService.extractPhotoKey(url);

            // Then
            assertThat(key).isEqualTo("meals/1/2025/02/test.jpg");
        }

        @Test
        @DisplayName("슬래시로 시작하는 path에서 photoKey를 추출한다")
        void extractPhotoKey_UrlWithSlashPrefix_ReturnsPhotoKey() {
            // Given
            String url = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/meals/1/2025/02/test.png";

            // When
            String key = fileStorageService.extractPhotoKey(url);

            // Then
            assertThat(key).isEqualTo("meals/1/2025/02/test.png");
        }

        @Test
        @DisplayName("잘못된 URL 형식 시 FILE_STORAGE_ERROR 에러 발생")
        void extractPhotoKey_InvalidUrl_ThrowsStorageError() {
            // Given
            String invalidUrl = "http://example.com/ path with spaces"; // URI with space is invalid

            // When & Then
            assertThatThrownBy(() -> fileStorageService.extractPhotoKey(invalidUrl))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.FILE_STORAGE_ERROR);
        }

        @Test
        @DisplayName("복잡한 경로 구조에서 photoKey를 추출한다")
        void extractPhotoKey_ComplexPath_ReturnsPhotoKey() {
            // Given
            String url = "https://bucket.s3.region.amazonaws.com/meals/123/2025/02/15/uuid-abc-123.jpg";

            // When
            String key = fileStorageService.extractPhotoKey(url);

            // Then
            assertThat(key).isEqualTo("meals/123/2025/02/15/uuid-abc-123.jpg");
        }
    }

    // Helper methods
    private MultipartFile createValidPhoto(String filename, String contentType, byte[] content) {
        return new MockMultipartFile("photo", filename, contentType, content);
    }
}
