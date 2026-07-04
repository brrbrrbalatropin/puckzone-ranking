package com.puckzone.ranking.university;

/**
 * Proyección del GROUP BY sobre la tabla players: el puntaje de una
 * universidad es la suma del ELO de todos sus estudiantes registrados.
 * Al calcularse en la consulta, nunca puede quedar desincronizado.
 */
public interface UniversityRankingView {

    String getUniversity();

    long getTotalElo();

    long getPlayerCount();
}
