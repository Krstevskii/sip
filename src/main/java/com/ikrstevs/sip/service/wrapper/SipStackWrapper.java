package com.ikrstevs.sip.service.wrapper;

import com.ikrstevs.sip.config.SipClientConfiguration;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.sip.PeerUnavailableException;
import javax.sip.SipStack;
import java.util.Properties;

@Component
@Validated
@Getter
public class SipStackWrapper {

    private final SipStack sipStack;

    public SipStackWrapper(@NotNull final SipClientConfiguration sipClientConfiguration, @NotNull final SipFactoryWrapper sipFactoryWrapper) throws PeerUnavailableException {
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "SimpleSipStack");
        properties.setProperty("javax.sip.IP_ADDRESS", sipClientConfiguration.getAddress());
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/server.log");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/debug.log");

        sipStack = sipFactoryWrapper.getSipFactory().createSipStack(properties);
    }
}
