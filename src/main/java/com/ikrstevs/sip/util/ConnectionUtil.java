package com.ikrstevs.sip.util;

import lombok.experimental.UtilityClass;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import static java.lang.Thread.sleep;

@UtilityClass
public class ConnectionUtil {

    public static String extractConnectionAddress(String sdp) throws Exception {
        for (String line : sdp.split("\r\n")) {
            if (line.startsWith("c=")) {
                return line.split(" ")[2].trim();
            }
        }
        throw new Exception("Connection address not found in SDP");
    }

    public static int extractMediaPort(String sdp) throws Exception {
        for (String line : sdp.split("\r\n")) {
            if (line.startsWith("m=audio")) {
                return Integer.parseInt(line.split(" ")[1]);
            }
        }
        throw new Exception("Media port not found in SDP");
    }

    public void startRtpStream(String ip, int port) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress address = InetAddress.getByName(ip);

                int sequenceNumber = 0;
                int timestamp = 0;
                byte[] rtpPacket = new byte[172]; // 12 byte RTP header + 160 payload

                while (true) {
                    rtpPacket[0] = (byte) 0x80;  // RTP Version 2
                    rtpPacket[1] = (byte) 0x00;  // Payload Type 0 (PCMU)

                    rtpPacket[2] = (byte) ((sequenceNumber >> 8) & 0xFF);
                    rtpPacket[3] = (byte) (sequenceNumber & 0xFF);

                    rtpPacket[4] = (byte) ((timestamp >> 24) & 0xFF);
                    rtpPacket[5] = (byte) ((timestamp >> 16) & 0xFF);
                    rtpPacket[6] = (byte) ((timestamp >> 8) & 0xFF);
                    rtpPacket[7] = (byte) (timestamp & 0xFF);

                    rtpPacket[8] = 0x00;
                    rtpPacket[9] = 0x00;
                    rtpPacket[10] = 0x00;
                    rtpPacket[11] = 0x00; // SSRC

                    // Fill payload with silence (PCMU silence is often 0xFF or 0x7F)
                    Random random = new Random();
                    for (int i = 12; i < rtpPacket.length; i++) {
                        rtpPacket[i] = (byte) (random.nextInt(256) & 0xFF);  // Random byte for static
                    }

                    DatagramPacket packet = new DatagramPacket(rtpPacket, rtpPacket.length, address, port);
                    socket.send(packet);

                    sequenceNumber++;
                    timestamp += 160; // 20ms worth of 8000 Hz samples
                    sleep(20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
