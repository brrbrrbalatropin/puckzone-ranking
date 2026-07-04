package com.puckzone.ranking.player;

import com.puckzone.ranking.config.RankingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RankingProperties properties;

    /**
     * Busca al jugador o lo crea con el ELO inicial si es la primera vez que
     * aparece en una partida. Si ya existía, refresca username y universidad
     * con lo que venga en el payload (pueden haber cambiado o llegar por
     * primera vez cuando game amplíe el contrato).
     */
    @Transactional
    public Player getOrCreate(UUID id, String username, String university) {
        return playerRepository.findById(id)
                .map(player -> {
                    if (username != null && !username.isBlank()) {
                        player.setUsername(username);
                    }
                    player.setUniversity(university);
                    return player;
                })
                .orElseGet(() -> playerRepository.save(
                        new Player(id, username, university, properties.initialElo())));
    }

    /** Leaderboard global: top N jugadores por ELO descendente, con posición 1-based. */
    @Transactional(readOnly = true)
    public List<PlayerRankingResponse> getGlobalRanking(int limit) {
        List<Player> top = playerRepository.findAllByOrderByEloDesc(PageRequest.of(0, limit));
        return IntStream.range(0, top.size())
                .mapToObj(i -> PlayerRankingResponse.of(top.get(i), i + 1L))
                .toList();
    }

    /** ELO, posición global y estadísticas de un jugador puntual. */
    @Transactional(readOnly = true)
    public PlayerRankingResponse getPlayer(UUID id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el jugador " + id));
        long position = playerRepository.countByEloGreaterThan(player.getElo()) + 1;
        return PlayerRankingResponse.of(player, position);
    }

    /** Aplica el resultado de una partida ya calculado: nuevo ELO y contadores. */
    public void applyResult(Player player, int newElo, boolean won) {
        player.setElo(newElo);
        if (won) {
            player.setWins(player.getWins() + 1);
        } else {
            player.setLosses(player.getLosses() + 1);
        }
        player.setUpdatedAt(Instant.now());
    }
}
