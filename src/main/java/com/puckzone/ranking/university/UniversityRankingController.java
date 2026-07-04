package com.puckzone.ranking.university;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta del ranking colaborativo por universidad.
 */
@RestController
@RequestMapping("/api/ranking/university")
@RequiredArgsConstructor
public class UniversityRankingController {

    private final UniversityRankingService universityRankingService;

    @GetMapping
    public List<UniversityRankingResponse> getUniversityRanking() {
        return universityRankingService.getUniversityRanking();
    }
}
