package fr.dmca.calculator.engine;

import fr.dmca.calculator.model.Region;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SheetRegistryTest {

    private static SheetMetadata metro(String id, String start, String end) {
        return new SheetMetadata(id, Region.METRO,
            LocalDate.parse(start),
            end != null ? LocalDate.parse(end) : null,
            "ns", id);
    }

    private static SheetMetadata drom(String id, String start, String end) {
        return new SheetMetadata(id, Region.DROM,
            LocalDate.parse(start),
            end != null ? LocalDate.parse(end) : null,
            "ns", id);
    }

    @Test
    void nonOverlappingConsecutiveSheetsPass() {
        assertThatNoException().isThrownBy(() ->
            SheetRegistry.validateUniqueness(List.of(
                metro("sheet-2023-metro", "2023-01-01", "2023-12-31"),
                metro("sheet-2024-metro", "2024-01-01", null)
            )));
    }

    @Test
    void differentRegionsDontOverlap() {
        assertThatNoException().isThrownBy(() ->
            SheetRegistry.validateUniqueness(List.of(
                metro("sheet-2024-metro",  "2024-01-01", null),
                drom("sheet-2024-drom",    "2024-01-01", null)
            )));
    }

    @Test
    void overlappingSheetsSameRegionFails() {
        assertThatThrownBy(() ->
            SheetRegistry.validateUniqueness(List.of(
                metro("sheet-2024-metro", "2024-01-01", null),
                metro("sheet-2024-bis",   "2024-06-01", null)
            )))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("overlap")
            .hasMessageContaining("METRO");
    }

    @Test
    void twoOpenEndedSheetsSameRegionFails() {
        assertThatThrownBy(() ->
            SheetRegistry.validateUniqueness(List.of(
                metro("sheet-A", "2024-01-01", null),
                metro("sheet-B", "2025-01-01", null)
            )))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exactBoundaryIsNotAnOverlap() {
        // sheet-A ends 2023-12-31, sheet-B starts 2024-01-01 — no overlap
        assertThatNoException().isThrownBy(() ->
            SheetRegistry.validateUniqueness(List.of(
                metro("sheet-2022-metro", "2022-01-01", "2022-12-31"),
                metro("sheet-2023-metro", "2023-01-01", "2023-12-31"),
                metro("sheet-2024-metro", "2024-01-01", null)
            )));
    }
}
