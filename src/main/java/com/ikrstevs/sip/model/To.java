package com.ikrstevs.sip.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record To(@NotBlank String address, @NotBlank String message, @NotNull String method) {
}
