package toock.backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import toock.backend.auth.dto.GoogleUserInfo;
import toock.backend.auth.dto.LoginResponseDto;
import toock.backend.auth.util.JwtUtil;
import toock.backend.member.domain.Field;
import toock.backend.member.domain.Member;
import toock.backend.member.repository.MemberRepository;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public LoginResponseDto processGoogleLogin(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = oAuth2UserService.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        GoogleUserInfo googleUserInfo = GoogleUserInfo.builder()
                .id((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .build();
        
        log.info("Google 로그인 처리: {}", googleUserInfo.getEmail());
        
        // 기존 회원인지 확인
        Member member = memberRepository.findByGoogleId(googleUserInfo.getId())
                .orElseGet(() -> memberRepository.findByEmail(googleUserInfo.getEmail())
                        .orElseGet(() -> createNewMember(googleUserInfo)));
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getEmail(), member.getId());
        
        return LoginResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }
    
    private Member createNewMember(GoogleUserInfo googleUserInfo) {
        // 새로운 회원 생성 (기본 필드 설정)
        Member newMember = Member.builder()
                .email(googleUserInfo.getEmail())
                .name(googleUserInfo.getName())
                .username(generateUsername(googleUserInfo.getEmail()))
                .field(Field.DEFAULT)
                .googleId(googleUserInfo.getId())
                .build();
        
        Member savedMember = memberRepository.save(newMember);
        log.info("새로운 회원 생성: {}", savedMember.getEmail());
        
        return savedMember;
    }
    
    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        // 중복된 username이 없을 때까지 숫자 추가
        while (memberRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}
