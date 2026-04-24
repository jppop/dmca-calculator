package fr.dmca.calculator.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dmca.calculator.model.RevenueNature;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ActivityCodeRegistry {

    private static final Logger LOG = Logger.getLogger(ActivityCodeRegistry.class);
    private static final String CODES_PATH = "/rules/activity-codes/na-codes.yaml";

    private record NaCodeEntry(String code, RevenueNature revenueNature) {}

    private final ObjectMapper yamlMapper;
    private Map<String, RevenueNature> index;

    @Inject
    public ActivityCodeRegistry(ObjectMapper yamlMapper) {
        this.yamlMapper = yamlMapper;
    }

    @PostConstruct
    void load() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(CODES_PATH)) {
            if (is == null) {
                throw new IllegalStateException("NA codes table not found at " + CODES_PATH);
            }
            List<NaCodeEntry> entries = yamlMapper.readValue(is, new TypeReference<>() {});
            index = entries.stream().collect(Collectors.toMap(NaCodeEntry::code, NaCodeEntry::revenueNature));
            LOG.infof("Activity code registry loaded: %d entries", index.size());
        }
    }

    public RevenueNature revenueNatureFor(String naCode) {
        RevenueNature nature = index.get(naCode);
        if (nature == null) {
            throw new IllegalArgumentException("Unknown activity code: " + naCode);
        }
        return nature;
    }
}
