package com.puckzone.ranking.match;

import com.puckzone.ranking.player.Player;
import com.puckzone.ranking.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private static final String BOT_NAME = "BOT";

    private final PlayerService playerService;
    private final EloCalculator eloCalculator;
    private final MatchRecordRepository matchRecordRepository;

    /**
     * Procesa el resultado de una partida en una sola transacción.
     * Idempotente por matchId: un retry de game responde lo ya aplicado sin
     * volver a mover nada. Partidas contra el bot: crean al jugador si no
     * existía (queda con el ELO inicial) y dejan registro en el historial con
     * delta 0, pero NO tocan ELO ni contadores de victorias/derrotas — regla
     * de negocio: el bot no afecta el ranking. El ranking universitario no
     * necesita actualización explícita porque se agrega al consultar.
     */
    @Transactional
    public MatchProcessedResponse processMatch(MatchResultRequest request) {
        var existing = matchRecordRepository.findById(request.matchId());
        if (existing.isPresent()) {
            log.info("Partida {} ya estaba procesada: retry ignorado", request.matchId());
            MatchRecord record = existing.get();
            return new MatchProcessedResponse(record.getWinnerId(), 0,
                    record.getLoserId(), 0, record.getEloDelta());
        }
        return request.vsBot() ? processBotMatch(request) : processHumanMatch(request);
    }

    private MatchProcessedResponse processHumanMatch(MatchResultRequest request) {
        if (request.winnerId() == null || request.loserId() == null) {
            throw new IllegalArgumentException("winnerId y loserId son obligatorios entre humanos");
        }
        if (request.winnerId().equals(request.loserId())) {
            throw new IllegalArgumentException("winnerId y loserId no pueden ser el mismo jugador");
        }
        if (isBlank(request.winnerUniversity()) || isBlank(request.loserUniversity())) {
            throw new IllegalArgumentException("winnerUniversity y loserUniversity son obligatorias entre humanos");
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

        matchRecordRepository.save(new MatchRecord(request.matchId(),
                winner.getId(), loser.getId(),
                winner.getUsername(), loser.getUsername(),
                request.winnerScore(), request.loserScore(),
                false, delta));

        log.info("Partida {} procesada: {} ({} -> {}) le ganó {}-{} a {} ({} -> {}), delta {}",
                request.matchId(),
                winner.getId(), winnerOldElo, winner.getElo(),
                request.winnerScore(), request.loserScore(),
                loser.getId(), loserOldElo, loser.getElo(),
                delta);

        return new MatchProcessedResponse(
                winner.getId(), winner.getElo(),
                loser.getId(), loser.getElo(),
                delta);
    }

    private MatchProcessedResponse processBotMatch(MatchResultRequest request) {
        boolean humanWon = request.winnerId() != null;
        if (humanWon == (request.loserId() != null)) {
            throw new IllegalArgumentException(
                    "en partidas vs bot exactamente uno de winnerId/loserId debe ser el humano");
        }
        UUID humanId = humanWon ? request.winnerId() : request.loserId();
        String humanUsername = humanWon ? request.winnerUsername() : request.loserUsername();
        String humanUniversity = humanWon ? request.winnerUniversity() : request.loserUniversity();
        if (isBlank(humanUniversity)) {
            throw new IllegalArgumentException("la universidad del humano es obligatoria");
        }

        // Registra al jugador si es su primera partida (ELO inicial), pero sin
        // aplicar resultado: vs bot no hay delta ni victoria/derrota que contar.
        Player human = playerService.getOrCreate(humanId, humanUsername, humanUniversity);

        matchRecordRepository.save(new MatchRecord(request.matchId(),
                humanWon ? humanId : null, humanWon ? null : humanId,
                humanWon ? human.getUsername() : BOT_NAME,
                humanWon ? BOT_NAME : human.getUsername(),
                request.winnerScore(), request.loserScore(),
                true, 0));

        log.info("Partida {} vs BOT registrada: {} {} {}-{} (sin efecto en ELO)",
                request.matchId(), human.getId(), humanWon ? "ganó" : "perdió",
                request.winnerScore(), request.loserScore());

        return new MatchProcessedResponse(
                request.winnerId(), humanWon ? human.getElo() : 0,
                request.loserId(), humanWon ? 0 : human.getElo(),
                0);
    }

    /** Últimas partidas de un jugador, vistas desde su lado. */
    @Transactional(readOnly = true)
    public List<PlayerMatchResponse> getPlayerMatches(UUID playerId, int limit) {
        return matchRecordRepository.findRecentByPlayer(playerId, PageRequest.of(0, limit))
                .stream()
                .map(record -> PlayerMatchResponse.of(record, playerId))
                .toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
