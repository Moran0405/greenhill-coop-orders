package com.greenhill.coop.web;

import com.greenhill.coop.model.Member;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpSession;

/**
 * Very light session based sign-in, appropriate for a small internal
 * tool run by volunteers. The session stores either the signed-in
 * member id or the coordinator flag.
 */
public final class SessionAuth {

    public static final String MEMBER_ID = "memberId";
    public static final String COORDINATOR = "coordinator";

    private SessionAuth() {
    }

    public static void signInMember(HttpSession session, Member member) {
        session.setAttribute(MEMBER_ID, member.getId());
        session.setAttribute("memberName", member.getName());
        session.removeAttribute(COORDINATOR);
    }

    public static void signInCoordinator(HttpSession session) {
        session.setAttribute(COORDINATOR, true);
        session.removeAttribute(MEMBER_ID);
    }

    public static void signOut(HttpSession session) {
        session.invalidate();
    }

    public static Long currentMemberId(HttpSession session) {
        Object id = session.getAttribute(MEMBER_ID);
        return id == null ? null : (Long) id;
    }

    public static boolean isCoordinator(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(COORDINATOR));
    }

    public static void requireSignedIn(HttpSession session) {
        if (currentMemberId(session) == null && !isCoordinator(session)) {
            throw new BizException("Please sign in first.", HttpStatus.UNAUTHORIZED);
        }
    }

    public static Long requireMember(HttpSession session) {
        Long id = currentMemberId(session);
        if (id == null) {
            throw new BizException("Please sign in as a member.", HttpStatus.UNAUTHORIZED);
        }
        return id;
    }

    public static void requireCoordinator(HttpSession session) {
        if (!isCoordinator(session)) {
            throw new BizException("Coordinator access required.", HttpStatus.FORBIDDEN);
        }
    }
}
