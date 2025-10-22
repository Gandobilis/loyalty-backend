package com.multi.loyaltybackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VersionService {

    @Value("${application.version:0.0.1}")
    private String version;

    /**
     * Get the application version from properties
     * @return version string
     */
    public String getVersion() {
        // Remove -SNAPSHOT suffix for display
        return version.replace("-SNAPSHOT", "");
    }

    /**
     * Get the full application version including snapshot suffix
     * @return full version string
     */
    public String getFullVersion() {
        return version;
    }
}
