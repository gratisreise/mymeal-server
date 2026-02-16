package com.mymealserver.auth.service;

/**
 * OAuth 제공업체 사용자 정보 인터페이스
 */
public interface OAuth2UserInfo {

    /**
     * 제공업체 사용자 ID 반환
     */
    String id();

    /**
     * 사용자 닉네임 또는 이름 반환
     */
    String name();

    /**
     * 프로필 이미지 URL 반환 (없을 경우 null)
     */
    String profileImage();
}
