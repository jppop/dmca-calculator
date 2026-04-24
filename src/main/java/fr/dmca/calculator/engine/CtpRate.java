package fr.dmca.calculator.engine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CtpRate(
    @JsonProperty("startDate") LocalDate startDate,
    @JsonProperty("endDate")   LocalDate endDate,  // null = open-ended
    BigDecimal rate
) {
    public boolean isApplicableOn(LocalDate date) {
        return !date.isBefore(startDate)
            && (endDate == null || !date.isAfter(endDate));
    }
}
