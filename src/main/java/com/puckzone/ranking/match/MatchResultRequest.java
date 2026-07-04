package com.puckzone.ranking.match;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/**
 * Payload que envía puckzone-game al terminar una partida (POST /api/ranking/match).
 * Los usernames son opcionales: game los agregará cuando amplíe el contrato
 * (los tendrá del JWT del handshake WS); mientras lleguen null el servicio
 * funciona igual, solo que el leaderboard muestra el UUID sin nombre.
 */
public record MatchResultRequest(

        @NotNull(message = "winnerId es obligatorio")
        UUID winnerId,

        @NotNull(message = "loserId es obligatorio")
        UUID loserId,

        String winnerUsername,

        String loserUsername,

        @NotBlank(message = "winnerUniversity es obligatoria")
        String winnerUniversity,

        @NotBlank(message = "loserUniversity es obligatoria")
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
