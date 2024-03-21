package com.goalracha.controller;

import com.goalracha.dto.MemberDTO;
import com.goalracha.dto.PageRequestDTO;
import com.goalracha.dto.PageResponseDTO;
import com.goalracha.dto.reserve.*;
import com.goalracha.entity.Reserve;
import com.goalracha.repository.GroundRepository;
import com.goalracha.repository.ReserveRepository;
import com.goalracha.service.ReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/reserve")
public class ReserveController {

    private final ReserveService reserveService;
    private final ReserveRepository reserveRepository;
    private final GroundRepository groundRepository;

    //예약 전체목록(테스트)
    @GetMapping("/v/")
    public ResponseEntity<?> getList() {
        List<ReserveListDTO> list = reserveService.getList();
        return ResponseEntity.ok("list");
    }

    @PostMapping("/v/date/")
    public ResponseEntity<?> getListondate(@RequestBody Map<String, Object> request) {

        Map<String, Object> response = reserveService.getAllList((String) request.get("date"), (String) request.get("time"), (List<String>) request.get("inout"), (String) request.get("search"));

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/v/ground/{gno}/{date}")
    public ResponseEntity<?> showGroundInfo(@PathVariable Long gno, @PathVariable String date) {
        log.info(date);
        Map<String, Object> response = reserveService.showGroundInfo(gno, date);

        return ResponseEntity.ok().body(response);
    }

//    @PostMapping("/v/")
//    public ResponseEntity<?> newReserve(@RequestBody ReservDTO reservDTO) {
//        log.info("reserveDTO :: " + reservDTO);
//
//        Reserve reserve = reserveService.newReserve(reservDTO);
//        if (reserve == null) {
//            return ResponseEntity.badRequest().body("error");
//        }
//        log.info(reserve.getRNO());
//        return ResponseEntity.ok(reserve.getRNO());
//    }

    @PostMapping("/") //예약등록
    public ResponseEntity<?> addReserve(@RequestBody ReserveAddsDTO reserveAddsdto) {

        Map<String, Object> response = reserveService.newReserve(reserveAddsdto);
        if (response == null) {
            return ResponseEntity.badRequest().body("error");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> addReserve(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info(authentication.getPrincipal());
        log.info(authentication.getPrincipal().toString());

        if (authentication.isAuthenticated() && authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.badRequest().body("잘못된 권한의 요청");
        }
        MemberDTO mdto = (MemberDTO) authentication.getPrincipal();
        log.info(mdto.toString());

        Map<String, Object> response = reserveService.cancel((String) request.get("header"), (String) request.get("payKey"),mdto.getUNo());
        if (response == null) {
            return ResponseEntity.badRequest().body("error");
        }
        return ResponseEntity.ok(response);
    }
    @PostMapping("/info")
    public ResponseEntity<?> getbyPayKey(@RequestBody String payKey) {
        Map<String, Object> response = reserveService.infoByPayKey(payKey);
        if(response.containsKey("error")) {
            return ResponseEntity.badRequest().body((String) response.get("error"));
        }

        return ResponseEntity.ok(response);
    }

    //예약번호로 그정보 가져오기
    @GetMapping("/v/{rno}")
    public ResponseEntity<?> getOne(@PathVariable Long rno) {
        Reserve reserve = reserveService.getOne(rno);
        log.info(rno);
        return ResponseEntity.ok(reserve.getRNO());
    }

    //구장번호로 예약목록 가져오기
    @GetMapping("/v/ground/{gno}")
    public ResponseEntity<?> getGroundReserve(@PathVariable Long gno) {

        ReserveListDTO dto = reserveService.getGroundReserve(gno);

        return ResponseEntity.ok(dto);
    }


    // user 이전 예약 목록 조회
    @GetMapping("/v/previous-reservations/{uNo}")
    public ResponseEntity<PageResponseDTO<UserReserveListDTO>> getUserPreviousReservations(
            @PathVariable Long uNo, @PageableDefault(size = 10) PageRequestDTO pageRequestDTO) {

        // 사용자의 이전 예약을 가져옵니다.
        PageResponseDTO<UserReserveListDTO> responseDTO = reserveService.getUserPreviousReservations(uNo, pageRequestDTO);

        // ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(responseDTO);
    }

    // 유저 번호로 예약현황 목록 조회
    @GetMapping("/v/reservation-status/{uNo}")
    public ResponseEntity<PageResponseDTO<UserReserveListDTO>> getUserReservationStatus(
            @PathVariable Long uNo,
            @PageableDefault(size = 10) PageRequestDTO pageRequestDTO) {
        // 사용자의 예약 현황을 가져옵니다.
        PageResponseDTO<UserReserveListDTO> responseDTO = reserveService.getUserReservationStatus(uNo, pageRequestDTO);
        // ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/v/test/{uNo}")
    public ResponseEntity<?> getUserReserveList(
            @PathVariable Long uNo,
            @PageableDefault(size = 10) PageRequestDTO pageRequestDTO) {
        // 사용자의 예약 현황을 가져옵니다.

        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1, // 1페이지가 0이므로 주의
                pageRequestDTO.getSize(),
                Sort.by("reserveDate").descending()
        );
        // 사용자의 예약 현황을 가져옵니다.
        Page<Object[]> page = reserveRepository.userReserveList(uNo, pageable);

        // 가져온 예약 현황을 PageResponseDTO 형식으로 변환하여 반환합니다.
        PageResponseDTO<Object[]> result = PageResponseDTO.<Object[]>withAll()
                .dtoList(page.getContent())
                .pageRequestDTO(PageRequestDTO.builder()
                        .page(page.getNumber() + 1)
                        .size(page.getSize())
                        .build())
                .totalCount(page.getTotalElements())
                .build();

        // ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(result);
    }


    // 구장 유저 번호로 예약 목록 조회
    @GetMapping("/v/owner-list/{uNo}")
    public ResponseEntity<PageResponseDTO<OwnerReserveListDTO>> getOwnerReserveList(
            @PathVariable Long uNo,
            @PageableDefault(size = 10) PageRequestDTO pageRequestDTO) {
        // 해당 사용자의 예약 리스트를 가져옵니다.
        PageResponseDTO<OwnerReserveListDTO> responseDTO = reserveService.getOwnerReserveList(uNo, pageRequestDTO);
        // ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(responseDTO);
    }


    // 예약 전체 리스트(관리자)
    @GetMapping("/v/list")
    public ResponseEntity<PageResponseDTO<AdminReserveListDTO>> getAllReserveList(
            @PageableDefault(size = 10) PageRequestDTO pageRequestDTO) {
        // 모든 예약 리스트를 가져옵니다.
        PageResponseDTO<AdminReserveListDTO> responseDTO = reserveService.getAllReserveList(pageRequestDTO);
        // ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(responseDTO);
    }

    // 사업자 예약 리스트 검색 (구장명, 고객명)
    @GetMapping("/v/owner-list/{uNo}/{searchName}")
    public ResponseEntity<PageResponseDTO<OwnerReserveListDTO>> getOwnerReserveListSearch(
            @PathVariable Long uNo,
            @PathVariable String searchName,
            @PageableDefault(size = 10) PageRequestDTO pageRequestDTO) {

        // 검색된 예약 리스트를 가져옵니다.
        PageResponseDTO<OwnerReserveListDTO> responseDTO = reserveService.getOwnerReserveListSearch(uNo, searchName, pageRequestDTO);

        // ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(responseDTO);
    }

    // 관리자 예약 리스트 검색 (구장명, 고객명, 사업자명)
    @GetMapping("/v/list/{searchName}")
    public ResponseEntity<PageResponseDTO<AdminReserveListDTO>> getAllReserveListSearch(
            @PathVariable String searchName,
            @PageableDefault(size = 10) PageRequestDTO pageRequestDTO) {

        // 검색된 예약 리스트를 가져옵니다.
        PageResponseDTO<AdminReserveListDTO> responseDTO = reserveService.getAllReserveListSearch(searchName, pageRequestDTO);

        // ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(responseDTO);
    }


}
