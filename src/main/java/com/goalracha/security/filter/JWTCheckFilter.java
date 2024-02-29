package com.goalracha.security.filter;

import com.goalracha.dto.MemberDTO;
import com.goalracha.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("-------------------- JWTCheckFilter -------------------");
        String authHeaderStr = request.getHeader("Authorization");
        try {
            // Bearer accestoken...
            String accessToken = authHeaderStr.substring(7);
            Map<String, Object> claims = JWTUtil.validateToken(accessToken);
            log.info("JWT claims: " + claims);

            MemberDTO memberDTO = MemberDTO.fromClaims(claims);
//            Long uNo = Long.parseLong(String.valueOf(claims.get("uNo"))); //claims.get("uNo")가 Integer로 반환해서 재반환
//
//            String userId = (String) claims.get("userId");
//            String pw = (String) claims.get("pw");
//
//            String typeStr = (String) claims.get("type");
//            MemberRole type = MemberRole.valueOf(typeStr); //claims.get("type")이 string으로 반환되서 재봔환
//
//            MemberDTO memberDTO = new MemberDTO(uNo, userId,pw,type);
            log.info("-----------------------------------");
            log.info(memberDTO);
            log.info(memberDTO.getAuthorities());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberDTO,
                    memberDTO.getPw(), memberDTO.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT Check Error..............");
            log.error(e.getMessage());
            Gson gson = new Gson();
            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));
            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Preflight 요청은 체크하지 않음
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        String path = request.getRequestURI();
        log.info("check uri.............." + path);
        // api/member/ 경로의 호출은 체크하지 않음
        if (path.startsWith("/api/member/")) {
            return true;
        }
        // 이미지 조회 경로는 체크하지 않는다면
        if (path.startsWith("/api/products/view/")) {
            return true;
        }
        return false;
    }
}