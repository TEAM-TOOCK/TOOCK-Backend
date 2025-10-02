package toock.backend.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import toock.backend.auth.service.AuthService;
import toock.backend.auth.dto.LoginResponseDto;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.success-url}")
    private String oauth2SuccessUrl;

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공 핸들러 실행");
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // AuthService를 통해 JWT 토큰을 포함한 LoginResponseDto를 받음
        LoginResponseDto loginResponse = authService.processGoogleLogin(oauth2User);

        String targetUrl = UriComponentsBuilder.fromUriString(oauth2SuccessUrl)
                .queryParam("accessToken", loginResponse.getAccessToken())
                .build().toUriString();

        log.info("리다이렉트 URL: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
