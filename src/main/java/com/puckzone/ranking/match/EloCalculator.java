package com.puckzone.ranking.match;

import com.puckzone.ranking.config.RankingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fórmula ELO estándar con factor K configurable (default 30).
 *
 * esperado = 1 / (1 + 10^((eloPerdedor - eloGanador) / 400))
 * delta    = round(K * (1 - esperado)), mínimo 1
 *
 * Entre iguales el delta es ~K/2 (±15). Ganarle a alguien muy superior
 * da casi +K; perderle contra alguien muy superior quita casi nada.
 */
@Component
@RequiredArgsConstructor
public class EloCalculator {

    private final RankingProperties properties;

    /** Puntos que gana el ganador y pierde el perdedor en esta partida. */
    public int computeDelta(int winnerElo, int loserElo) {
        double expected = 1.0 / (1 + Math.pow(10, (loserElo - winnerElo) / 400.0));
        int delta = (int) Math.round(properties.kFactor() * (1 - expected));
        return Math.max(delta, 1);
    }
}
