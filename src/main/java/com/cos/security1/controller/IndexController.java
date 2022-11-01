package com.cos.security1.controller;

import com.cos.security1.auth.PrincipalDetails;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/test/login")
    public @ResponseBody String testLogin(Authentication authentication
            , @AuthenticationPrincipal PrincipalDetails userDetails){ //DI(의존성 주입)
        System.out.println("/test/login ========================");
        PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal(); //down캐스팅이라는 과정을 거쳐서 user오브젝트를 찾을 수 있다.
        //PrincipalDetails 는 원래 UserDetails로 해야하는데 PrincipalDetails는 UserDetails를 상속받았기 때문에 쓸 수 있다.
        System.out.println("authentication: "+principalDetails.getUser());

        System.out.println("userDetails: "+userDetails.getUser());//Authentication어노테이션을 통해 getuser를 찾을 수 있다.
        return "세션 정보 확인하기";
    }

    @GetMapping("/test/oauth/login")
    public @ResponseBody String testOauthLogin(Authentication authentication
            ,@AuthenticationPrincipal OAuth2User oauth){ //DI(의존성 주입)
        System.out.println("/test/login ========================");
        OAuth2User oauth2User = (OAuth2User)authentication.getPrincipal(); //down캐스팅이라는 과정을 거쳐서 user오브젝트를 찾을 수 있다.
        System.out.println("authentication: "+oauth2User.getAttributes());
        System.out.println("oauth2User: "+oauth.getAttributes());
        return "Oauth 세션 정보 확인하기";
    }
    //정리!
    //스프링 시큐리티는 자신만의 시큐리티 자신의 세션을 가지고 있다.
    //즉 큰세션안에 시큐리티 세션이 따로 있다. 그 안에는 Authenticiation객체 밖에 못들어간다.
    //이 객체덕분에 DI가 가능하다. Authentication(UserDetails type, Oauth2User type만 들어갈 수 있다.)



    @GetMapping({"","/"})
    public String index(){
        //머스테치 기본폴더 src/main/resources/
        //뷰리졸버 설정: templates (prefix), .mustache (suffix) 셍략가능!!
        return "index"; //src/main/resources/templates/index.mustache
    }

    //Oauth로그인을 해도 PrincipalDetails
    //일반 로그인ㅇ르 해도 PrincipalDetails로 받을 수 있다.
    @GetMapping("/user")
    public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails){
        System.out.println("principalDetails: "+principalDetails.getUser());
        return "user";
    }


    @GetMapping("/admin")
    public @ResponseBody String admin(){
        return "admin";
    }

    @GetMapping("/manager")
    public @ResponseBody String manager(){
        return "manager";
    }

    @GetMapping("/loginForm")
    public String loginForm(){
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm(){
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user){
        System.out.println(user);
        user.setRole("ROLE_USER");
        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);
        userRepository.save(user); //회원가입 잘 됨. 이렇게 하면안됨. 비밀번호 :1234 => 시큐리티로 로그인을 할 수 없음.
        //이유는 패스워드가 암호화가 안되었기 때문에! 따라서 위처럼 비밀번호를 암호화해주어야함.

        return "redirect:/loginForm";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    public @ResponseBody String info(){
        return "개인정보";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") //data라는 메서드가 실행되기 직전에 실행이 되는 어노테이션
    //하나를 걸거면 Secured를 사용하면되고 여러개를 걸거면 PreAuthorize를 사용하자!
//    @PostAuthorize()  postauthorize는 메서드가 실행 된 후 에 실행. 근데 잘 쓰지 않음.
    @GetMapping("/data")
    public @ResponseBody String data(){
        return "데이터정보";
    }

}
