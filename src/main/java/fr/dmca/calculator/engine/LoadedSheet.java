package fr.dmca.calculator.engine;

import fr.dmca.calculator.model.CalcContext;
import fr.dmca.calculator.model.ChargeCategory;
import fr.dmca.calculator.model.RevenueNature;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A CalcSheet bound to its compiled DMN model.
 * Evaluates CTP selection decisions for a given (revenueNature, chargeCategory) pair.
 *
 * Decision name convention in the .dmn file: "{REVENUE_NATURE}_{CHARGE_CATEGORY}"
 * e.g. "BICP_COTISATION", "BNC_TAXE", etc.
 */
public class LoadedSheet {

    private final SheetMetadata metadata;
    private final DMNRuntime dmnRuntime;
    private final DMNModel dmnModel;

    LoadedSheet(SheetMetadata metadata, DMNRuntime dmnRuntime) {
        this.metadata = metadata;
        this.dmnRuntime = dmnRuntime;
        this.dmnModel = dmnRuntime.getModel(metadata.dmnNamespace(), metadata.dmnModelName());
        if (dmnModel == null) {
            throw new IllegalStateException(
                "DMN model not found: namespace=%s name=%s (sheet %s)"
                    .formatted(metadata.dmnNamespace(), metadata.dmnModelName(), metadata.id()));
        }
    }

    public SheetMetadata metadata() {
        return metadata;
    }

    /**
     * Returns the list of CTP codes applicable for the given revenue nature and charge category,
     * evaluated against the provided CalcContext.
     *
     * The DMN decision is named "{revenueNature}_{chargeCategory}" and returns a FEEL list of
     * CTP code strings, e.g. ["500", "042"].
     */
    @SuppressWarnings("unchecked")
    public List<String> ctpsFor(RevenueNature nature, ChargeCategory category, CalcContext context) {
        String decisionName = nature.name() + "_" + category.name();
        DMNContext dmnContext = buildDmnContext(context);
        DMNResult result = dmnRuntime.evaluateDecisionByName(dmnModel, decisionName, dmnContext);
        if (result.hasErrors()) {
            throw new IllegalStateException(
                "DMN evaluation errors for %s in sheet %s: %s"
                    .formatted(decisionName, metadata.id(), result.getMessages()));
        }
        Object value = result.getDecisionResultByName(decisionName).getResult();
        if (value == null) {
            return List.of();
        }
        return (List<String>) value;
    }

    private DMNContext buildDmnContext(CalcContext context) {
        DMNContext ctx = dmnRuntime.newContext();
        ctx.set("mainActivity", context.mainActivity().name());
        ctx.set("hasPLF", context.hasOption("PLF"));
        ctx.set("activityDate", context.activityDate());
        ctx.set("totalPreviousYearRevenue", context.totalPreviousYearRevenue());

        // Per-nature previous year revenues
        for (var nature : fr.dmca.calculator.model.RevenueNature.values()) {
            ctx.set("previousYearRevenue_" + nature.name(), context.previousYearRevenue(nature));
        }

        // Exemptions as a list of FEEL contexts: [{type: "ACRE", startDate: ..., endDate: ...}]
        List<Map<String, Object>> feelExemptions = context.exemptions().stream()
            .map(e -> Map.<String, Object>of(
                "type", e.type(),
                "startDate", e.startDate(),
                "endDate", e.endDate() != null ? e.endDate() : LocalDate.MAX
            ))
            .toList();
        ctx.set("exemptions", feelExemptions);

        return ctx;
    }
}
