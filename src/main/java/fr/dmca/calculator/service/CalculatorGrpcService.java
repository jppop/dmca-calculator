package fr.dmca.calculator.service;

import fr.dmca.calculator.engine.CalculationEngine;
import fr.dmca.calculator.grpc.CalcRequest;
import fr.dmca.calculator.grpc.CalcResponse;
import fr.dmca.calculator.grpc.CalculatorService;
import fr.dmca.calculator.model.ActivityType;
import fr.dmca.calculator.model.CalcContext;
import fr.dmca.calculator.model.ChargeCategory;
import fr.dmca.calculator.model.Exemption;
import fr.dmca.calculator.model.Region;
import fr.dmca.calculator.model.RevenueEntry;
import fr.dmca.calculator.model.RevenueNature;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@GrpcService
public class CalculatorGrpcService implements CalculatorService {

    private final CalculationEngine engine;

    @Inject
    public CalculatorGrpcService(CalculationEngine engine) {
        this.engine = engine;
    }

    @Override
    public Uni<CalcResponse> calculate(CalcRequest request) {
        CalcContext context = toContext(request);
        List<RevenueEntry> revenues = toRevenues(request.getRevenuesList());

        List<CalculationEngine.RevenueResult> results = engine.calculate(context, revenues);

        CalcResponse response = CalcResponse.newBuilder()
            .addAllResults(results.stream().map(this::toProtoResult).toList())
            .build();

        return Uni.createFrom().item(response);
    }

    // ── Mapping: proto → domain ───────────────────────────────────────────────

    private CalcContext toContext(CalcRequest req) {
        List<Exemption> exemptions = req.getExemptionsList().stream()
            .map(e -> new Exemption(
                e.getType(),
                LocalDate.parse(e.getStartDate()),
                e.getEndDate().isBlank() ? null : LocalDate.parse(e.getEndDate())
            ))
            .toList();

        Map<RevenueNature, BigDecimal> prevRevenues = new EnumMap<>(RevenueNature.class);
        for (fr.dmca.calculator.grpc.RevenueEntry entry : req.getPreviousYearRevenuesList()) {
            prevRevenues.put(toNature(entry.getRevenueNature()), new BigDecimal(entry.getAmount()));
        }

        return new CalcContext(
            toActivityType(req.getMainActivity()),
            toRegion(req.getRegion()),
            LocalDate.parse(req.getActivityDate()),
            List.copyOf(req.getOptionsList()),
            exemptions,
            prevRevenues
        );
    }

    private List<RevenueEntry> toRevenues(List<fr.dmca.calculator.grpc.RevenueEntry> entries) {
        return entries.stream()
            .map(e -> new RevenueEntry(toNature(e.getRevenueNature()), new BigDecimal(e.getAmount())))
            .toList();
    }

    // ── Mapping: domain → proto ───────────────────────────────────────────────

    private fr.dmca.calculator.grpc.RevenueResult toProtoResult(CalculationEngine.RevenueResult r) {
        return fr.dmca.calculator.grpc.RevenueResult.newBuilder()
            .setRevenueNature(fromNature(r.nature()))
            .setAmount(r.amount().toPlainString())
            .addAllCharges(r.charges().stream().map(this::toProtoCharge).toList())
            .build();
    }

    private fr.dmca.calculator.grpc.ChargeResult toProtoCharge(CalculationEngine.ChargeResult c) {
        return fr.dmca.calculator.grpc.ChargeResult.newBuilder()
            .setCategory(fromCategory(c.category()))
            .setTotalRate(c.totalRate().toPlainString())
            .setTotalAmount(c.totalAmount().toPlainString())
            .addAllCtps(c.ctpResults().stream().map(this::toProtoCtpResult).toList())
            .build();
    }

    private fr.dmca.calculator.grpc.CtpResult toProtoCtpResult(CalculationEngine.CtpResult r) {
        return fr.dmca.calculator.grpc.CtpResult.newBuilder()
            .setCode(r.code())
            .setRate(r.rate().toPlainString())
            .setAmount(r.amount().toPlainString())
            .build();
    }

    // ── Enum conversions ──────────────────────────────────────────────────────

    private static ActivityType toActivityType(fr.dmca.calculator.grpc.ActivityType proto) {
        return switch (proto) {
            case ARTISANAL  -> ActivityType.ARTISANAL;
            case COMMERCIAL -> ActivityType.COMMERCIAL;
            case LIBERAL    -> ActivityType.LIBERAL;
            case LIBERAL_NR -> ActivityType.LIBERAL_NR;
            default -> throw new IllegalArgumentException("Unknown activity type: " + proto);
        };
    }

    private static Region toRegion(fr.dmca.calculator.grpc.Region proto) {
        return switch (proto) {
            case METRO   -> Region.METRO;
            case ALSACE  -> Region.ALSACE;
            case MOSELLE -> Region.MOSELLE;
            case DROM    -> Region.DROM;
            case MAYOTTE -> Region.MAYOTTE;
            default -> throw new IllegalArgumentException("Unknown region: " + proto);
        };
    }

    private static RevenueNature toNature(fr.dmca.calculator.grpc.RevenueNature proto) {
        return switch (proto) {
            case BICP -> RevenueNature.BICP;
            case BICV -> RevenueNature.BICV;
            case BNC  -> RevenueNature.BNC;
            case LMTC -> RevenueNature.LMTC;
            default -> throw new IllegalArgumentException("Unknown revenue nature: " + proto);
        };
    }

    private static fr.dmca.calculator.grpc.RevenueNature fromNature(RevenueNature n) {
        return switch (n) {
            case BICP -> fr.dmca.calculator.grpc.RevenueNature.BICP;
            case BICV -> fr.dmca.calculator.grpc.RevenueNature.BICV;
            case BNC  -> fr.dmca.calculator.grpc.RevenueNature.BNC;
            case LMTC -> fr.dmca.calculator.grpc.RevenueNature.LMTC;
        };
    }

    private static fr.dmca.calculator.grpc.ChargeCategory fromCategory(ChargeCategory c) {
        return switch (c) {
            case COTISATION -> fr.dmca.calculator.grpc.ChargeCategory.COTISATION;
            case TAXE       -> fr.dmca.calculator.grpc.ChargeCategory.TAXE;
            case CFP        -> fr.dmca.calculator.grpc.ChargeCategory.CFP;
            case CCICMA     -> fr.dmca.calculator.grpc.ChargeCategory.CCICMA;
        };
    }
}
