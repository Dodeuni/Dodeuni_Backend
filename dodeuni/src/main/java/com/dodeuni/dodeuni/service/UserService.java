package com.dodeuni.dodeuni.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dodeuni.dodeuni.domain.user.User;
import com.dodeuni.dodeuni.domain.user.UserRepository;
import com.dodeuni.dodeuni.web.dto.user.TokenDto;
import com.dodeuni.dodeuni.web.dto.user.UserResponseDto;
import com.dodeuni.dodeuni.web.dto.user.UserSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    public String secret;

    @Value("${jwt.expiration_time}")
    public int expiration_time;
    
    public TokenDto login(UserSaveRequestDto userSaveRequestDto) {

        if(validateDuplicatedUser(userSaveRequestDto.getEmail())) {
            User user = userRepository.findByEmail(userSaveRequestDto.getEmail());
            return createToken(user);
        }
        else {
            User user = userRepository.save(userSaveRequestDto.toEntity());
            return createToken(user);
        }
    }

    public UserResponseDto getProfile(Long id) {

        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));
        return new UserResponseDto(user);
    }

    public UserResponseDto updateNickname(Long id, String newNickname) {

        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));
        user.updateProfile(newNickname);
        return new UserResponseDto(user);
    }

   public Boolean validateDuplicatedUser(String email) {

        return userRepository.countByEmail(email) > 0;
    }

    public TokenDto createToken(User user) {
        String jwtToken = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis()+ expiration_time))
                .withClaim("id", user.getId())
                .withClaim("role", "회원")
                .sign(Algorithm.HMAC512(secret));

        return new TokenDto(user.getId(), user.getEmail(), user.getNickname(), jwtToken);
    }
}
