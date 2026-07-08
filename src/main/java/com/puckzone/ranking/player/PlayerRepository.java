package com.puckzone.ranking.player;

import com.puckzone.ranking.university.UniversityRankingView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Los leaderboards (global, posición y universidades) solo cuentan jugadores
 * con partidas HUMANAS (wins+losses > 0): quien solo ha jugado contra el bot
 * existe (para su perfil) pero no aparece ni suma a su universidad — regla de
 * negocio: el bot no afecta el ranking.
 */
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    /** Top N del leaderboard global (usar con PageRequest.of(0, n)). */
    @Query("""
            select p from Player p
            where p.wins > 0 or p.losses > 0
            order by p.elo desc
            """)
    List<Player> findLeaderboard(Pageable pageable);

    /** Jugadores rankeados con más ELO que el dado; la posición es este conteo + 1. */
    @Query("""
            select count(p) from Player p
            where (p.wins > 0 or p.losses > 0) and p.elo > :elo
            """)
    long countRankedWithEloGreaterThan(int elo);

    /** Ranking colaborativo: suma del ELO de los estudiantes de cada universidad. */
    @Query("""
            select p.university as university, sum(p.elo) as totalElo, count(p) as playerCount
            from Player p
            where p.wins > 0 or p.losses > 0
            group by p.university
            order by sum(p.elo) desc
            """)
    List<UniversityRankingView> findUniversityRanking();
}
