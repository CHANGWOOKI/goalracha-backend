package com.goalracha.controller;

import com.goalracha.dto.MemberModifyDTO;
import com.goalracha.dto.MemberDTO;
import com.goalracha.dto.OwnerJoinDTO;
import com.goalracha.dto.OwnerNameModifyDTO;
import com.goalracha.dto.OwnerPwModifyDTO;
import com.goalracha.entity.Member;
import com.goalracha.entity.MemberRole;
import com.goalracha.service.MemberService;
import com.goalracha.service.ReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class memberController {
    private final MemberService memberService;
    private final ReserveService reserveService;

    @PostMapping("/api/member/g/checkid")
    public Map<String, Object> modify(@RequestBody String userid) {
        boolean check = memberService.checkId(userid);
        return Map.of("result", check);
    }

    // 회원 닉네임 중복 확인 API
    @GetMapping("/api/member/g/checkNickname/{nickname}")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@PathVariable String nickname) {
        boolean isNicknameAvailable = memberService.checkNickname(nickname);
        return ResponseEntity.ok(Map.of("available", isNicknameAvailable));
    }

    @PostMapping("/api/member/g/owner")
    public Map<String, Object> joinOwner(@RequestBody OwnerJoinDTO joinDto) {

        Long memberId = memberService.ownerJoin(joinDto);
        return Map.of("result", memberId);
    }
    @GetMapping("/api/member/user")
    public ResponseEntity<List<MemberDTO>> getUserMembers() {
        List<MemberDTO> users = memberService.findMembersByType(MemberRole.USER);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/api/member/owner")
    public ResponseEntity<List<MemberDTO>> getOwnerMembers() {
        List<MemberDTO> owners = memberService.findMembersByType(MemberRole.OWNER);
        return ResponseEntity.ok(owners);
    }

    @PutMapping("/api/member/user/{uNo}")
    public Map<String, String> userModify(@PathVariable(name = "uNo") Long uNo,
                                          @RequestBody MemberModifyDTO memberModifyDTO) {
        memberModifyDTO.setUNo(uNo);
        log.info("Modify." + memberModifyDTO);
        memberService.userModify(memberModifyDTO);

        return Map.of("RESULT", "SUCCESS");
    }

    @PutMapping("/api/member/owner/pw/{uNo}")
    public Map<String, String> ownerPwModify(@PathVariable(name = "uNo") Long uNo,
                                             @RequestBody OwnerPwModifyDTO ownerPwModifyDTO) {
        ownerPwModifyDTO.setUNo(uNo);
        log.info("Modify : " + ownerPwModifyDTO);
        memberService.ownerPwModify(ownerPwModifyDTO);

        return Map.of("RESULT", "SUCCESS");
    }

    @PutMapping("/api/member/owner/{uNo}")
    public Map<String, String> ownerNameModify(@PathVariable(name = "uNo") Long uNo,
                                               @RequestBody OwnerNameModifyDTO ownerNameModifyDTO) {
        ownerNameModifyDTO.setUNo(uNo);
        log.info("Modify : " + ownerNameModifyDTO);
        memberService.ownerNameModify(ownerNameModifyDTO);

        return Map.of("RESULT", "SUCCESS");
    }

    // 회원 탈퇴 API
    @DeleteMapping("/api/member/{uNo}")
    public ResponseEntity<Map<String, String>> withdrawMember(@PathVariable Long uNo) {
        try {
            memberService.withdrawMember(uNo); // 회원 탈퇴 서비스 메서드 호출
            return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "회원 탈퇴에 실패하였습니다."));
        }
    }
}
