package toock.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import toock.backend.auth.service.AuthService;
import toock.backend.global.dto.CommonResponseDto;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/google/login")
    public ResponseEntity<CommonResponseDto<Void>> oauth2LoginFailure() {
        log.warn("OAuth2 로그인 실패");
        return ResponseEntity.status(401).body(CommonResponseDto.fail("UNAUTHORIZED", "로그인에 실패했습니다."));
    }

    @GetMapping("/user/me")
    public ResponseEntity<String> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("현재 인증된 사용자 ID: " + authentication.getName());
        }
        return ResponseEntity.ok("인증되지 않은 사용자");
    }
}
