package com.challenge.chat.security.oauth.handler;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.challenge.chat.domain.member.repository.MemberRepository;
import com.challenge.chat.security.jwt.service.JwtService;
import com.challenge.chat.security.oauth.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final MemberRepository memberRepository;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException,
		ServletException {
		log.info("OAuth2 Login 성공!");
		try {
			CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();

			// User의 Role이 GUEST일 경우 처음 요청한 회원이므로 회원가입 페이지로 리다이렉트
			// if(oAuth2User.getRole() == MemberRole.GUEST) {
			// 	String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
			// response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
			// response.addCookie(new Cookie(jwtService.getAccessHeader(),
			// 	URLEncoder.encode("Bearer " + accessToken, "utf-8").replaceAll("\\+", "%20")));

			// response.sendRedirect("oauth2/sign-up"); // 프론트의 회원가입 추가 정보 입력 폼으로 리다이렉트

			// 	jwtService.sendAccessAndRefreshToken(response, accessToken, null);
			//
			// 	// User의 Role을 USER로 전환
			//    	Member findUser = memberRepository.findByEmail(oAuth2User.getEmail())
			// 		.orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));
			//    	findUser.authorizeUser();
			// 	log.info("이메일에 해당하는 유저를 db에서 찾았습니다!" + findUser.getRole());
			// } else {
			loginSuccess(response, oAuth2User); // 로그인에 성공한 경우 access, refresh 토큰 생성
			// }
		} catch (Exception e) {
			throw e;
		}

	}

	// TODO : 소셜 로그인 시에도 무조건 토큰 생성하지 말고 JWT 인증 필터처럼 RefreshToken 유/무에 따라 다르게 처리해보기
	private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
		String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
		String refreshToken = jwtService.createRefreshToken();

		// response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
		// response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

		jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
//		jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);

		String redirectUrl = "http://this.code.s3-website-us-east-1.amazonaws.com/userslist?" + "Authorization" + "=" +
			URLEncoder.encode("Bearer " + accessToken, "UTF-8") +
			"&" + "Authorization-refresh" + "=" + URLEncoder.encode("Bearer " + refreshToken, "UTF-8");
		response.sendRedirect(redirectUrl);
	}
}
