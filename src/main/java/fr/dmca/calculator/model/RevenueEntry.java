package fr.dmca.calculator.model;

import java.math.BigDecimal;

public record RevenueEntry(
    RevenueNature nature,
    BigDecimal amount
) {}
