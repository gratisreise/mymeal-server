package com.mymealserver.domain.searchprompt;

import com.mymealserver.common.db.SoftDeletable;
import com.mymealserver.common.enums.PromptType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SearchPrompt extends SoftDeletable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private PromptType promptType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String promptText;

  @Column(columnDefinition = "vector(3072)")
  private String embedding;

  @Column private LocalDateTime embeddingCreatedAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  public void deactivate() {
    this.isActive = false;
  }

  public void activate() {
    this.isActive = true;
  }
}
