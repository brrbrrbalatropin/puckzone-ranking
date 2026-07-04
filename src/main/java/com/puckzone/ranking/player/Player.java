package com.puckzone.ranking.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de ranking de un jugador. La PK es el mismo UUID que emite
 * puckzone-auth, de modo que todos los servicios identifican al jugador igual.
 * Se crea perezosamente la primera vez que el jugador aparece en un resultado
 * de partida reportado por puckzone-game.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    private UUID id;

    /** Puede ser null: game aún no envía usernames en el payload (contrato pendiente). */
    private String username;

    /** Segmento del correo antes de .edu.co, ej: "eci", "unal". */
    @Column(nullable = false)
    private String university;

    @Column(nullable = false)
    private int elo;

    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private int losses;

    @Column(nullable = false)
    private Instant updatedAt;

    public Player(UUID id, String username, String university, int initialElo) {
        this.id = id;
        this.username = username;
        this.university = university;
        this.elo = initialElo;
        this.wins = 0;
        this.losses = 0;
        this.updatedAt = Instant.now();
    }
}
