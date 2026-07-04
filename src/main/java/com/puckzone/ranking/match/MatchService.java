package com.puckzone.ranking.match;

import com.puckzone.ranking.player.Player;
import com.puckzone.ranking.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final PlayerService playerService;
    private final EloCalculator eloCalculator;

    /**
     * Procesa el resultado de una partida en una sola transacción:
     * crea/actualiza a ambos jugadores, calcula el delta ELO y lo aplica.
     * El ranking universitario no necesita actualización explícita porque
     * se calcula agregando el ELO de los jugadores al momento de consultar.
     */
    @Transactional
    public MatchProcessedResponse processMatch(MatchResultRequest request) {
        if (request.winnerId().equals(request.loserId())) {
            throw new IllegalArgumentException("winnerId y loserId no pueden ser el mismo jugador");
        }

        Player winner = playerService.getOrCreate(
                request.winnerId(), request.winnerUsername(), request.winnerUniversity());
        Player loser = playerService.getOrCreate(
                request.loserId(), request.loserUsername(), request.loserUniversity());

        int winnerOldElo = winner.getElo();
        int loserOldElo = loser.getElo();
        int delta = eloCalculator.computeDelta(winnerOldElo, loserOldElo);

        playerService.applyResult(winner, winnerOldElo + delta, true);
        playerService.applyResult(loser, Math.max(loserOldElo - delta, 0), false);

        log.info("Partida procesada: {} ({} -> {}) le ganó {}-{} a {} ({} -> {}), delta {}",
                winner.getId(), winnerOldElo, winner.getElo(),
                request.winnerScore(), request.loserScore(),
                loser.getId(), loserOldElo, loser.getElo(),
                delta);

        return new MatchProcessedResponse(
                winner.getId(), winner.getElo(),
                loser.getId(), loser.getElo(),
                delta);
    }
}
