package com.ikrstevs.sip.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
                InputStream ulawFile = new DefaultResourceLoader().getResource("classpath:audio.pcm").getInputStream();
                InetAddress address = InetAddress.getByName(ip);

                int sequenceNumber = 0;
                int timestamp = 0;
                byte[] rtpPacket = new byte[172]; // 12 byte RTP header + 160 byte payload (20ms of audio)

                byte[] buffer = new byte[160]; // Buffer to hold one frame of G.711 μ-law data

                while (true) {
                    // Read 160 bytes (one RTP payload) from the .ulaw file
                    int bytesRead = ulawFile.read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("End of file reached. Restarting the stream.");
                        ulawFile.close();
                        ulawFile = new DefaultResourceLoader().getResource("classpath:audio.pcm").getInputStream();
                        continue;
                    }

                    // RTP header:
                    // 0x80 for RTP version 2, 0x00 for Payload Type 0 (PCMU), Sequence number, Timestamp, SSRC
                    rtpPacket[0] = (byte) 0x80;  // RTP Version 2
                    rtpPacket[1] = (byte) 0x00;  // Payload Type 0 (PCMU)

                    rtpPacket[2] = (byte) ((sequenceNumber >> 8) & 0xFF);  // Sequence number (high byte)
                    rtpPacket[3] = (byte) (sequenceNumber & 0xFF);         // Sequence number (low byte)

                    rtpPacket[4] = (byte) ((timestamp >> 24) & 0xFF);      // Timestamp (high byte)
                    rtpPacket[5] = (byte) ((timestamp >> 16) & 0xFF);      // Timestamp (next byte)
                    rtpPacket[6] = (byte) ((timestamp >> 8) & 0xFF);       // Timestamp (next byte)
                    rtpPacket[7] = (byte) (timestamp & 0xFF);              // Timestamp (low byte)

                    rtpPacket[8] = 0x00;  // SSRC (high byte)
                    rtpPacket[9] = 0x00;  // SSRC (next byte)
                    rtpPacket[10] = 0x00; // SSRC (next byte)
                    rtpPacket[11] = 0x00; // SSRC (low byte)

                    // Copy the G.711 μ-law payload (the 160 bytes read from the .ulaw file) into the RTP packet
                    System.arraycopy(buffer, 0, rtpPacket, 12, bytesRead);

                    // Send the packet
                    DatagramPacket packet = new DatagramPacket(rtpPacket, rtpPacket.length, address, port);
                    socket.send(packet);

                    // Increment sequence number and timestamp for the next packet
                    sequenceNumber++;
                    timestamp += 160; // 20ms worth of 8000 Hz samples (160 bytes)

                    // Sleep for 20ms (20ms corresponds to 160 bytes of G.711 μ-law data)
                    Thread.sleep(20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
