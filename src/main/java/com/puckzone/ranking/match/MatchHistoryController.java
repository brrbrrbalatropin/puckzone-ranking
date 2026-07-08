package com.puckzone.ranking.match;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Historial de partidas de un jugador (lo consume la pantalla de perfil).
 * Incluye las partidas contra el bot, que aparecen con eloChange 0.
 */
@RestController
@RequestMapping("/api/ranking/player/{id}/matches")
@RequiredArgsConstructor
public class MatchHistoryController {

    private static final int DEFAULT_LIMIT = 20;

    private final MatchService matchService;

    @GetMapping
    public List<PlayerMatchResponse> getPlayerMatches(@PathVariable UUID id,
                                                      @RequestParam(required = false) Integer limit) {
        int size = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
        return matchService.getPlayerMatches(id, size);
    }
}
