package com.ikrstevs.sip.service.wrapper;

import com.ikrstevs.sip.config.SipClientConfiguration;
import com.ikrstevs.sip.config.SipServerConfiguration;
import com.ikrstevs.sip.service.SipMessageSender;
import com.ikrstevs.sip.service.SipProviderTcp;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.List;
import java.util.TooManyListenersException;

@Service
@Validated
public class SipMessageSenderTcp extends SipMessageSender {

    private final SipClientConfiguration sipClientConfiguration;
    private final SipServerConfiguration sipServerConfiguration;
    private final SipProvider sipProvider;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;

    protected SipMessageSenderTcp(@NotNull final SipFactoryWrapper sipFactoryWrapper,
                                  @NotNull final SipClientConfiguration sipClientConfiguration,
                                  @NotNull final SipProviderTcp sipProvider,
                                  @NotNull final SipServerConfiguration sipServerConfiguration) throws InvalidArgumentException, ParseException, SipException, TooManyListenersException {
        super(sipFactoryWrapper.getAddressFactory(),
                sipFactoryWrapper.getHeaderFactory(),
                sipFactoryWrapper.getMessageFactory(),
                sipClientConfiguration,
                sipProvider.getSipProvider());
        this.sipProvider = sipProvider.getSipProvider();
        this.sipServerConfiguration = sipServerConfiguration;
        this.sipClientConfiguration = sipClientConfiguration;
        this.headerFactory = sipFactoryWrapper.getHeaderFactory();
        this.messageFactory = sipFactoryWrapper.getMessageFactory();
        ClientTransaction transaction = this.sipProvider.getNewClientTransaction(register());
        transaction.sendRequest();
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        final ServerTransaction serverTransaction = requestEvent.getServerTransaction();
        final var method = requestEvent.getRequest().getMethod();

        if (method.equals("OPTIONS")) {
            processOptionsRequest(serverTransaction, requestEvent.getRequest());
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        final var response = responseEvent.getResponse();
        if (response.getStatusCode() == Response.UNAUTHORIZED) {
            // handle unauthorized
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
    }

    @Override
    protected String proxyHost() {
        return String.format("%s:%d;transport=%s", sipServerConfiguration.getAddress(), sipServerConfiguration.getPort(), sipServerConfiguration.getTransport().getTransportName());
    }

    @Override
    protected String clientHost() {
        return String.format("%s:%d", sipClientConfiguration.getAddress(), sipClientConfiguration.getPort());
    }

    private void processOptionsRequest(ServerTransaction serverTransaction, Request request) {
        try {
            // Log that we received OPTIONS
            System.out.println("Received OPTIONS request");

            Response response = messageFactory.createResponse(Response.OK, request);

            // Add required headers
            response.addHeader(headerFactory.createAllowHeader("INVITE, ACK, CANCEL, OPTIONS, BYE"));
            response.addHeader(headerFactory.createUserAgentHeader(List.of("JAIN SIP Client")));

            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(request);
            }
            serverTransaction.sendResponse(response);
            System.out.println("Sent 200 OK in response to OPTIONS");
        } catch (ParseException | SipException | InvalidArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
