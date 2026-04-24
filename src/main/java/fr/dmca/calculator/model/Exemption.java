package fr.dmca.calculator.model;

import java.time.LocalDate;

public record Exemption(
    String type,
    LocalDate startDate,
    LocalDate endDate   // null = open-ended
) {
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate)
            && (endDate == null || !date.isAfter(endDate));
    }
}
