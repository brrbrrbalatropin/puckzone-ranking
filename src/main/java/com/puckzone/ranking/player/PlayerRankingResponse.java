package com.puckzone.ranking.player;

/**
 * Vista pública de un jugador en el ranking: se usa tanto para las entradas
 * del leaderboard global como para la consulta individual por id.
 * {@code position} es null para jugadores sin partidas humanas (solo bot):
 * existen pero no están rankeados.
 * <p>
 * NO expone el id del jugador a propósito: el leaderboard es una lista de
 * TODOS los jugadores, y devolver ahí el UUID publicaba el identificador
 * interno (el subject del JWT) de cada uno a cualquiera con una cuenta. El
 * cliente identifica su propia fila por {@code username}, que auth garantiza
 * único. Quien consulta /player/{id} ya conoce el id que preguntó.
 */
public record PlayerRankingResponse(
        Long position,
        String username,
        String university,
        int elo,
        int wins,
        int losses
) {

    public static PlayerRankingResponse of(Player player, Long position) {
        return new PlayerRankingResponse(
                position,
                player.getUsername(),
                player.getUniversity(),
                player.getElo(),
                player.getWins(),
                player.getLosses()
        );
    }
}
