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
import javax.sip.header.CSeqHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.List;
import java.util.TooManyListenersException;

import static com.ikrstevs.sip.util.ConnectionUtil.*;

@Service
@Validated
public class SipMessageSenderTcp extends SipMessageSender {

    private final SipClientConfiguration sipClientConfiguration;
    private final SipServerConfiguration sipServerConfiguration;
    private final SipProvider sipProvider;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final AddressFactory addressFactory;

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
        this.addressFactory = sipFactoryWrapper.getAddressFactory();
        sendRegister();
    }

    private void sendRegister() throws SipException, InvalidArgumentException, ParseException {
        ClientTransaction transaction = this.sipProvider.getNewClientTransaction(register());
        transaction.sendRequest();
    }

    public void sendInvite(String username) throws InvalidArgumentException, ParseException, SipException {
        var invite = invite(username);
        ClientTransaction transaction = this.sipProvider.getNewClientTransaction(invite);
        transaction.sendRequest();
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        final ServerTransaction serverTransaction = requestEvent.getServerTransaction();
        final var method = requestEvent.getRequest().getMethod();

        if (method.equals("OPTIONS")) {
            processOptionsRequest(serverTransaction, requestEvent.getRequest());
        }

        if (method.equals("BYE")) {
            processByeRequest(requestEvent.getDialog());
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        final var response = responseEvent.getResponse();
        if (response.getStatusCode() == Response.UNAUTHORIZED) {
            // handle unauthorized
        }

        if (isInviteAccepted(response)) {
            processInviteAccepted(responseEvent, response);
        }
    }

    private static boolean isInviteAccepted(Response response) {
        final var statusCode = response.getStatusCode();

        var cSeqHeader = (CSeqHeader) response.getHeader("CSeq");
        return cSeqHeader.getMethod().equals("INVITE") && statusCode == Response.OK;
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

    private void processByeRequest(Dialog dialog) {
        try {
            // Create an ACK request as per the dialog
            Request ackRequest = dialog.createAck(1);  // Sequence number is 1
            dialog.sendAck(ackRequest);  // Send the ACK message

            System.out.println("ACK sent for BYE request.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processInviteAccepted(ResponseEvent responseEvent, Response response) {
        Dialog dialog = responseEvent.getDialog();

        try {
            Request ackRequest = dialog.createAck(((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getSeqNumber());
            ackRequest.setRequestURI(addressFactory.createSipURI(null, proxyHost()));
            dialog.sendAck(ackRequest);

            byte[] content = (byte[]) response.getContent();
            String sdp = new String(content);
            System.out.println("Received SDP:\n" + sdp);

            // Extract IP and port from SDP
            String rtpIp = extractConnectionAddress(sdp);
            int rtpPort = extractMediaPort(sdp);

            System.out.println("Sending RTP to: " + rtpIp + ":" + rtpPort);
            startRtpStream(rtpIp, 8000);

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    protected String proxyHost() {
        return String.format("%s:%d;transport=%s", sipServerConfiguration.getAddress(), sipServerConfiguration.getPort(), sipServerConfiguration.getTransport().getTransportName());
    }

    @Override
    protected String clientHost() {
        return String.format("%s:%d", sipClientConfiguration.getAddress(), sipClientConfiguration.getPort());
    }
}
