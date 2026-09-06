package com.voyageguard.auth.api;

import com.voyageguard.auth.api.dto.MemberResponse;
import com.voyageguard.auth.api.dto.MemberSignUpRequest;
import com.voyageguard.auth.application.MemberService;
import com.voyageguard.auth.domain.member.Member;
import com.voyageguard.common.exception.AuthenticationFailedException;
import com.voyageguard.common.security.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원가입/내 정보 조회 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final CurrentMember currentMember;

    @Operation(summary = "로컬 회원가입", description = "이메일/비밀번호로 회원가입한다.")
    @ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    @PostMapping
    public Long signUp(@RequestBody MemberSignUpRequest request) {
        return memberService.signUp(request.email(), request.password(), request.name());
    }

    // JWT/Gateway 검증 메커니즘을 실제로 확인하기 위한 지점 - 로그인 필수를 URL 매처가 아니라
    // 여기서 CurrentMember로 직접 확인한다(전역 인증 강제는 아직 안 걸었으므로).
    @Operation(summary = "내 정보 조회", description = "로그인한 회원 본인의 정보를 조회한다.")
    @ApiResponse(responseCode = "401", description = "로그인 필요")
    @GetMapping("/me")
    public MemberResponse me() {
        Long memberId = currentMember.memberId()
                .orElseThrow(() -> new AuthenticationFailedException("로그인이 필요합니다."));
        Member member = memberService.get(memberId);
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getProvider(), member.getRole());
    }
}
