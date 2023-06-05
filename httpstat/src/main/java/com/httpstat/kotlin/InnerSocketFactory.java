package com.httpstat.kotlin;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class InnerSocketFactory extends SocketFactory {
    @Override
    public Socket createSocket() throws IOException {
        return new InnerSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return new InnerSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return new InnerSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return new InnerSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return new InnerSocket(address, port, localAddress, localPort);
    }
}
