package com.ikrstevs.sip.controller;

import com.ikrstevs.sip.service.wrapper.SipMessageSenderTcp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void invite() throws InvalidArgumentException, ParseException, SipException {
    }

    @PostMapping(value = "/accept")
    public void accept() {
//        sipMessageSender.accept();
    }
}
