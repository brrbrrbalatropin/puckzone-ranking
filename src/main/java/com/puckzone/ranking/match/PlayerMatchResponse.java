package com.puckzone.ranking.match;

import java.time.Instant;
import java.util.UUID;

/**
 * Una partida del historial vista desde el lado de un jugador: contra quién
 * jugó, si ganó, el marcador desde su perspectiva y cuánto ELO se movió
 * (con signo; 0 en partidas contra el bot y amistosas de sala privada).
 */
public record PlayerMatchResponse(
        String matchId,
        String opponentUsername,
        boolean vsBot,
        boolean friendly,
        boolean won,
        int myScore,
        int rivalScore,
        int eloChange,
        Instant playedAt
) {

    public static PlayerMatchResponse of(MatchRecord match, UUID playerId) {
        boolean won = playerId.equals(match.getWinnerId());
        int eloChange = 0;
        if (!match.isVsBot() && !match.isFriendly()) {
            eloChange = won ? match.getEloDelta() : -match.getEloDelta();
        }
        return new PlayerMatchResponse(
                match.getMatchId(),
                won ? match.getLoserUsername() : match.getWinnerUsername(),
                match.isVsBot(),
                match.isFriendly(),
                won,
                won ? match.getWinnerScore() : match.getLoserScore(),
                won ? match.getLoserScore() : match.getWinnerScore(),
                eloChange,
                match.getPlayedAt()
        );
    }
}
