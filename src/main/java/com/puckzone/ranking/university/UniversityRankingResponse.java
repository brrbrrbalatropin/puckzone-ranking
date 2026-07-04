package com.puckzone.ranking.university;

/**
 * Entrada del ranking colaborativo por universidad que expone el API:
 * posición 1-based, nombre corto de la universidad (ej: "eci"), suma del
 * ELO de sus estudiantes y cuántos estudiantes aportan a esa suma.
 */
public record UniversityRankingResponse(
        long position,
        String university,
        long totalElo,
        long playerCount
) {

    public static UniversityRankingResponse of(UniversityRankingView view, long position) {
        return new UniversityRankingResponse(
                position,
                view.getUniversity(),
                view.getTotalElo(),
                view.getPlayerCount()
        );
    }
}
