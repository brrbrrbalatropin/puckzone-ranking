package com.puckzone.ranking.player;

import java.util.UUID;

/**
 * Vista pública de un jugador en el ranking: se usa tanto para las entradas
 * del leaderboard global como para la consulta individual por id.
 * {@code position} es null para jugadores sin partidas humanas (solo bot):
 * existen pero no están rankeados.
 */
public record PlayerRankingResponse(
        Long position,
        UUID id,
        String username,
        String university,
        int elo,
        int wins,
        int losses
) {

    public static PlayerRankingResponse of(Player player, Long position) {
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
