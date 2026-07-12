package com.puckzone.ranking.match;

import com.puckzone.ranking.config.RankingProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La fórmula ELO estándar con K=30: entre iguales se mueve K/2, el
 * favorito aplastante gana lo mínimo, la hazaña paga casi K completo y
 * ninguna victoria vale 0 puntos.
 */
class EloCalculatorTest {

    private final EloCalculator calculator =
            new EloCalculator(new RankingProperties(1200, 30, 50));

    @Test
    void entreIgualesSeMueveLaMitadDeK() {
        assertEquals(15, calculator.computeDelta(1200, 1200));
    }

    @Test
    void elFavoritoAplastanteGanaLoMinimo() {
        // +1200 de ventaja: lo esperado es ganar; el piso evita el delta 0.
        assertEquals(1, calculator.computeDelta(2400, 1200));
    }

    @Test
    void laHazanaPagaCasiElKCompleto() {
        assertEquals(30, calculator.computeDelta(1200, 2400));
    }

    @Test
    void aMasVentajaDelGanadorMenosPuntos() {
        int comoFavorito = calculator.computeDelta(1300, 1200);
        int entreIguales = calculator.computeDelta(1250, 1250);
        int comoUnderdog = calculator.computeDelta(1200, 1300);
        assertTrue(comoFavorito < entreIguales, "el favorito debe ganar menos que entre iguales");
        assertTrue(entreIguales < comoUnderdog, "el underdog debe ganar más que entre iguales");
    }

    @Test
    void esSimetricoConLaVentana() {
        // Lo que paga ganar de underdog + lo que paga ganar de favorito ≈ K
        // (propiedad de la fórmula: expected(a,b) + expected(b,a) = 1).
        int favorito = calculator.computeDelta(1400, 1100);
        int underdog = calculator.computeDelta(1100, 1400);
        assertEquals(30, favorito + underdog);
    }
}
