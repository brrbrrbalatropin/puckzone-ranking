package com.puckzone.ranking.player;

import java.util.UUID;

/**
 * Vista pública de un jugador en el ranking: se usa tanto para las entradas
 * del leaderboard global como para la consulta individual por id.
 */
public record PlayerRankingResponse(
        long position,
        UUID id,
        String username,
        String university,
        int elo,
        int wins,
        int losses
) {

    public static PlayerRankingResponse of(Player player, long position) {
        return new PlayerRankingResponse(
                position,
                player.getId(),
                player.getUsername(),
                player.getUniversity(),
                player.getElo(),
                player.getWins(),
                player.getLosses()
        );
    }
}
