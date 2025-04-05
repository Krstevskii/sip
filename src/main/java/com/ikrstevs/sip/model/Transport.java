package com.ikrstevs.sip.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Transport {
    TCP("tcp"),
    UDP("udp");

    private final String transportName;
}
