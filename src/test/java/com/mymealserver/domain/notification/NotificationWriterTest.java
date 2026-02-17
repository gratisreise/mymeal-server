package com.mymealserver.domain.notification;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationWriter 단위 테스트")
class NotificationWriterTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationWriter notificationWriter;

    private Long testMemberId;
    private Long testNotificationId;
    private Notification unreadNotification;
    private Notification readNotification;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;
        testNotificationId = 1L;

        unreadNotification = NotificationFixture.createUnreadNotification(testNotificationId, testMemberId);
        readNotification = NotificationFixture.createReadNotification(2L, testMemberId);
    }

    @Nested
    @DisplayName("단일 알림 읽음 처리")
    class MarkAsReadTests {

        @Test
        @DisplayName("알림 읽음 처리 - isRead가 true로 변경됨")
        void markAsRead_WithUnreadNotification_ShouldMarkAsRead() {
            // Given
            assertThat(unreadNotification.getIsRead()).isFalse();

            when(notificationRepository.save(any(Notification.class)))
                    .thenReturn(unreadNotification);

            // When
            notificationWriter.markAsRead(unreadNotification);

            // Then
            assertThat(unreadNotification.getIsRead()).isTrue();
            verify(notificationRepository).save(unreadNotification);
        }

        @Test
        @DisplayName("알림 읽음 처리 - readAt이 설정됨")
        void markAsRead_WithUnreadNotification_ShouldSetReadAt() {
            // Given
            assertThat(unreadNotification.getReadAt()).isNull();

            when(notificationRepository.save(any(Notification.class)))
                    .thenReturn(unreadNotification);

            // When
            notificationWriter.markAsRead(unreadNotification);

            // Then
            assertThat(unreadNotification.getReadAt()).isNotNull();
            verify(notificationRepository).save(unreadNotification);
        }

        @Test
        @DisplayName("이미 읽은 알림 처리 - 상태 유지")
        void markAsRead_WithReadNotification_ShouldKeepReadState() {
            // Given
            assertThat(readNotification.getIsRead()).isTrue();
            assertThat(readNotification.getReadAt()).isNotNull();

            when(notificationRepository.save(any(Notification.class)))
                    .thenReturn(readNotification);

            // When
            notificationWriter.markAsRead(readNotification);

            // Then
            assertThat(readNotification.getIsRead()).isTrue();
            assertThat(readNotification.getReadAt()).isNotNull();
            verify(notificationRepository).save(readNotification);
        }
    }

    @Nested
    @DisplayName("전체 알림 읽음 처리")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("전체 알림 읽음 처리 - 모든 읽지 않은 알림이 읽음으로 변경됨")
        void markAllAsRead_WithUnreadNotifications_ShouldMarkAllAsRead() {
            // Given
            List<Notification> unreadNotifications = List.of(
                    NotificationFixture.createUnreadNotification(1L, testMemberId),
                    NotificationFixture.createUnreadNotification(2L, testMemberId),
                    NotificationFixture.createUnreadNotification(3L, testMemberId)
            );

            when(notificationRepository.findByMemberIdAndIsReadAndDeletedAtIsNull(
                    eq(testMemberId), eq(false), any()))
                    .thenReturn(new PageImpl<>(unreadNotifications));

            // When
            notificationWriter.markAllAsRead(testMemberId);

            // Then
            assertThat(unreadNotifications).allMatch(n -> n.getIsRead());
            assertThat(unreadNotifications).allMatch(n -> n.getReadAt() != null);
            verify(notificationRepository).saveAll(unreadNotifications);
        }

        @Test
        @DisplayName("전체 알림 읽음 처리 - 읽지 않은 알림 없을 때 처리")
        void markAllAsRead_WithNoUnreadNotifications_ShouldDoNothing() {
            // Given
            List<Notification> emptyList = List.of();

            when(notificationRepository.findByMemberIdAndIsReadAndDeletedAtIsNull(
                    eq(testMemberId), eq(false), any()))
                    .thenReturn(new PageImpl<>(emptyList));

            // When
            notificationWriter.markAllAsRead(testMemberId);

            // Then
            verify(notificationRepository).saveAll(emptyList);
        }

        @Test
        @DisplayName("전체 알림 읽음 처리 - 일부만 읽지 않은 알림")
        void markAllAsRead_WithMixedNotifications_ShouldMarkOnlyUnreadAsRead() {
            // Given
            List<Notification> unreadNotifications = List.of(
                    NotificationFixture.createUnreadNotification(3L, testMemberId),
                    NotificationFixture.createUnreadNotification(5L, testMemberId)
            );

            when(notificationRepository.findByMemberIdAndIsReadAndDeletedAtIsNull(
                    eq(testMemberId), eq(false), any()))
                    .thenReturn(new PageImpl<>(unreadNotifications));

            // When
            notificationWriter.markAllAsRead(testMemberId);

            // Then
            assertThat(unreadNotifications).hasSize(2);
            assertThat(unreadNotifications).allMatch(n -> n.getIsRead());
            assertThat(unreadNotifications).allMatch(n -> n.getReadAt() != null);
            verify(notificationRepository).saveAll(unreadNotifications);
        }
    }
}
