package com.ikrstevs.sip.service;

import com.ikrstevs.sip.config.SipClientConfiguration;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import javax.sip.InvalidArgumentException;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.List;
import java.util.TooManyListenersException;

@Slf4j
public abstract class SipMessageSender implements SipListener {

    private final SipClientConfiguration sipClientConfiguration;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;

    protected SipMessageSender(@NotNull final AddressFactory addressFactory,
                               @NotNull final HeaderFactory headerFactory,
                               @NotNull final MessageFactory messageFactory,
                               @NotNull final SipClientConfiguration sipClientConfiguration,
                               @NotNull final SipProvider sipProvider) throws TooManyListenersException {
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;
        this.sipClientConfiguration = sipClientConfiguration;
        this.sipProvider = sipProvider;
        sipProvider.addSipListener(this);
    }

    protected Request register() throws InvalidArgumentException, ParseException {
        final SipURI proxyUri = proxyUri();

        // FROM
        SipURI fromAddress = addressFactory.createSipURI(sipClientConfiguration.getUsername(), proxyHost());
        Address fromNameAddress = addressFactory.createAddress(fromAddress);
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "12345");

        // TO
        SipURI toAddress = addressFactory.createSipURI(sipClientConfiguration.getUsername(), proxyHost());
        Address toNameAddress = addressFactory.createAddress(toAddress);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.REGISTER); // Sequence number 1
        MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

        // VIA
        ViaHeader viaHeader = headerFactory.createViaHeader(sipClientConfiguration.getAddress(), sipClientConfiguration.getPort(), sipClientConfiguration.getTransport().getTransportName(), null);

        // Contact
        SipURI contactURI = addressFactory.createSipURI(sipClientConfiguration.getUsername(), clientHost());
        Address contactAddress = addressFactory.createAddress(contactURI);
        contactAddress.setDisplayName(sipClientConfiguration.getUsername());
        ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);

        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600); // 1 hour

        Request request = messageFactory.createRequest(
                proxyUri,
                Request.REGISTER,
                callIdHeader,
                cSeqHeader,
                fromHeader,
                toHeader,
                List.of(viaHeader),
                maxForwards
        );

        request.addHeader(contactHeader);
        request.addHeader(expiresHeader);

        System.out.println("REGISTER request:\n" + request);

        return request;
    }

    protected SipURI proxyUri() throws ParseException {
        return addressFactory.createSipURI(null, proxyHost());
    }

    protected abstract String proxyHost();

    protected abstract String clientHost();
}
