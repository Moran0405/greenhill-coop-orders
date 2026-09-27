package com.greenhill.coop.web;

import com.greenhill.coop.model.Member;
import com.greenhill.coop.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

/** Sign-in endpoints for members and for the coordinator. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;

    @Value("${app.coordinator.password}")
    private String coordinatorPassword;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    /** Members sign in with the email address the co-op has on file. */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.getOrDefault("email", "");
        Member member = memberService.findByEmail(email);
        SessionAuth.signInMember(session, member);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("role", "member");
        result.put("memberId", member.getId());
        result.put("memberName", member.getName());
        return result;
    }

    /** The coordinator signs in with the shared pass phrase from the configuration. */
    @PostMapping("/coordinator-login")
    public Map<String, Object> coordinatorLogin(@RequestBody Map<String, String> body, HttpSession session) {
        String password = body.getOrDefault("password", "");
        if (!coordinatorPassword.equals(password)) {
            throw new BizException("Incorrect coordinator password.", HttpStatus.UNAUTHORIZED);
        }
        SessionAuth.signInCoordinator(session);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("role", "coordinator");
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        SessionAuth.signOut(session);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("role", null);
        return result;
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        Long memberId = SessionAuth.currentMemberId(session);
        if (memberId != null) {
            result.put("role", "member");
            result.put("memberId", memberId);
            result.put("memberName", session.getAttribute("memberName"));
        } else if (SessionAuth.isCoordinator(session)) {
            result.put("role", "coordinator");
        } else {
            result.put("role", null);
        }
        return result;
    }
}
