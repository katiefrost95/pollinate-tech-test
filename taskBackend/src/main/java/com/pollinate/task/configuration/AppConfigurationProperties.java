package com.pollinate.task.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class AppConfigurationProperties {

    private String secret;
    private int expiration;
    private String name;

}
