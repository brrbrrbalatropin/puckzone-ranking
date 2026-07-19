package com.puckzone.ranking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parámetros del sistema de ranking, configurables desde application.yaml
 * bajo el prefijo puckzone.ranking.
 *
 * @param initialElo             ELO con el que arranca cada jugador nuevo.
 * @param kFactor                factor K de la fórmula ELO (máximo que se puede ganar/perder por partida).
 * @param defaultLeaderboardSize cantidad de jugadores que devuelve el leaderboard global por defecto.
 */
@ConfigurationProperties(prefix = "puckzone.ranking")
public record RankingProperties(
        int initialElo,
        int kFactor,
        int defaultLeaderboardSize
) {
}
