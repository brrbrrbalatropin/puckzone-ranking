package com.puckzone.ranking.match;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MatchRecordRepository extends JpaRepository<MatchRecord, String> {

    /** Últimas partidas de un jugador, ganadas o perdidas (usar con PageRequest). */
    @Query("""
            select m from MatchRecord m
            where m.winnerId = :playerId or m.loserId = :playerId
            order by m.playedAt desc
            """)
    List<MatchRecord> findRecentByPlayer(UUID playerId, Pageable pageable);
}
