package com.mymealserver.domain.notification;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.test.fixtures.NotificationFixture;
import com.mymealserver.entity.Notification;
import com.mymealserver.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationReader 단위 테스트")
class NotificationReaderTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationReader notificationReader;

    private Long testMemberId;
    private Long testNotificationId;
    private List<Notification> testNotifications;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;
        testNotificationId = 1L;

        testNotifications = List.of(
                NotificationFixture.createUnreadNotification(1L, testMemberId),
                NotificationFixture.createReadNotification(2L, testMemberId),
                NotificationFixture.createUnreadNotification(3L, testMemberId)
        );
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class FindByMemberIdTests {

        @Test
        @DisplayName("전체 알림 조회 - 성공")
        void findByMemberId_WithValidMemberId_ShouldReturnNotifications() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Notification> page = new PageImpl<>(testNotifications, pageable, testNotifications.size());

            when(notificationRepository.findByMemberIdAndDeletedAtIsNull(eq(testMemberId), any(Pageable.class)))
                    .thenReturn(page);

            // When
            Page<Notification> result = notificationReader.findByMemberId(testMemberId, false, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent()).hasSize(3);
            verify(notificationRepository).findByMemberIdAndDeletedAtIsNull(eq(testMemberId), any(Pageable.class));
        }

        @Test
        @DisplayName("읽지 않은 알림만 조회 - 성공")
        void findByMemberId_WithUnreadOnly_ShouldFilterUnreadNotifications() {
            // Given
            List<Notification> unreadNotifications = testNotifications.stream()
                    .filter(Notification::isUnread)
                    .toList();
            Pageable pageable = PageRequest.of(0, 20);
            Page<Notification> page = new PageImpl<>(unreadNotifications, pageable, unreadNotifications.size());

            when(notificationRepository.findByMemberIdAndIsReadAndDeletedAtIsNull(
                    eq(testMemberId), eq(false), any(Pageable.class)))
                    .thenReturn(page);

            // When
            Page<Notification> result = notificationReader.findByMemberId(testMemberId, true, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().stream().allMatch(Notification::isUnread)).isTrue();
            verify(notificationRepository).findByMemberIdAndIsReadAndDeletedAtIsNull(
                    eq(testMemberId), eq(false), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("특정 알림 조회 (권한 검증)")
    class FindByIdAndMemberIdTests {

        @Test
        @DisplayName("알림 조회 - 성공")
        void findByIdAndMemberId_WithValidIds_ShouldReturnNotification() {
            // Given
            Notification notification = NotificationFixture.createUnreadNotification(testNotificationId, testMemberId);

            when(notificationRepository.findByIdAndMemberIdAndDeletedAtIsNull(testNotificationId, testMemberId))
                    .thenReturn(Optional.of(notification));

            // When
            Notification result = notificationReader.findByIdAndMemberId(testNotificationId, testMemberId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testNotificationId);
            assertThat(result.getMemberId()).isEqualTo(testMemberId);
            verify(notificationRepository).findByIdAndMemberIdAndDeletedAtIsNull(testNotificationId, testMemberId);
        }

        @Test
        @DisplayName("알림 조회 - 알림 없음 (다른 회원 또는 삭제된 알림)")
        void findByIdAndMemberId_WithInvalidIds_ShouldThrowException() {
            // Given
            when(notificationRepository.findByIdAndMemberIdAndDeletedAtIsNull(testNotificationId, testMemberId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> notificationReader.findByIdAndMemberId(testNotificationId, testMemberId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("읽지 않은 알림 수 조회")
    class CountUnreadTests {

        @Test
        @DisplayName("읽지 않은 알림 수 조회 - 성공")
        void countUnread_WithValidMemberId_ShouldReturnCorrectCount() {
            // Given
            long expectedCount = 2L;
            when(notificationRepository.countByMemberIdAndIsReadAndDeletedAtIsNull(testMemberId, false))
                    .thenReturn(expectedCount);

            // When
            long actualCount = notificationReader.countUnread(testMemberId);

            // Then
            assertThat(actualCount).isEqualTo(expectedCount);
            verify(notificationRepository).countByMemberIdAndIsReadAndDeletedAtIsNull(testMemberId, false);
        }

        @Test
        @DisplayName("읽지 않은 알림 수 조회 - 모두 읽음")
        void countUnread_WithAllRead_ShouldReturnZero() {
            // Given
            when(notificationRepository.countByMemberIdAndIsReadAndDeletedAtIsNull(testMemberId, false))
                    .thenReturn(0L);

            // When
            long actualCount = notificationReader.countUnread(testMemberId);

            // Then
            assertThat(actualCount).isZero();
        }

        @Test
        @DisplayName("읽지 않은 알림 수 조회 - 알림 없음")
        void countUnread_WithNoNotifications_ShouldReturnZero() {
            // Given
            when(notificationRepository.countByMemberIdAndIsReadAndDeletedAtIsNull(testMemberId, false))
                    .thenReturn(0L);

            // When
            long actualCount = notificationReader.countUnread(testMemberId);

            // Then
            assertThat(actualCount).isZero();
        }
    }
}
