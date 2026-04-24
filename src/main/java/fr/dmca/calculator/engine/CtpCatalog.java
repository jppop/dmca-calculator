package fr.dmca.calculator.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class CtpCatalog {

    private static final Logger LOG = Logger.getLogger(CtpCatalog.class);
    private static final String CATALOG_PATH = "/rules/ctps/ctp-catalog.yaml";

    private final ObjectMapper yamlMapper;
    private Map<String, CtpEntry> index;

    @Inject
    public CtpCatalog(ObjectMapper yamlMapper) {
        this.yamlMapper = yamlMapper;
    }

    @PostConstruct
    void load() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(CATALOG_PATH)) {
            if (is == null) {
                throw new IllegalStateException("CTP catalog not found at " + CATALOG_PATH);
            }
            List<CtpEntry> entries = yamlMapper.readValue(is, new TypeReference<>() {});
            index = entries.stream().collect(Collectors.toMap(CtpEntry::code, e -> e));
            LOG.infof("CTP catalog loaded: %d entries", index.size());
        }
    }

    public BigDecimal rateFor(String ctpCode, LocalDate date) {
        CtpEntry entry = index.get(ctpCode);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown CTP code: " + ctpCode);
        }
        return entry.rateOn(date);
    }

    public boolean contains(String ctpCode) {
        return index.containsKey(ctpCode);
    }
}
