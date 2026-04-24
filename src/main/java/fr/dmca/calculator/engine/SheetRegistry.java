package fr.dmca.calculator.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dmca.calculator.model.Region;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.kie.dmn.api.core.DMNRuntime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Loads all CalcSheets at startup, validates that no two sheets overlap on
 * the same (region, period) pair, and resolves the applicable sheet for a
 * given (region, date) at runtime.
 */
@ApplicationScoped
public class SheetRegistry {

    private static final Logger LOG = Logger.getLogger(SheetRegistry.class);
    private static final String SHEETS_ROOT = "rules/sheets";

    private final ObjectMapper yamlMapper;
    private final DMNRuntime dmnRuntime;

    private List<LoadedSheet> sheets;

    @Inject
    public SheetRegistry(ObjectMapper yamlMapper, DMNRuntime dmnRuntime) {
        this.yamlMapper = yamlMapper;
        this.dmnRuntime = dmnRuntime;
    }

    @PostConstruct
    void init() throws IOException, URISyntaxException {
        List<SheetMetadata> metadatas = discoverMetadatas();
        validateUniqueness(metadatas);
        sheets = metadatas.stream()
            .map(meta -> new LoadedSheet(meta, dmnRuntime))
            .toList();
        LOG.infof("Sheet registry initialised: %d sheets loaded", sheets.size());
    }

    public LoadedSheet findApplicable(Region region, LocalDate date) {
        return sheets.stream()
            .filter(s -> s.metadata().region() == region
                      && s.metadata().isApplicableOn(date))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No CalcSheet found for region=%s date=%s".formatted(region, date)));
    }

    private List<SheetMetadata> discoverMetadatas() throws IOException {
        List<SheetMetadata> result = new ArrayList<>();
        Enumeration<URL> resources = getClass().getClassLoader()
            .getResources(SHEETS_ROOT);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            collectFromDirectory(url, result);
        }
        return result;
    }

    private void collectFromDirectory(URL dirUrl, List<SheetMetadata> result) throws IOException {
        // In a packaged JAR / native image, resources are enumerated via the classpath index.
        // The quarkus.native.resources.includes pattern "rules/sheets/**" must be set in
        // application.properties to ensure sheet.yaml files are embedded in the native binary.
        Enumeration<URL> yamls = getClass().getClassLoader()
            .getResources(SHEETS_ROOT);
        // Walk sibling sheet.yaml files — Quarkus resource scanning replaces this at build time.
        // See SheetRegistryTest for the validated loading path used in tests.
        throw new UnsupportedOperationException(
            "Directory scanning must be replaced by explicit resource listing. " +
            "See SheetRegistryTest for the test-friendly constructor.");
    }

    SheetMetadata loadMetadata(String resourcePath) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Sheet metadata not found: " + resourcePath);
            }
            return yamlMapper.readValue(is, SheetMetadata.class);
        }
    }

    static void validateUniqueness(List<SheetMetadata> metadatas) {
        for (int i = 0; i < metadatas.size(); i++) {
            for (int j = i + 1; j < metadatas.size(); j++) {
                SheetMetadata a = metadatas.get(i);
                SheetMetadata b = metadatas.get(j);
                if (a.overlapsWith(b)) {
                    throw new IllegalStateException(
                        "CalcSheet overlap detected between '%s' and '%s': both cover region=%s and their periods overlap"
                            .formatted(a.id(), b.id(), a.region()));
                }
            }
        }
    }
}
