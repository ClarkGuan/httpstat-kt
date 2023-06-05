package com.httpstat.kotlin;

import okhttp3.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

public interface EventDelegate {
    void callStart(Call call);

    void dnsStart(Call call, String domainName);

    void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList);

    void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy);

    void secureConnectStart(Call call);

    void secureConnectEnd(Call call, Handshake handshake);

    void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol);

    void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol, IOException ioe);

    void connectionAcquired(Call call, Connection connection);

    void connectionReleased(Call call, Connection connection);

    void requestHeadersStart(Call call);

    void requestHeadersEnd(Call call, Request request);

    void requestBodyStart(Call call);

    void requestBodyEnd(Call call, long byteCount);

    void requestFailed(Call call, IOException ioe);

    void responseHeadersStart(Call call);

    void responseHeadersEnd(Call call, Response response);

    void responseBodyStart(Call call);

    void responseBodyEnd(Call call, long byteCount);

    void responseFailed(Call call, IOException ioe);

    void callEnd(Call call);

    void callFailed(Call call, IOException ioe);

    class DefaultEventDelegate implements EventDelegate {
        @Override
        public void callStart(Call call) {
        }

        @Override
        public void dnsStart(Call call, String domainName) {
        }

        @Override
        public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        }

        @Override
        public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        }

        @Override
        public void secureConnectStart(Call call) {
        }

        @Override
        public void secureConnectEnd(Call call, Handshake handshake) {
        }

        @Override
        public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
        }

        @Override
        public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol, IOException ioe) {
        }

        @Override
        public void connectionAcquired(Call call, Connection connection) {
        }

        @Override
        public void connectionReleased(Call call, Connection connection) {
        }

        @Override
        public void requestHeadersStart(Call call) {
        }

        @Override
        public void requestHeadersEnd(Call call, Request request) {
        }

        @Override
        public void requestBodyStart(Call call) {
        }

        @Override
        public void requestBodyEnd(Call call, long byteCount) {
        }

        @Override
        public void requestFailed(Call call, IOException ioe) {
        }

        @Override
        public void responseHeadersStart(Call call) {
        }

        @Override
        public void responseHeadersEnd(Call call, Response response) {
        }

        @Override
        public void responseBodyStart(Call call) {
        }

        @Override
        public void responseBodyEnd(Call call, long byteCount) {
        }

        @Override
        public void responseFailed(Call call, IOException ioe) {
        }

        @Override
        public void callEnd(Call call) {
        }

        @Override
        public void callFailed(Call call, IOException ioe) {
        }
    }
}
