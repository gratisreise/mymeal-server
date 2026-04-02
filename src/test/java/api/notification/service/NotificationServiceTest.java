package api.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.mymealserver.api.notification.dto.response.NotificationListResponse;
import com.mymealserver.api.notification.dto.response.UnreadCountResponse;
import com.mymealserver.api.notification.service.NotificationService;
import com.mymealserver.common.enums.NotificationType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.notification.Notification;
import com.mymealserver.domain.notification.NotificationReader;
import com.mymealserver.domain.notification.NotificationWriter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationReader notificationReader;
  @Mock private NotificationWriter notificationWriter;

  @InjectMocks private NotificationService notificationService;

  private static final Long MEMBER_ID = 1L;
  private static final int DEFAULT_SIZE = 20;

  // ========================
  // getNotifications
  // ========================

  @Test
  void getNotifications_success_noCursorNoType() {
    // given
    List<Notification> notifications =
        List.of(
            createNotification(3L, NotificationType.RECOMMENDATION, false),
            createNotification(2L, NotificationType.REACTION_REMINDER, true),
            createNotification(1L, NotificationType.MEAL_REMINDER, false));

    Slice<Notification> slice = new SliceImpl<>(notifications);
    given(notificationReader.findByMemberIdWithCursor(any(), any(), any(), any()))
        .willReturn(slice);
    given(notificationReader.countUnread(MEMBER_ID)).willReturn(5L);

    // when
    NotificationListResponse response =
        notificationService.getNotifications(MEMBER_ID, null, DEFAULT_SIZE, null);

    // then
    assertThat(response.data()).hasSize(3);
    assertThat(response.hasNext()).isFalse();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.unreadCount()).isEqualTo(5);
  }

  @Test
  void getNotifications_success_hasNext() {
    // given: size=2, size+1=3개 반환 → hasNext=true
    List<Notification> notifications =
        List.of(
            createNotification(30L, NotificationType.RECOMMENDATION, false),
            createNotification(20L, NotificationType.REACTION_REMINDER, true),
            createNotification(10L, NotificationType.MEAL_REMINDER, false));

    Slice<Notification> slice = new SliceImpl<>(notifications);
    given(notificationReader.findByMemberIdWithCursor(any(), any(), any(), any()))
        .willReturn(slice);
    given(notificationReader.countUnread(MEMBER_ID)).willReturn(3L);

    // when
    NotificationListResponse response =
        notificationService.getNotifications(MEMBER_ID, 30L, 2, null);

    // then
    assertThat(response.data()).hasSize(2); // trimmed
    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextCursor()).isEqualTo(20L);
    assertThat(response.size()).isEqualTo(2);
  }

  @Test
  void getNotifications_success_withTypeFilter() {
    // given
    List<Notification> notifications =
        List.of(
            createNotification(5L, NotificationType.RECOMMENDATION, false),
            createNotification(4L, NotificationType.RECOMMENDATION, true));

    Slice<Notification> slice = new SliceImpl<>(notifications);
    given(notificationReader.findByMemberIdWithCursor(any(), any(), any(), any()))
        .willReturn(slice);
    given(notificationReader.countUnread(MEMBER_ID)).willReturn(1L);

    // when
    NotificationListResponse response =
        notificationService.getNotifications(
            MEMBER_ID, null, DEFAULT_SIZE, NotificationType.RECOMMENDATION);

    // then
    assertThat(response.data()).hasSize(2);
  }

  @Test
  void getNotifications_success_emptyResult() {
    // given
    Slice<Notification> emptySlice = new SliceImpl<>(List.of());
    given(notificationReader.findByMemberIdWithCursor(any(), any(), any(), any()))
        .willReturn(emptySlice);
    given(notificationReader.countUnread(MEMBER_ID)).willReturn(0L);

    // when
    NotificationListResponse response =
        notificationService.getNotifications(MEMBER_ID, null, DEFAULT_SIZE, null);

    // then
    assertThat(response.data()).isEmpty();
    assertThat(response.hasNext()).isFalse();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.unreadCount()).isZero();
  }

  // ========================
  // getUnreadCount
  // ========================

  @Test
  void getUnreadCount_success() {
    // given
    given(notificationReader.countUnread(MEMBER_ID)).willReturn(7L);

    // when
    UnreadCountResponse response = notificationService.getUnreadCount(MEMBER_ID);

    // then
    assertThat(response.count()).isEqualTo(7);
  }

  @Test
  void getUnreadCount_success_zero() {
    // given
    given(notificationReader.countUnread(MEMBER_ID)).willReturn(0L);

    // when
    UnreadCountResponse response = notificationService.getUnreadCount(MEMBER_ID);

    // then
    assertThat(response.count()).isZero();
  }

  // ========================
  // markAsRead
  // ========================

  @Test
  void markAsRead_success() {
    // given
    Notification notification = createNotification(1L, NotificationType.RECOMMENDATION, false);
    given(notificationReader.findByIdAndMemberId(1L, MEMBER_ID)).willReturn(notification);

    // when
    notificationService.markAsRead(MEMBER_ID, 1L);

    // then
    then(notificationWriter).should().markAsRead(notification);
  }

  @Test
  void markAsRead_fail_notificationNotFound() {
    // given
    given(notificationReader.findByIdAndMemberId(999L, MEMBER_ID))
        .willThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> notificationService.markAsRead(MEMBER_ID, 999L))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);

    then(notificationWriter).should(never()).markAsRead(any());
  }

  // ========================
  // markAllAsRead
  // ========================

  @Test
  void markAllAsRead_success() {
    // when
    notificationService.markAllAsRead(MEMBER_ID);

    // then
    then(notificationWriter).should().markAllAsRead(MEMBER_ID);
  }

  // ========================
  // markAsReadBatch
  // ========================

  @Test
  void markAsReadBatch_success() {
    // given
    List<Long> ids = List.of(1L, 2L, 3L);
    List<Notification> notifications =
        List.of(
            createNotification(1L, NotificationType.RECOMMENDATION, false),
            createNotification(2L, NotificationType.REACTION_REMINDER, false),
            createNotification(3L, NotificationType.MEAL_REMINDER, true));

    given(notificationReader.findByIdsAndMemberId(ids, MEMBER_ID)).willReturn(notifications);

    // when
    notificationService.markAsReadBatch(MEMBER_ID, ids);

    // then
    then(notificationWriter).should().markAsReadBatch(notifications);
  }

  @Test
  void markAsReadBatch_fail_sizeMismatch() {
    // given: 3개 요청했으나 2개만 조회됨
    List<Long> ids = List.of(1L, 2L, 3L);
    List<Notification> found =
        List.of(
            createNotification(1L, NotificationType.RECOMMENDATION, false),
            createNotification(2L, NotificationType.REACTION_REMINDER, false));

    given(notificationReader.findByIdsAndMemberId(ids, MEMBER_ID)).willReturn(found);

    // when & then
    assertThatThrownBy(() -> notificationService.markAsReadBatch(MEMBER_ID, ids))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);

    then(notificationWriter).should(never()).markAsReadBatch(any());
  }

  @Test
  void markAsReadBatch_fail_emptyResult() {
    // given
    List<Long> ids = List.of(1L, 2L);
    given(notificationReader.findByIdsAndMemberId(ids, MEMBER_ID)).willReturn(List.of());

    // when & then
    assertThatThrownBy(() -> notificationService.markAsReadBatch(MEMBER_ID, ids))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
  }

  // ========================
  // deleteNotification
  // ========================

  @Test
  void deleteNotification_success() {
    // given
    Notification notification = createNotification(1L, NotificationType.RECOMMENDATION, false);
    given(notificationReader.findByIdAndMemberId(1L, MEMBER_ID)).willReturn(notification);

    // when
    notificationService.deleteNotification(MEMBER_ID, 1L);

    // then
    then(notificationWriter).should().softDelete(notification);
  }

  @Test
  void deleteNotification_fail_notificationNotFound() {
    // given
    given(notificationReader.findByIdAndMemberId(999L, MEMBER_ID))
        .willThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> notificationService.deleteNotification(MEMBER_ID, 999L))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);

    then(notificationWriter).should(never()).softDelete(any());
  }

  // --- Helper ---

  private Notification createNotification(Long id, NotificationType type, boolean isRead) {
    return Notification.builder()
        .id(id)
        .memberId(MEMBER_ID)
        .type(type)
        .title("테스트 알림")
        .body("테스트 내용")
        .data("{\"key\":\"value\"}")
        .isRead(isRead)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
