package com.ikrstevs.sip.config;

import com.ikrstevs.sip.model.Transport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@Configuration
@ConfigurationProperties(prefix = "sip.server")
public class SipServerConfiguration {

    @NotBlank
    private String address;

    @NotNull
    private Integer port;

    @NotNull
    private Transport transport;
}
