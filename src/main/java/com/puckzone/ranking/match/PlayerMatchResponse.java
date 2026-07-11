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

    public static PlayerMatchResponse of(MatchRecord record, UUID playerId) {
        boolean won = playerId.equals(record.getWinnerId());
        return new PlayerMatchResponse(
                record.getMatchId(),
                won ? record.getLoserUsername() : record.getWinnerUsername(),
                record.isVsBot(),
                record.isFriendly(),
                won,
                won ? record.getWinnerScore() : record.getLoserScore(),
                won ? record.getLoserScore() : record.getWinnerScore(),
                record.isVsBot() || record.isFriendly() ? 0 : (won ? record.getEloDelta() : -record.getEloDelta()),
                record.getPlayedAt()
        );
    }
}
