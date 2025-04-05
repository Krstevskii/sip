package com.ikrstevs.sip.service;

import com.ikrstevs.sip.config.SipClientConfiguration;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import javax.sip.*;

@Getter
abstract class AbstractSipProvider {

    private final SipProvider sipProvider;

    protected AbstractSipProvider(@NotNull final SipClientConfiguration sipClientConfiguration,
                                  @NotNull final SipStack sipStack) throws TransportNotSupportedException, InvalidArgumentException, ObjectInUseException {
        final var address = sipClientConfiguration.getAddress();
        final var port = sipClientConfiguration.getPort();
        final var transport = sipClientConfiguration.getTransport();
        ListeningPoint listeningPoint = sipStack.createListeningPoint(address, port, transport.getTransportName());

        // Create SIP provider and register this class as the listener
        sipProvider = sipStack.createSipProvider(listeningPoint);

        System.out.println("SIP Provider initialized and listening on " + address + ":" + port + "/" + transport);
    }
}
