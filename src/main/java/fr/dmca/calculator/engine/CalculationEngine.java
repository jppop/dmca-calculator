package fr.dmca.calculator.engine;

import fr.dmca.calculator.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class CalculationEngine {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final int SCALE = 2;

    private final SheetRegistry sheetRegistry;
    private final CtpCatalog ctpCatalog;

    @Inject
    public CalculationEngine(SheetRegistry sheetRegistry, CtpCatalog ctpCatalog) {
        this.sheetRegistry = sheetRegistry;
        this.ctpCatalog = ctpCatalog;
    }

    public List<RevenueResult> calculate(CalcContext context, List<RevenueEntry> revenues) {
        LoadedSheet sheet = sheetRegistry.findApplicable(context.region(), context.activityDate());
        return revenues.stream()
            .map(revenue -> calculateRevenue(context, sheet, revenue))
            .toList();
    }

    private RevenueResult calculateRevenue(CalcContext context, LoadedSheet sheet, RevenueEntry revenue) {
        List<ChargeResult> charges = Arrays.stream(ChargeCategory.values())
            .map(category -> calculateCharge(sheet, context, revenue, category))
            .filter(c -> !c.ctpResults().isEmpty())
            .toList();
        return new RevenueResult(revenue.nature(), revenue.amount(), charges);
    }

    private ChargeResult calculateCharge(LoadedSheet sheet, CalcContext context,
                                         RevenueEntry revenue, ChargeCategory category) {
        List<String> ctpCodes = sheet.ctpsFor(revenue.nature(), category, context);

        List<CtpResult> ctpResults = ctpCodes.stream()
            .map(code -> {
                BigDecimal rate   = ctpCatalog.rateFor(code, context.activityDate());
                BigDecimal amount = revenue.amount().multiply(rate).setScale(SCALE, ROUNDING);
                return new CtpResult(code, rate, amount);
            })
            .toList();

        BigDecimal totalRate   = ctpResults.stream().map(CtpResult::rate).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = ctpResults.stream().map(CtpResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ChargeResult(category, totalRate, totalAmount, ctpResults);
    }

    // ── Internal result types ─────────────────────────────────────────────────

    public record RevenueResult(RevenueNature nature, BigDecimal amount, List<ChargeResult> charges) {}

    public record ChargeResult(ChargeCategory category, BigDecimal totalRate,
                               BigDecimal totalAmount, List<CtpResult> ctpResults) {}

    public record CtpResult(String code, BigDecimal rate, BigDecimal amount) {}
}
