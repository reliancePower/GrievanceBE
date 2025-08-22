package com.reliance.grievance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Value("${portal.url}")
    private String portalUrl;

    public String getPortalUrl() {
        return portalUrl;
    }
}
