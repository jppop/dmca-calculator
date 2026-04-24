# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

**Project** : dmca-calculator.
**Description**: Contribution, Cotisation and taxes calculator for self workers.
En français : moteur de calcul des charges sociales (contribution, cotisations et taxes) liés aux chiffres d'affaires réalisés par les auto-entrepreneurs.

## Business rules

See @.claude/rules/calculator.md

## Target Architecture

**Stack**: Java 21, Quarkus 3.22.3, gRPC (quarkus-grpc), Drools DMN (drools-quarkus-decisions 10.1.0), GraalVM native image ready.

### Regions
METRO (France métropolitaine hors Alsace et Moselle), ALSACE (dép. 67/68), MOSELLE (dép. 57), DROM, MAYOTTE.

### Rule files layout
```
src/main/resources/rules/
  sheets/{year}/{region}/
    sheet.yaml   ← metadata: id, region, startDate, endDate?, dmnNamespace, dmnModelName
    sheet.dmn    ← DMN 1.3 decision tables; compiled to Java at build time by Drools/Quarkus
  ctps/
    ctp-catalog.yaml   ← CTP code → time-bound rates (runtime-loaded, no rebuild needed)
  activity-codes/
    na-codes.yaml      ← NA01..NAnn → RevenueNature mapping (runtime-loaded)
```

### DMN decision naming convention
One decision per `(RevenueNature, ChargeCategory)` pair: `BICP_COTISATION`, `BNC_TAXE`, etc.
Hit policy: **COLLECT** — one row per CTP, returns a flat list of CTP code strings.

### Calculation flow
```
CalcRequest → CalcContext → SheetRegistry (select sheet by region+date)
  → LoadedSheet (evaluate DMN decisions → CTP codes)
  → CtpCatalog (resolve rates at activityDate)
  → ChargeCalculator (BigDecimal, HALF_UP, 2 decimals)
  → CalcResponse
```

### Sheet uniqueness invariant
No two sheets may have overlapping (region, period) pairs. Validated at startup (fail-fast) and covered by `SheetRegistryTest`.

### Adding a new regulatory version
1. Author `sheet.dmn` in Camunda/Operaton Modeler.
2. Add `sheet.yaml` sidecar with new `startDate`, set `endDate` on the previous sheet.
3. Update `ctp-catalog.yaml` if rates changed.
4. Run tests — overlap detection will catch any misconfiguration.
5. Rebuild and deploy.

# Key points

## code base langage

This calculator is designed to comply with French regulatory requirements. Although the business stakeholders are French-speaking, the codebase (variable name, function, comments, etc) should be maintained in English as much as possible, while ensuring that a French-speaking developer can easily understand the code and make the connection with the business domain.

## Roadmap

- [ ] Define the target architecture. The calculator will be implemented as a gRPC microservice (and maybe a REST interface) probably in Java.
- [ ] Implementation 
