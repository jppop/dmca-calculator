package fr.dmca.calculator.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.dmca.calculator.model.Region;

import java.time.LocalDate;

public record SheetMetadata(
    String id,
    Region region,
    @JsonProperty("startDate") LocalDate startDate,
    @JsonProperty("endDate")   LocalDate endDate,   // null = open-ended (most recent sheet)
    String dmnNamespace,
    String dmnModelName
) {
    public boolean isApplicableOn(LocalDate date) {
        return !date.isBefore(startDate)
            && (endDate == null || !date.isAfter(endDate));
    }

    public boolean overlapsWith(SheetMetadata other) {
        if (this.region != other.region) {
            return false;
        }
        LocalDate thisEnd  = this.endDate  != null ? this.endDate  : LocalDate.MAX;
        LocalDate otherEnd = other.endDate != null ? other.endDate : LocalDate.MAX;
        return !this.startDate.isAfter(otherEnd) && !other.startDate.isAfter(thisEnd);
    }
}
