package com.cos.security1.auth;

//시큐리티가 /login을 주소 요청이 오면 낚아채서 로그인을 진행시킨다.
//로그인을 진행이 완료가 되면 시큐리티 session을 만들어줍니다. (Security ContextHolder)라는 키값에다가
//세션정보를 저장시킨다.
//오브젝트 타입=> Authentication 타입의 객체만 들어갈 수 있다.
//Authentication 안에 User 정보가 있어야 됨.
//User 오브젝트 타입 => UserDetails 타입 객체

//Security Session => Authentication => UserDetails(PrincipalDetails)

import com.cos.security1.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user; //콤포지션
    private Map<String,Object> attributes;
    //생성자
    //일반 로그인 생성자
    public PrincipalDetails(User user){
        this.user=user;
    }

    //Oauth로그인 생성자
    public PrincipalDetails(User user, Map<String,Object>attributes){
        this.user=user;
        this.attributes=attributes;
    }



    //해당 유저의 권한을 리턴하는 곳!
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    //계정이 만료 되었는지.
    @Override
    public boolean isAccountNonExpired() {
        return true; //아니오가 true임. 즉 계정이 만료가 안됨.
    }

    //너의 계정이 잠겼니?
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //너의 계정의 비밀번호가 1년이 지났니? 즉 비밀번호가 너무 오래사용한거 아니니?
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //너의 계정이 활성화 되어있니?
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    //아니오는 true
    //언제 false하냐 ? 우리사이트에서 1년동안 회원이 로그인을 안하면 휴면 계정으로 하기
    //로 함. 그러면 user 모델에 private TimeStamp loginDate라는 것을 만들어서 해야한다.
    //user.getLoginDate()를 가져와서 현재시간 - 로그인 시간 => 1년을 초과하면 return을 false로한다.
    //지금은 아직 하지 않기 때문에 모델에도 등록하지않고 여기서도 처리하지 않아서 true로 놔둔다.
}
