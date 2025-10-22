package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final VersionService versionService;

    /**
     * Makes the application version available to all templates
     */
    @ModelAttribute("appVersion")
    public String appVersion() {
        return versionService.getVersion();
    }
}
