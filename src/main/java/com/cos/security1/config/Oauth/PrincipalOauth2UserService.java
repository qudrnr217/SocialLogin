package com.cos.security1.config.Oauth;

import com.cos.security1.auth.PrincipalDetails;
import com.cos.security1.config.Oauth.provider.FacebookUserInfo;
import com.cos.security1.config.Oauth.provider.GoogleUserInfo;
import com.cos.security1.config.Oauth.provider.NaverUserInfo;
import com.cos.security1.config.Oauth.provider.OAuth2UserInfo;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private BCryptPasswordEncoder bCryptPasswordEncoder =new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    //구글로 부터 받은 userRequest 데이터에 대한 후처리되는 함수
    @Override//여기서 후처리가 되어야함.
    //코드는 안받고 바로 accessToken과 사용자의 정보가 userRequest에 바로 리턴이 된다.
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("getClientRegistration: "+userRequest.getClientRegistration()); //registrationId로 어떤 Oauth로 로그인 했는지 확인 가능
        System.out.println("getAccessToken: "+userRequest.getAccessToken().getTokenValue());


        OAuth2User oauth2User = super.loadUser(userRequest);

        //우리가 구글로그인버튼을 클릭-> 구글로그인창->로그인을 완료->code를 리턴(Oauth-Client라이브러리가 받음)->Access토큰 요청
        //여기까지가 userRequest정보->loadUser함수 호출->구글로부터 회원프로필 받아준다.
        System.out.println("getAttributes: "+oauth2User.getAttributes());


        //회원가입을 강제로 진행해볼 예정.
        OAuth2UserInfo oAuth2UserInfo =null;
        if(userRequest.getClientRegistration().getRegistrationId().equals("google")){
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
        }else if(userRequest.getClientRegistration().getRegistrationId().equals("facebook")){
            System.out.println("페이스북 로그인 요청");
            oAuth2UserInfo = new FacebookUserInfo(oauth2User.getAttributes());
        }else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")){
            System.out.println("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map)oauth2User.getAttributes().get("response"));
        }else{
            System.out.println("우리는 구글과 페이스북과 네이버만 지원해요!");
        }
//        String provider=userRequest.getClientRegistration().getRegistrationId(); //google
////        String providerId = oauth2User.getAttribute("sub"); //구글일 경우
//        String providerId = oauth2User.getAttribute("id"); //facebook일 경우
//        String username = provider+"_"+providerId; //google_103823801447068230340
//        String password =bCryptPasswordEncoder.encode("겟인데어");
//        String email = oauth2User.getAttribute("email");
//        String role = "ROLE_USER";

        String provider=oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId(); //facebook일 경우
        String username = provider+"_"+providerId; //google_103823801447068230340
        String password =bCryptPasswordEncoder.encode("겟인데어");
        String email = oAuth2UserInfo.getEmail();
        String role = "ROLE_USER";


        User userEntity = userRepository.findByUsername(username);
        if(userEntity == null){
            System.out.println("로그인이 최초입니다.");
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);

        }else{
            System.out.println("로그인을 이미 한적이 있습니다. 당신은 회원가입이 되어있습니다.");
        }

        //PrincipalOauth2UserService를 만든이유는
        //1.principalDetails로 return 하기위해서
        //2.Oauth로 로그인했을 때 회원가입을 강제로 진행시키기 위해서 한것임.

        //Map<String,Object>
        //Oauth2User 타입은 PrincipalDetails에 상속받았기 때문에 return으로 PrincipalDetails를 해도된다.
        return new PrincipalDetails(userEntity,oauth2User.getAttributes());
    }
}
