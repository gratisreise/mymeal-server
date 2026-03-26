package com.mymealserver.domain.memberwithdrawal;

import com.mymealserver.common.enums.WithdrawalReason;
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
public class MemberWithdrawal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private WithdrawalReason reason;

  @Column(columnDefinition = "TEXT")
  private String reasonDetail;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
