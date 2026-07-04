package com.puckzone.ranking.player;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    /** Top N del leaderboard global (usar con PageRequest.of(0, n)). */
    List<Player> findAllByOrderByEloDesc(Pageable pageable);

    /** Jugadores con más ELO que el dado; la posición de un jugador es este conteo + 1. */
    long countByEloGreaterThan(int elo);
}
