package com.greenhill.coop.web;

import com.greenhill.coop.dto.RoundRequest;
import com.greenhill.coop.dto.RoundView;
import com.greenhill.coop.model.OrderRound;
import com.greenhill.coop.service.RoundService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/** Weekly round management - opening, closing and packing. */
@RestController
@RequestMapping("/api/rounds")
public class RoundController {

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @GetMapping
    public List<RoundView> list(HttpSession session) {
        SessionAuth.requireSignedIn(session);
        return roundService.listAll().stream().map(RoundView::from).collect(Collectors.toList());
    }

    @GetMapping("/current")
    public RoundView current(HttpSession session) {
        SessionAuth.requireSignedIn(session);
        OrderRound round = roundService.getCurrentOpenRound();
        return round == null ? null : RoundView.from(round);
    }

    @PostMapping
    public RoundView create(@RequestBody RoundRequest request, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        try {
            LocalDateTime opensAt = LocalDateTime.parse(request.getOpensAt());
            LocalDateTime closesAt = LocalDateTime.parse(request.getClosesAt());
            return RoundView.from(roundService.create(request.getName(), opensAt, closesAt));
        } catch (DateTimeParseException ex) {
            throw new BizException("Dates must use the ISO format yyyy-MM-ddTHH:mm:ss");
        }
    }

    @PostMapping("/{id}/close")
    public RoundView close(@PathVariable Long id, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return RoundView.from(roundService.close(id));
    }

    @PostMapping("/{id}/pack")
    public RoundView pack(@PathVariable Long id, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return RoundView.from(roundService.pack(id));
    }
}
