package toock.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import toock.backend.auth.dto.LoginResponseDto;
import toock.backend.auth.service.AuthService;
import toock.backend.global.dto.CommonResponseDto;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/google/callback")
    public ResponseEntity<CommonResponseDto<LoginResponseDto>> googleCallback(OAuth2AuthenticationToken authentication) {
        try {
            if (authentication == null) {
                log.warn("OAuth2AuthenticationToken is null at callback");
                return ResponseEntity.status(401).body(CommonResponseDto.fail("UNAUTHORIZED", "인증 정보가 없습니다."));
            }
            String clientRegistrationId = authentication.getAuthorizedClientRegistrationId();
            String name = authentication.getName();
            
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(clientRegistrationId, name);
            if (client == null || client.getAccessToken() == null) {
                log.warn("Authorized client or access token is null");
                return ResponseEntity.status(401).build();
            }
            OAuth2AccessToken accessToken = client.getAccessToken();
            
            OAuth2UserRequest userRequest = new OAuth2UserRequest(client.getClientRegistration(), accessToken);
            LoginResponseDto response = authService.processGoogleLogin(userRequest);
            
            log.info("Google 로그인 성공: {}", response.getEmail());
            
            return ResponseEntity.ok(CommonResponseDto.success(response));
        } catch (Exception e) {
            log.error("Google 로그인 처리 중 오류 발생", e);
            return ResponseEntity.badRequest().body(CommonResponseDto.fail("BAD_REQUEST", e.getMessage()));
        }
    }

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
