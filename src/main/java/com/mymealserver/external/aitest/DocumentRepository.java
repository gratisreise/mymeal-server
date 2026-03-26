package com.mymealserver.external.aitest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  @Transactional
  @Modifying
  @Query(
      value =
          "INSERT INTO document (content, embedding) "
              + "VALUES (:content, CAST(:embedding AS vector))",
      nativeQuery = true)
  void saveWithVector(String content, String embedding);
}
