package ru.ifmo.rain.yatcheniy.rmi.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

class NetUtils {
    static int findFreePort() {
        try {
            var socket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress());
            var result = socket.getLocalPort();
            socket.setReuseAddress(true);
            socket.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
