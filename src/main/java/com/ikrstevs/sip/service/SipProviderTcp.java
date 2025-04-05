package com.ikrstevs.sip.service;

import com.ikrstevs.sip.config.SipClientConfiguration;
import com.ikrstevs.sip.service.wrapper.SipStackWrapper;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.sip.InvalidArgumentException;
import javax.sip.ObjectInUseException;
import javax.sip.TransportNotSupportedException;
import java.util.TooManyListenersException;

@Component
@Validated
@Getter
public class SipProviderTcp extends AbstractSipProvider {

    protected SipProviderTcp(@NotNull final SipClientConfiguration sipClientConfiguration,
                             @NotNull final SipStackWrapper sipStackWrapper) throws TransportNotSupportedException, InvalidArgumentException, ObjectInUseException, TooManyListenersException {
        super(sipClientConfiguration, sipStackWrapper.getSipStack());
    }
}
