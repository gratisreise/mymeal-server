package com.mymealserver.domain.notification;

import com.mymealserver.common.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NotificationType type;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 500)
  private String body;

  @Column(columnDefinition = "jsonb")
  private String data;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  @Column private LocalDateTime readAt;

  @Column private LocalDateTime sentAt;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column private LocalDateTime deletedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public void markAsRead() {
    this.isRead = true;
    this.readAt = LocalDateTime.now();
  }

  public void markAsSent() {
    this.sentAt = LocalDateTime.now();
  }

  public boolean isUnread() {
    return !isRead;
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }
}
