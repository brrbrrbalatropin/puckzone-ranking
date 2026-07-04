package com.puckzone.ranking.player;

import com.puckzone.ranking.config.RankingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Consultas del ranking individual: leaderboard global y jugador puntual.
 */
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class PlayerRankingController {

    private final PlayerService playerService;
    private final RankingProperties properties;

    @GetMapping("/global")
    public List<PlayerRankingResponse> getGlobalRanking(
            @RequestParam(required = false) Integer limit) {
        int size = (limit != null && limit > 0) ? limit : properties.defaultLeaderboardSize();
        return playerService.getGlobalRanking(size);
    }

    @GetMapping("/player/{id}")
    public PlayerRankingResponse getPlayer(@PathVariable UUID id) {
        return playerService.getPlayer(id);
    }
}
