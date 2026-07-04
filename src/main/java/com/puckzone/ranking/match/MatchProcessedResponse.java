package com.puckzone.ranking.match;

import java.util.UUID;

/**
 * Respuesta al procesar una partida: el delta aplicado y el ELO resultante
 * de cada jugador, por si game quiere mostrarlo en la pantalla de fin de juego.
 */
public record MatchProcessedResponse(
        UUID winnerId,
        int winnerNewElo,
        UUID loserId,
        int loserNewElo,
        int eloDelta
) {
}
