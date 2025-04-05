package com.ikrstevs.sip.service.wrapper;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

@Component
@Getter
public class SipFactoryWrapper {

    private final SipFactory sipFactory;

    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;

    public SipFactoryWrapper() throws PeerUnavailableException {
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        addressFactory = sipFactory.createAddressFactory();
        headerFactory = sipFactory.createHeaderFactory();
        messageFactory = sipFactory.createMessageFactory();
    }

}
