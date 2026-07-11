package com.puckzone.ranking.match;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/**
 * Payload que envía puckzone-game al terminar una partida (POST /api/ranking/match).
 *
 * <p>{@code matchId} es la clave de idempotencia: si llega dos veces (retry de
 * game), la segunda no reprocesa nada.
 *
 * <p>Si {@code vsBot} es true, el lado del bot llega con id/universidad null
 * (el bot no tiene cuenta) — por eso los ids y universidades no llevan
 * {@code @NotNull} aquí: la validación condicional vive en {@link MatchService}.
 * Las partidas contra el bot NO mueven ELO ni contadores: solo dejan registro
 * en el historial con delta 0.
 *
 * <p>Si {@code friendly} es true (sala privada entre amigos), la partida es
 * entre dos humanos reales pero tampoco mueve ELO ni contadores: queda en el
 * historial de ambos con delta 0. Los reportes viejos no traen el campo y
 * caen a false (partida rankeada normal).
 */
public record MatchResultRequest(

        @NotBlank(message = "matchId es obligatorio")
        String matchId,

        boolean vsBot,

        boolean friendly,

        /** null solo cuando vsBot y ganó el bot. */
        UUID winnerId,

        /** null solo cuando vsBot y perdió el bot. */
        UUID loserId,

        String winnerUsername,

        String loserUsername,

        String winnerUniversity,

        String loserUniversity,

        @Min(value = 7, message = "las partidas se ganan a 7 goles")
        @Max(value = 7, message = "las partidas se ganan a 7 goles")
        int winnerScore,

        @Min(value = 0, message = "loserScore debe estar entre 0 y 6")
        @Max(value = 6, message = "loserScore debe estar entre 0 y 6")
        int loserScore,

        @PositiveOrZero(message = "gameDuration no puede ser negativa")
        long gameDuration
) {
}
