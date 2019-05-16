package ru.ifmo.rain.yatcheniy.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    private static final int SO_TIMEOUT = 200;
    private final Logger logger = Logger.getLogger("Client");

    @SuppressWarnings("WeakerAccess")
    public HelloUDPClient() {
        logger.addHandler(new StreamHandler(System.err, new SimpleFormatter()));
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS.%1$tL %1$Tp %n%2$s%n%4$s: %5$s%n");
    }

    private static void printManualString() {
        System.out.println(String.format(
                "Running UDPClient%nHelloUDPClient <host> <port> <prefix> <threads count> <requests count>")
        );
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            printManualString();
            return;
        }

        for (var arg : args) {
            if (arg == null) {
                printManualString();
                return;
            }
        }

        String host = args[0];
        int port;
        String prefix = args[2];
        int threads;
        int requests;

        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            printManualString();
            return;
        }

        new HelloUDPClient().run(host, port, prefix, threads, requests);
    }

    private static boolean isRespond(final String requestMessage, final String respondMessage) {
        return respondMessage.contains(requestMessage);
    }

    private String getMessage(String prefix, int threadId, int requestId) {
        return String.format(
                "%s%d_%d",
                prefix,
                threadId,
                requestId);
    }

    private void send(final SocketAddress socketProvider, final String prefix, int requests, int threadId) {
        try (var socket = new DatagramSocket()) {
            socket.setSoTimeout(SO_TIMEOUT);
            for (int requestId = 0; requestId < requests; requestId++) {
                final var requestMessage = getMessage(prefix, threadId, requestId);
                final var requestPacket = Packet.createPacket(requestMessage.getBytes(StandardCharsets.UTF_8), socketProvider);
                final var respondPacket = Packet.createPacket(socket.getReceiveBufferSize());
                while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    try {
                        socket.send(requestPacket);
                        logger.info(String.format(
                                "Sending request to %s: %s",
                                socketProvider.toString(),
                                requestMessage));
                        socket.receive(respondPacket);
                        final var respondMessage = Packet.toString(respondPacket);
                        if (isRespond(requestMessage, respondMessage)) {
                            logger.info(String.format("Message received: %s", respondMessage));
                            break;
                        } else {
                            logger.warning(String.format("%s is not respond", respondMessage));
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, String.format("Can't send request in thread %d: " + e.getMessage(), threadId));
                    }
                }
            }
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Can't create socket: " + e.getMessage());
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        logger.info(String.format(
                "Run client with host:%s, port:%d, prefix:%s, threads:%d, requests:%d",
                host,
                port,
                prefix,
                threads,
                requests));
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unknown host:" + host);
            return;
        }

        final var socketProvider = new InetSocketAddress(address, port);
        final var senders = Executors.newFixedThreadPool(threads);
        IntStream.range(0, threads)
                .forEach(
                        threadId -> senders.submit(
                                () -> send(socketProvider, prefix, requests, threadId)
                        ));
        senders.shutdown();
        try {
            senders.awaitTermination(threads * requests, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }
    }
}