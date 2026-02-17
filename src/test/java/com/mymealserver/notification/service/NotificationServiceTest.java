package com.mymealserver.notification.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.test.fixtures.NotificationFixture;
import com.mymealserver.domain.notification.NotificationReader;
import com.mymealserver.domain.notification.NotificationWriter;
import com.mymealserver.entity.Notification;
import com.mymealserver.notification.dto.response.NotificationResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationReader notificationReader;

    @Mock
    private NotificationWriter notificationWriter;

    @InjectMocks
    private NotificationService notificationService;

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
    class GetNotificationsTests {

        @Test
        @DisplayName("알림 목록 조회 - 전체 알림 조회 성공")
        void getNotifications_WithAllNotifications_ShouldReturnNotificationPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Notification> page = new PageImpl<>(testNotifications, pageable, testNotifications.size());

            when(notificationReader.findByMemberId(eq(testMemberId), eq(false), any(Pageable.class)))
                    .thenReturn(page);

            // When
            Page<NotificationResponse> result = notificationService.getNotifications(testMemberId, false, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).id()).isEqualTo(1L);
            assertThat(result.getContent().get(1).id()).isEqualTo(2L);
            assertThat(result.getContent().get(2).id()).isEqualTo(3L);
        }

        @Test
        @DisplayName("알림 목록 조회 - 읽지 않은 알림만 필터링")
        void getNotifications_WithUnreadOnly_ShouldFilterUnreadNotifications() {
            // Given
            List<Notification> unreadNotifications = testNotifications.stream()
                    .filter(Notification::isUnread)
                    .toList();
            Pageable pageable = PageRequest.of(0, 20);
            Page<Notification> page = new PageImpl<>(unreadNotifications, pageable, unreadNotifications.size());

            when(notificationReader.findByMemberId(eq(testMemberId), eq(true), any(Pageable.class)))
                    .thenReturn(page);

            // When
            Page<NotificationResponse> result = notificationService.getNotifications(testMemberId, true, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).isRead()).isFalse();
            assertThat(result.getContent().get(1).isRead()).isFalse();
        }

        @Test
        @DisplayName("알림 목록 조회 - 알림 없음 (빈 페이지)")
        void getNotifications_WithNoNotifications_ShouldReturnEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Notification> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(notificationReader.findByMemberId(eq(testMemberId), eq(false), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When
            Page<NotificationResponse> result = notificationService.getNotifications(testMemberId, false, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("알림 목록 조회 - 페이지네이션 확인")
        void getNotifications_WithPagination_ShouldReturnCorrectPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 2);
            List<Notification> firstPageNotifications = testNotifications.subList(0, 2);
            Page<Notification> page = new PageImpl<>(firstPageNotifications, pageable, 3);

            when(notificationReader.findByMemberId(eq(testMemberId), eq(false), any(Pageable.class)))
                    .thenReturn(page);

            // When
            Page<NotificationResponse> result = notificationService.getNotifications(testMemberId, false, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("단일 알림 읽음 처리")
    class MarkAsReadTests {

        @Test
        @DisplayName("알림 읽음 처리 - 성공")
        void markAsRead_WithValidNotification_ShouldMarkAsRead() {
            // Given
            Notification notification = NotificationFixture.createUnreadNotification(testNotificationId, testMemberId);

            when(notificationReader.findByIdAndMemberId(testNotificationId, testMemberId))
                    .thenReturn(notification);

            // When
            notificationService.markAsRead(testMemberId, testNotificationId);

            // Then
            verify(notificationReader).findByIdAndMemberId(testNotificationId, testMemberId);
            verify(notificationWriter).markAsRead(notification);
        }

        @Test
        @DisplayName("알림 읽음 처리 - 알림 없음 (권한 없음)")
        void markAsRead_WithNonExistentNotification_ShouldThrowException() {
            // Given
            when(notificationReader.findByIdAndMemberId(testNotificationId, testMemberId))
                    .thenThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> notificationService.markAsRead(testMemberId, testNotificationId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("전체 알림 읽음 처리")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("전체 알림 읽음 처리 - 성공")
        void markAllAsRead_WithUnreadNotifications_ShouldMarkAllAsRead() {
            // Given
            // When
            notificationService.markAllAsRead(testMemberId);

            // Then
            verify(notificationWriter).markAllAsRead(testMemberId);
        }

        @Test
        @DisplayName("전체 알림 읽음 처리 - 읽지 않은 알림 없음")
        void markAllAsRead_WithNoUnreadNotifications_ShouldCompleteSuccessfully() {
            // Given - reader/writer handles empty case internally
            // When
            notificationService.markAllAsRead(testMemberId);

            // Then
            verify(notificationWriter).markAllAsRead(testMemberId);
        }
    }

    @Nested
    @DisplayName("읽지 않은 알림 수 조회")
    class GetUnreadCountTests {

        @Test
        @DisplayName("읽지 않은 알림 수 조회 - 성공")
        void getUnreadCount_WithUnreadNotifications_ShouldReturnCorrectCount() {
            // Given
            long expectedCount = 2L;
            when(notificationReader.countUnread(testMemberId))
                    .thenReturn(expectedCount);

            // When
            long actualCount = notificationService.getUnreadCount(testMemberId);

            // Then
            assertThat(actualCount).isEqualTo(expectedCount);
            verify(notificationReader).countUnread(testMemberId);
        }

        @Test
        @DisplayName("읽지 않은 알림 수 조회 - 모두 읽음")
        void getUnreadCount_WithAllRead_ShouldReturnZero() {
            // Given
            when(notificationReader.countUnread(testMemberId))
                    .thenReturn(0L);

            // When
            long actualCount = notificationService.getUnreadCount(testMemberId);

            // Then
            assertThat(actualCount).isZero();
        }

        @Test
        @DisplayName("읽지 않은 알림 수 조회 - 알림 없음")
        void getUnreadCount_WithNoNotifications_ShouldReturnZero() {
            // Given
            when(notificationReader.countUnread(testMemberId))
                    .thenReturn(0L);

            // When
            long actualCount = notificationService.getUnreadCount(testMemberId);

            // Then
            assertThat(actualCount).isZero();
        }
    }
}
