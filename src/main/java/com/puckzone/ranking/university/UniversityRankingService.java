package com.puckzone.ranking.university;

import com.puckzone.ranking.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UniversityRankingService {

    private final PlayerRepository playerRepository;

    /** Ranking de universidades ordenado por suma de ELO, con posición 1-based. */
    @Transactional(readOnly = true)
    public List<UniversityRankingResponse> getUniversityRanking() {
        List<UniversityRankingView> rows = playerRepository.findUniversityRanking();
        return IntStream.range(0, rows.size())
                .mapToObj(i -> UniversityRankingResponse.of(rows.get(i), i + 1L))
                .toList();
    }
}
