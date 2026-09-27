package com.greenhill.coop.web;

import com.greenhill.coop.dto.MemberRequest;
import com.greenhill.coop.model.Member;
import com.greenhill.coop.service.MemberService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

/** Member administration - visible to the coordinator only. */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<Member> list(HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return memberService.listAll();
    }

    @GetMapping("/{id}")
    public Member get(@PathVariable Long id, HttpSession session) {
        // A member may look at their own record; the coordinator may look at any.
        if (!SessionAuth.isCoordinator(session)
                && !id.equals(SessionAuth.currentMemberId(session))) {
            throw new BizException("Members can only view their own details.",
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
        return memberService.get(id);
    }

    @PostMapping
    public Member create(@Valid @RequestBody MemberRequest request, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return memberService.register(request.getName(), request.getEmail(), request.getPhone());
    }
}
