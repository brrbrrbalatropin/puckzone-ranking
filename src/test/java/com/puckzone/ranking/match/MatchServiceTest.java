package com.puckzone.ranking.match;

import com.puckzone.ranking.config.RankingProperties;
import com.puckzone.ranking.player.Player;
import com.puckzone.ranking.player.PlayerRepository;
import com.puckzone.ranking.player.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reglas de negocio del reporte de partidas contra la BD real (H2):
 * la rankeada mueve ELO y V-D, el retry es idempotente por matchId, y ni
 * el bot ni las amistosas tocan el ranking (pero sí dejan historial).
 */
@DataJpaTest
@Import({MatchService.class, PlayerService.class, EloCalculator.class})
class MatchServiceTest {

    @TestConfiguration
    static class Config {
        @Bean
        RankingProperties rankingProperties() {
            return new RankingProperties(1200, 30, 50);
        }
    }

    private static final UUID ANA = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID BETO = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    private static final UUID CARLA = UUID.fromString("cccccccc-0000-0000-0000-000000000003");

    @Autowired
    private MatchService service;
    @Autowired
    private PlayerService players;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private MatchRecordRepository matchRecords;

    private static MatchResultRequest humana(String matchId) {
        return new MatchResultRequest(matchId, false, false, ANA, BETO,
                "ana", "beto", "escuelaing", "unal", 7, 3, 120);
    }

    @Test
    void laPrimeraPartidaHumanaCreaJugadoresYMueveElo() {
        var response = service.processMatch(humana("m-1"));

        assertThat(response.eloDelta()).isEqualTo(15); // iguales a 1200, K=30
        Player ana = playerRepository.findById(ANA).orElseThrow();
        Player beto = playerRepository.findById(BETO).orElseThrow();
        assertThat(ana.getElo()).isEqualTo(1215);
        assertThat(ana.getWins()).isEqualTo(1);
        assertThat(beto.getElo()).isEqualTo(1185);
        assertThat(beto.getLosses()).isEqualTo(1);
        assertThat(matchRecords.count()).isEqualTo(1);
    }

    @Test
    void elRetryDeGameEsIdempotentePorMatchId() {
        service.processMatch(humana("m-retry"));
        var second = service.processMatch(humana("m-retry"));

        assertThat(second.eloDelta()).isEqualTo(15); // responde lo ya aplicado
        Player ana = playerRepository.findById(ANA).orElseThrow();
        assertThat(ana.getElo()).isEqualTo(1215);
        assertThat(ana.getWins()).isEqualTo(1); // no se duplicó nada
        assertThat(matchRecords.count()).isEqualTo(1);
    }

    @Test
    void laPartidaVsBotNoTocaEloNiContadoresNiLeaderboard() {
        var response = service.processMatch(new MatchResultRequest("m-bot", true, false,
                CARLA, null, "carla", null, "escuelaing", null, 7, 5, 90));

        assertThat(response.eloDelta()).isZero();
        Player carla = playerRepository.findById(CARLA).orElseThrow();
        assertThat(carla.getElo()).isEqualTo(1200);
        assertThat(carla.getWins()).isZero();
        assertThat(matchRecords.count()).isEqualTo(1); // pero sí hay historial

        // Solo-bot: existe con perfil pero sin posición y fuera del leaderboard.
        assertThat(players.getPlayer(CARLA).position()).isNull();
        assertThat(players.getGlobalRanking(50)).isEmpty();
    }

    @Test
    void laAmistosaQuedaEnHistorialSinMoverElRanking() {
        var response = service.processMatch(new MatchResultRequest("m-friendly", false, true,
                ANA, BETO, "ana", "beto", "escuelaing", "unal", 7, 6, 200));

        assertThat(response.eloDelta()).isZero();
        Player ana = playerRepository.findById(ANA).orElseThrow();
        Player beto = playerRepository.findById(BETO).orElseThrow();
        assertThat(ana.getElo()).isEqualTo(1200);
        assertThat(ana.getWins()).isZero();
        assertThat(beto.getLosses()).isZero();
        assertThat(matchRecords.findById("m-friendly")).isPresent();
        assertThat(players.getGlobalRanking(50)).isEmpty();
    }

    @Test
    void elLeaderboardSoloCuentaPartidasHumanas() {
        service.processMatch(humana("m-h"));
        service.processMatch(new MatchResultRequest("m-b", true, false,
                CARLA, null, "carla", null, "escuelaing", null, 7, 0, 60));

        var leaderboard = players.getGlobalRanking(50);
        assertThat(leaderboard).hasSize(2); // ana y beto; carla no aparece
        assertThat(leaderboard.getFirst().username()).isEqualTo("ana");
        assertThat(players.getPlayer(ANA).position()).isEqualTo(1);
        assertThat(players.getPlayer(BETO).position()).isEqualTo(2);
    }

    @Test
    void elEloDelPerdedorNoBajaDeCero() {
        playerRepository.save(new Player(BETO, "beto", "unal", 0));

        service.processMatch(humana("m-floor"));

        Player beto = playerRepository.findById(BETO).orElseThrow();
        assertThat(beto.getElo()).isZero(); // 0 - delta se recorta en 0
    }

    @Test
    void losReportesInvalidosSeRechazan() {
        // vs bot con ambos ids (o ninguno): no se sabe quién es el humano.
        var botAmbiguo = new MatchResultRequest("m-x", true, false,
                ANA, BETO, "ana", "beto", "escuelaing", "unal", 7, 1, 60);
        assertThatThrownBy(() -> service.processMatch(botAmbiguo))
                .isInstanceOf(IllegalArgumentException.class);
        // Humana contra sí mismo.
        var contraSiMismo = new MatchResultRequest("m-y", false, false,
                ANA, ANA, "ana", "ana", "escuelaing", "escuelaing", 7, 1, 60);
        assertThatThrownBy(() -> service.processMatch(contraSiMismo))
                .isInstanceOf(IllegalArgumentException.class);
        // Humana sin universidades.
        var sinUniversidades = new MatchResultRequest("m-z", false, false,
                ANA, BETO, "ana", "beto", null, null, 7, 1, 60);
        assertThatThrownBy(() -> service.processMatch(sinUniversidades))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(matchRecords.count()).isZero();
    }
}
