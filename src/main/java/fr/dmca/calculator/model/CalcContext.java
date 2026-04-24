package fr.dmca.calculator.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record CalcContext(
    ActivityType mainActivity,
    Region region,
    LocalDate activityDate,
    List<String> options,
    List<Exemption> exemptions,
    Map<RevenueNature, BigDecimal> previousYearRevenues
) {
    public boolean hasOption(String option) {
        return options.contains(option);
    }

    public boolean hasActiveExemption(String type) {
        return exemptions.stream()
            .anyMatch(e -> e.type().equals(type) && e.isActiveOn(activityDate));
    }

    public BigDecimal previousYearRevenue(RevenueNature nature) {
        return previousYearRevenues.getOrDefault(nature, BigDecimal.ZERO);
    }

    public BigDecimal totalPreviousYearRevenue() {
        return previousYearRevenues.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
