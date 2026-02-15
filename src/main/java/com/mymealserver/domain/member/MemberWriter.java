package com.mymealserver.domain.member;

import com.mymealserver.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberWriter {

    private final MemberRepository memberRepository;

}
