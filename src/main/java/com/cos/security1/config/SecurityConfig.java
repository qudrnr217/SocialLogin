package com.cos.security1.config;

//구글 로그인이 완료된 뒤의 후처리가 필요함. 1.코드받기(인증), 2.엑세스토큰(권한),
//3.사용자프로필 정보를 가져오고 4-1.그 정보를 토대로 회원가입을 자동으로 진행시키기도함
//4-2 (이메일, 전화번호, 이름, 아이디) 쇼핑몰 -> (추가적인 집주소 등이 필요), 백화점(Vip 등급, 일반등급 추가필요)



import com.cos.security1.config.Oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity //스프링시큐리티 필터가 스프링 필터체인에 등록이 된다.
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true) //secured 어노테이션 활성화, prePostEnabled = preAuthorize, postAuthorize라는 어노테이션 활성화
public class SecurityConfig{

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    //해당 메서드의 리턴되는 오브젝트를 IoC로 등록해준다.
    @Bean
    public BCryptPasswordEncoder encodePwd(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/user/**").authenticated() //인증만되면 들어갈 수 있는 주소
                .antMatchers("/manager/**").access("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/loginForm")
//                .usernameParameter("username2")
                .loginProcessingUrl("/login") //login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행해줌. 따라서 컨트롤러에 로그인을 만들지 않아도 된다.
                .defaultSuccessUrl("/") //로그인이 성공한다면 메인페이지로 넘어가게 만듦.
                .and()
                .oauth2Login()
                .loginPage("/loginForm")
                .userInfoEndpoint() //구글 로그인이 완료된 뒤의 후처리가 필요함. Tip. Oauth-client 라이브러리를 쓰면 코드X, (액세스토큰 + 사용자프로필정보를 코드없이 바로 받아온다.)
                .userService(principalOauth2UserService);

        return http.build();
    }

    /*
    기존: WebSecurityConfigurerAdapter를 상속하고 configure매소드를 오버라이딩하여 설정하는 방법
    => 현재: SecurityFilterChain을 리턴하는 메소드를 빈에 등록하는 방식(컴포넌트 방식으로 컨테이너가 관리)
    //https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter

    @Override
    protected void configure(HttpSecurity http) throws  Exception{
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/user/**").authenticated()
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
                .antMatchers("/admin").access("\"hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll();
    }

     */
}