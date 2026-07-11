package com.puckzone.ranking.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Historial de partidas reportadas por game. La PK es el matchId que creó
 * matchmaking: la idempotencia sale gratis (un retry de game no puede
 * insertar dos veces). Los ids son null en el lado del bot (no tiene cuenta);
 * su username se guarda como "BOT".
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class MatchRecord {

    @Id
    private String matchId;

    /** null si ganó el bot. */
    private UUID winnerId;

    /** null si perdió el bot. */
    private UUID loserId;

    private String winnerUsername;

    private String loserUsername;

    @Column(nullable = false)
    private int winnerScore;

    @Column(nullable = false)
    private int loserScore;

    @Column(nullable = false)
    private boolean vsBot;

    /**
     * Partida amistosa (sala privada entre amigos): humanos reales pero sin
     * efecto en ELO/V-D. El default es imprescindible: la tabla ya está
     * poblada y sin él el ALTER de ddl-auto falla al agregar la columna.
     */
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean friendly;

    /** Delta ELO aplicado; 0 en partidas contra el bot o amistosas. */
    @Column(nullable = false)
    private int eloDelta;

    @Column(nullable = false)
    private Instant playedAt;

    public MatchRecord(String matchId, UUID winnerId, UUID loserId,
                       String winnerUsername, String loserUsername,
                       int winnerScore, int loserScore,
                       boolean vsBot, boolean friendly, int eloDelta) {
        this.matchId = matchId;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.winnerUsername = winnerUsername;
        this.loserUsername = loserUsername;
        this.winnerScore = winnerScore;
        this.loserScore = loserScore;
        this.vsBot = vsBot;
        this.friendly = friendly;
        this.eloDelta = eloDelta;
        this.playedAt = Instant.now();
    }
}
