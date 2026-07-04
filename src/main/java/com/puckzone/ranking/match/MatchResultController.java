package com.puckzone.ranking.match;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint que consume puckzone-game al terminar cada partida.
 */
@RestController
@RequestMapping("/api/ranking/match")
@RequiredArgsConstructor
public class MatchResultController {

    private final MatchService matchService;

    @PostMapping
    public MatchProcessedResponse reportMatch(@Valid @RequestBody MatchResultRequest request) {
        return matchService.processMatch(request);
    }
}
