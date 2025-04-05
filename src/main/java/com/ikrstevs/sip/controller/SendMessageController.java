package com.ikrstevs.sip.controller;

import com.ikrstevs.sip.service.wrapper.SipMessageSenderTcp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class SendMessageController {

    private final SipMessageSenderTcp sipMessageSender;

    @GetMapping
    public String hello() {
        return "HELLO WORLD!";
    }

    @GetMapping(value = "/invite")
    public void invite(@RequestParam(name = "username") final String username) throws InvalidArgumentException, ParseException, SipException {
        sipMessageSender.sendInvite(username);
    }

    @PostMapping(value = "/accept")
    public void accept() {
//        sipMessageSender.accept();
    }
}
