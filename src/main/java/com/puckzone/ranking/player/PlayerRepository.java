package com.puckzone.ranking.player;

import com.puckzone.ranking.university.UniversityRankingView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    /** Top N del leaderboard global (usar con PageRequest.of(0, n)). */
    List<Player> findAllByOrderByEloDesc(Pageable pageable);

    /** Jugadores con más ELO que el dado; la posición de un jugador es este conteo + 1. */
    long countByEloGreaterThan(int elo);

    /** Ranking colaborativo: suma del ELO de los estudiantes de cada universidad. */
    @Query("""
            select p.university as university, sum(p.elo) as totalElo, count(p) as playerCount
            from Player p
            group by p.university
            order by sum(p.elo) desc
            """)
    List<UniversityRankingView> findUniversityRanking();
}
