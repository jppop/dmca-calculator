package fr.dmca.calculator.engine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CtpEntry(
    String code,
    String label,
    List<CtpRate> rates
) {
    public BigDecimal rateOn(LocalDate date) {
        return rates.stream()
            .filter(r -> r.isApplicableOn(date))
            .map(CtpRate::rate)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No rate defined for CTP %s on %s".formatted(code, date)));
    }
}
