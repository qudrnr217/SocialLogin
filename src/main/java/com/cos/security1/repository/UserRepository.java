package com.cos.security1.repository;

import com.cos.security1.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

//CRUD 함수를 JpaRepository가 들고 있음.
//@Repository 어노테이션이 없어도 IoC가 됨. 이유는 JpaRepository를 상속했기 때문에 가능함.
//즉 자동으로 bean으로 등록이 된다.
public interface UserRepository extends JpaRepository<User,Integer> {

    //findBy 규칙 -> Username 문법
    //Select * from user where username = 1? 이게 호출이 된다.
    public User findByUsername(String username); //Jpa Query methods라고 검색해보면 알 수 있다.

    //select * from user where email=?
//    public User findByEmail();
}
