package com.placement.portal.service.admin;

import com.placement.portal.domain.SystemConfig;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD operations over the {@code system_configs} table.
 *
 * <p>System configuration is stored as key-value pairs and used for runtime-tunable
 * settings (e.g. max applications per student, feature flags). All writes are
 * performed as upserts so callers do not need to distinguish insert from update.</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    // ---------------------------------------------------------------------------
    // Read operations
    // ---------------------------------------------------------------------------

    /**
     * Returns all configuration entries as a {@code key → value} map, ordered
     * alphabetically by key.
     *
     * @return map of all config entries; never null, may be empty
     */
    @Transactional(readOnly = true)
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        Map<String, String> result = new LinkedHashMap<>();
        configs.stream()
               .sorted((a, b) -> a.getConfigKey().compareToIgnoreCase(b.getConfigKey()))
               .forEach(c -> result.put(c.getConfigKey(), c.getConfigValue()));
        return result;
    }

    /**
     * Returns the value for the given configuration key.
     *
     * @param key the configuration key
     * @return the stored value string
     * @throws EntityNotFoundException if no entry exists for the given key
     */
    @Transactional(readOnly = true)
    public String getConfig(String key) {
        SystemConfig config = systemConfigRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(
                        "System configuration not found for key: " + key));
        return config.getConfigValue();
    }

    // ---------------------------------------------------------------------------
    // Write operations
    // ---------------------------------------------------------------------------

    /**
     * Creates or updates the configuration entry for the given key.
     *
     * @param key   the configuration key (may not be null or blank)
     * @param value the value to store
     */
    public void setConfig(String key, String value) {
        SystemConfig config = systemConfigRepository.findById(key)
                .orElseGet(() -> SystemConfig.builder().configKey(key).build());
        config.setConfigValue(value);
        systemConfigRepository.save(config);
        log.info("System config upserted: key={}", key);
    }

    /**
     * Deletes the configuration entry with the given key.
     *
     * <p>This is a silent no-op if the key does not exist.</p>
     *
     * @param key the configuration key to remove
     */
    public void deleteConfig(String key) {
        if (systemConfigRepository.existsById(key)) {
            systemConfigRepository.deleteById(key);
            log.info("System config deleted: key={}", key);
        } else {
            log.warn("System config delete requested for non-existent key: {}", key);
        }
    }
}
