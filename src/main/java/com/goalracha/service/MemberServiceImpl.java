package com.goalracha.service;

import com.goalracha.dto.MemberModifyDTO;
import com.goalracha.entity.Member;
import com.goalracha.entity.MemberRole;
import com.goalracha.dto.MemberDTO;
import com.goalracha.dto.MemberJoinDTO;
import com.goalracha.dto.OwnerJoinDTO;
import com.goalracha.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberDTO getKakaoMember(String accessToken) {
        String email = getEmailFromKakaoAccessToken(accessToken);
        log.info("email: " + email);
        Member result = memberRepository.findByEmail(email);
        // 기존의 회원
        if (result != null) {
            MemberDTO memberDTO = MemberDTO.entityToDTO(result);
            return memberDTO;
        }
        log.info("유저 생성합니다");

        Member member = createKkoMember(email);
        memberRepository.save(member);
        log.info(member.toString());
        MemberDTO memberDTO = new MemberDTO(
                member.getUNo(),
                member.getUserId(),
                member.getPw(),
                member.getNickname(),
                member.getName(),
                member.getTel(),
                member.getCreateDate(),
                member.getEmail(),
                member.getBusinessName(),
                member.getBusinessId(),
                member.getState(),
                member.getType());
        return memberDTO;
    }


    @Override
    public void userModify(MemberModifyDTO memberModifyDTO) {
        // 회원 번호로 회원을 조회합니다.
        Optional<Member> result = memberRepository.findById(memberModifyDTO.getUNo());

        // 조회된 회원이 존재하지 않으면 null을 반환합니다.
        Member member = result.orElse(null);

        // 조회된 회원이 없으면 메서드를 종료합니다.
        if (member == null) {
            return;
        }

        // 회원 정보를 수정합니다.
        member.userModify(memberModifyDTO.getUNo(), memberModifyDTO.getNickname(), memberModifyDTO.getTel());

        // 수정된 회원 정보를 저장합니다.
        memberRepository.save(member);
    }

    @Override
    public void modifyMember(MemberJoinDTO memberJoinDTO) {
        Member member = memberRepository.findByEmail(memberJoinDTO.getEmail());
//        MemberDTO edit = MemberDTO.entityToDTO(member);
//        edit.setName(memberJoinDTO.getName());
//        edit.setNickname(memberJoinDTO.getNickname());
//        edit.setTel(memberJoinDTO.getTel());

        member.joinMember(memberJoinDTO.getUNo(), memberJoinDTO.getName(), memberJoinDTO.getNickname(), memberJoinDTO.getTel());


        log.info("member info::" + member.toString());
        memberRepository.save(member);
    }

    @Override
    public boolean checkId(String userid) {
        log.info(userid);
        Member member = memberRepository.findByUserId(userid);
        if (member == null) {
            return false;
        }

        return true;
    }

    @Override
    public Long ownerJoin(OwnerJoinDTO joinDto) {
        Member member = Member.builder()
                .type(MemberRole.OWNER)
                .name(joinDto.getName())
                .email(joinDto.getEmail())
                .tel(joinDto.getTel())
                .businessId(joinDto.getBusiness_id())
                .businessName(joinDto.getBusiness_name())
                .userId(joinDto.getId())
                .pw(passwordEncoder.encode(joinDto.getPw()))
                .createDate(new Date())
                .build();
        Member result = memberRepository.save(member);
        log.info("user 생성 : :" + result.toString());
        return result.getUNo();
    }


    private Member createKkoMember(String email) { //카카오 유저 빌더

        Member member = Member.builder().email(email).userId(email).nickname(email).pw("null").type(MemberRole.USER).createDate(new Date()).state(1).build();
        return member;
    }

    private String getEmailFromKakaoAccessToken(String accessToken) { //카카오토큰으로 유저 이메일 리턴
        String kakaoGetUserURL = "https://kapi.kakao.com/v2/user/me";
        if (accessToken == null) {
            throw new RuntimeException("Access Token is null");
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(kakaoGetUserURL).build();
        ResponseEntity<LinkedHashMap> response = restTemplate.exchange(uriBuilder.toString(), HttpMethod.GET, entity,
                LinkedHashMap.class);
        log.info(response);
        LinkedHashMap<String, LinkedHashMap> bodyMap = response.getBody();
        log.info("------------------------------------");
        log.info(bodyMap);
        LinkedHashMap<String, String> kakaoAccount = bodyMap.get("kakao_account");
        log.info("kakaoAccount: " + kakaoAccount);
        return kakaoAccount.get("email");
    }

}
