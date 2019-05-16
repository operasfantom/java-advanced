package ru.ifmo.rain.yatcheniy.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class HelloUDPServer implements HelloServer {
    private static final int QUEUE_SIZE = 10_000;
    private final Logger logger = Logger.getLogger("Server");
    private DatagramSocket socket;
    private int requestBufferSize;
    private ExecutorService listener;
    private ExecutorService sender;
    private boolean closed = true;

    @SuppressWarnings("WeakerAccess")
    public HelloUDPServer() {
        logger.addHandler(new StreamHandler(System.err, new SimpleFormatter()));
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println(String.format("Running UDPServer%nHelloUDPServer <port> <threads count>"));
            return;
        }

        int port;
        int threads;

        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Can't parse number " + e.getMessage());
            return;
        }

        new HelloUDPServer().start(port, threads);
    }

    @Override
    public void start(int port, int threads) {
        logger.info(String.format("Server start with port:%d, threads:%d", port, threads));
        try {
            socket = new DatagramSocket(port);
            requestBufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Failed to create socket at port " + port);
            return;
        }

        listener = Executors.newSingleThreadExecutor();
        sender = new ThreadPoolExecutor(
                threads,
                threads,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_SIZE),
                new ThreadPoolExecutor.DiscardPolicy());
        closed = false;
        listener.submit(this::listen);
    }

    @Override
    public void close() {
        closed = true;
        listener.shutdownNow();
        sender.shutdownNow();
        try {
            sender.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        socket.close();
    }

    private void listen() {
        while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
            final var packet = Packet.createPacket(requestBufferSize);

            try {
                socket.receive(packet);
                sender.submit(() -> sendResponse(packet));
            } catch (IOException e) {
                if (!closed) {
                    logger.log(Level.WARNING, "Can't receive packet: " + e.getMessage());
                }
            }
        }
    }

    private void sendResponse(DatagramPacket packet) {
        final var requestMessage = Packet.toString(packet);
        final var responseMessage = "Hello, " + requestMessage;
        final var responsePacket = Packet.createPacket(
                responseMessage.getBytes(StandardCharsets.UTF_8),
                packet.getSocketAddress());

        try {
            socket.send(responsePacket);
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("Can't send packet to %s: " + e.getMessage(), packet.getSocketAddress()));
        }
    }
}
