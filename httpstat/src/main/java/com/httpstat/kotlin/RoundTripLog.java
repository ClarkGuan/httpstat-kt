package com.httpstat.kotlin;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.util.Log;
import okhttp3.*;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.http.RequestLine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class RoundTripLog {
    private final EventDelegate delegate;
    private Proxy.Type proxyType;
    private InetSocketAddress targetAddr;
    private List<InetAddress> dnsAddrs;
    private Protocol protocol;
    private Handshake handshake;
    private long callStartWallTime;
    private IOException error;
    private InnerSocket sock;

    // time cost
    private TimeCounter timeCounter;
    private long dispatchCost;
    private long dnsCost;
    private long tcpCost;
    private long tlsCost;
    private long httpCostWithoutResponseBody;
    private long httpResponseBodyCost;
    private long reflectCost;

    // size cost
    private Counter readSizeCounter, writeSizeCounter;
    private Counter readSizeCounterCopy, writeSizeCounterCopy;
    private long totalReadSize, totalWriteSize;
    private long tlsReadSize, tlsWriteSize;
    private long requestSize;
    private long requestBodySize;
    private long responseHeaderSize;
    private long responseBodySize;

    // request
    private String requestLine;
    private Headers requestHeaders;
    private boolean hasRequestBody;

    // response
    private int responseCode;
    private String responseMessage;
    private Headers responseHeaders;

    private boolean isHttps;
    private boolean tcpHandshake;

    RoundTripLog() {
        delegate = new LogEventDelegate();
    }

    public long dnsTimeCost() {
        return dnsCost;
    }

    public long tcpHandshakeTimeCost() {
        return tcpCost;
    }

    public long tlsHandshakeTimeCost() {
        return tlsCost;
    }

    public long beforeResponseBodyTimeCost() {
        return httpCostWithoutResponseBody;
    }

    public long responseBodyTimeCost() {
        return httpResponseBodyCost;
    }

    public long reflectTimeCost() {
        return reflectCost;
    }

    public long roundTripTimeCost() {
        return dnsCost + tcpCost + tlsCost + httpCostWithoutResponseBody + httpResponseBodyCost;
    }

    public long totalTimeCost() {
        return dispatchCost + roundTripTimeCost();
    }

    public long getCallStartWallTime() {
        return callStartWallTime;
    }

    public boolean hasError() {
        return error != null;
    }

    public IOException getError() {
        return error;
    }

    public EventDelegate getDelegate() {
        return delegate;
    }

    public boolean connectionReused() {
        return !tcpHandshake;
    }

    public long getTotalRead() {
        return totalReadSize;
    }

    public long getTotalWrite() {
        return totalWriteSize;
    }

    public long getTlsReadSize() {
        return tlsReadSize;
    }

    public long getTlsWriteSize() {
        return tlsWriteSize;
    }

    public long getRequestHeaderSize() {
        return requestSize - requestBodySize;
    }

    public long getRequestBodySize() {
        return requestBodySize;
    }

    public long getRequestSize() {
        return requestSize;
    }

    public long getResponseHeaderSize() {
        return responseHeaderSize;
    }

    public long getResponseBodySize() {
        return responseBodySize;
    }

    public long getResponseSize() {
        return responseHeaderSize + responseBodySize;
    }

    public boolean isProxy() {
        return proxyType != null && proxyType != Proxy.Type.DIRECT;
    }

    public String getRemoteAddress() {
        if (targetAddr == null) return "";
        return targetAddr.getAddress().getHostAddress() + ":" + targetAddr.getPort();
    }

    public String getProtocol() {
        if (protocol == null) return "";
        return protocol.toString();
    }

    private static final String[] EMPTY_ADDRESSES = new String[0];

    public String[] getDnsResult() {
        if (dnsAddrs == null || dnsAddrs.isEmpty()) return EMPTY_ADDRESSES;
        String[] ret = new String[dnsAddrs.size()];
        for (int i = 0; i < dnsAddrs.size(); i++) {
            ret[i] = dnsAddrs.get(i).getHostAddress();
        }
        return ret;
    }

    @SuppressLint("DefaultLocale")
    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Connected to %s", getRemoteAddress()));
        if (connectionReused()) {
            sb.append(" (cached)\n");
        } else {
            sb.append(String.format(", DNS: %s\n", Arrays.toString(getDnsResult())));
        }

        if (handshake != null) {
            sb.append(String.format("Connected to %s\n", handshake.tlsVersion().javaName()));
        }

        sb.append('\n').append(requestLine).append('\n');
        if (requestHeaders != null && requestHeaders.size() > 0) {
            sb.append(requestHeaders).append('\n');
        }

        sb.append(String.format("%d %s\n", responseCode, responseMessage));
        if (responseHeaders != null && responseHeaders.size() > 0) {
            sb.append(responseHeaders).append('\n');
        }

        if (sock != null) {
            if(isHttps) {
                sb.append(String.format("TLS   ⮁: %9d bytes, %9d bytes\n", tlsWriteSize, tlsReadSize));
            }
            sb.append(String.format("HTTP  ⮁: %9d bytes, %9d bytes\n" +
                            "Total ⮁: %9d bytes, %9d bytes\n\n" +
                            "Reflect: %dms\n",
                    getRequestSize(),
                    getResponseSize(), totalWriteSize, totalReadSize, reflectTimeCost()));
        }

        long dns = dnsTimeCost();
        long tcp = tcpHandshakeTimeCost();
        long server = beforeResponseBodyTimeCost();
        long body = responseBodyTimeCost();

        if (isHttps) {
            long tls = tlsHandshakeTimeCost();
            sb.append('\n').append(String.format("  DNS Lookup   TCP Connection   TLS Handshake   Server Processing   Content Transfer\n" +
                            "[%7dms  |     %7dms  |    %7dms  |        %7dms  |       %7dms  ]\n" +
                            "            |                |               |                   |                  |\n" +
                            "   namelookup:%-9s      |               |                   |                  |\n" +
                            "                       connect:%-9s     |                   |                  |\n" +
                            "                                   pretransfer:%-9s         |                  |\n" +
                            "                                                     starttransfer:%-9s        |\n" +
                            "                                                                                total:%-9s\n",
                    dns, tcp, tls, server, body,
                    String.format("%dms", dns),
                    String.format("%dms", dns + tcp),
                    String.format("%dms", dns + tcp + tls),
                    String.format("%dms", dns + tcp + tls + server),
                    String.format("%dms", dns + tcp + tls + server + body)));
        } else {
            sb.append('\n').append(String.format("   DNS Lookup   TCP Connection   Server Processing   Content Transfer\n" +
                            "[ %7dms  |     %7dms  |        %7dms  |       %7dms  ]\n" +
                            "             |                |                   |                  |\n" +
                            "    namelookup:%-9s      |                   |                  |\n" +
                            "                        connect:%-9s         |                  |\n" +
                            "                                      starttransfer:%-9s        |\n" +
                            "                                                                 total:%-9s\n",
                    dns, tcp, server, body,
                    String.format("%dms", dns),
                    String.format("%dms", dns + tcp),
                    String.format("%dms", dns + tcp + server),
                    String.format("%dms", dns + tcp + server + body)));
        }

        return sb.toString();
    }

    private void findSocket(Connection conn) {
        if (sock != null) return;
        if (isProxy()) return;

        if (conn instanceof RealConnection) {
            long reflectStart = SystemClock.uptimeMillis();
            try {
                findSocketInner((RealConnection) conn);
            } catch (Throwable ignore) {
                Log.d("clark", "", ignore);
            } finally {
                reflectCost = SystemClock.uptimeMillis() - reflectStart;
            }
        }
    }

    private void findSocketInner(RealConnection conn) throws Exception {
        Field field = RealConnection.class.getDeclaredField("rawSocket");
        field.setAccessible(true);
        Socket s = (Socket) field.get(conn);
        if (s instanceof InnerSocket) {
            sock = (InnerSocket) s;
        }
    }

    class LogEventDelegate extends EventDelegate.DefaultEventDelegate {

        @Override
        public void callStart(Call call) {
            callStartWallTime = System.currentTimeMillis();
            timeCounter = new TimeCounter();
        }

        @Override
        public void dnsStart(Call call, String domainName) {
            dispatchCost = timeCounter.count();
        }

        @Override
        public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
            dnsCost = timeCounter.count();
            dnsAddrs = inetAddressList;
        }

        @Override
        public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
            tcpHandshake = true;
        }

        @Override
        public void secureConnectStart(Call call) {
            // tcp handshake end
            tcpCost = timeCounter.count();
        }

        @Override
        public void secureConnectEnd(Call call, Handshake handshake) {
            // dns handshake end
            tlsCost = timeCounter.count();
            RoundTripLog.this.handshake = handshake;
            // can't get CountSocket right now
        }

        @Override
        public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol, IOException ioe) {
            error = ioe;
        }

        @Override
        public void connectionAcquired(Call call, Connection connection) {
            if (connectionReused()) {
                dispatchCost = timeCounter.count();
            }

            Handshake hs = connection.handshake();
            if (handshake == null && hs != null) {
                handshake = hs;
            }

            if (protocol == null) {
                protocol = connection.protocol();
            }

            Route route = connection.route();
            proxyType = route.proxy().type();
            targetAddr = route.socketAddress();

            findSocket(connection);
            if (sock != null) {
                if (connectionReused()) {
                    readSizeCounter = new Counter(sock.getReadCount());
                    writeSizeCounter = new Counter(sock.getWriteCount());
                    readSizeCounterCopy = readSizeCounter.copy();
                    writeSizeCounterCopy = writeSizeCounter.copy();
                } else {
                    // new tcp connection
                    readSizeCounter = new Counter(0);
                    writeSizeCounter = new Counter(0);
                    readSizeCounterCopy = readSizeCounter.copy();
                    writeSizeCounterCopy = writeSizeCounter.copy();

                    tlsReadSize = readSizeCounter.count(sock.getReadCount());
                    tlsWriteSize = writeSizeCounter.count(sock.getWriteCount());
                }
            }
        }

        @Override
        public void requestHeadersEnd(Call call, Request request) {
            isHttps = request.isHttps();
            requestLine = RequestLine.get(request, proxyType);
            requestHeaders = request.headers();

            // notice: request not flush right now!
        }

        @Override
        public void requestBodyEnd(Call call, long byteCount) {
            requestBodySize = byteCount;
        }

        @Override
        public void requestFailed(Call call, IOException ioe) {
            error = ioe;
        }

        @Override
        public void responseHeadersEnd(Call call, Response response) {
            httpCostWithoutResponseBody = timeCounter.count();

            responseCode = response.code();
            responseMessage = response.message();
            responseHeaders = response.headers();

            if (sock != null) {
                responseHeaderSize = readSizeCounter.count(sock.getReadCount());
            }
        }

        @Override
        public void responseBodyEnd(Call call, long byteCount) {
            httpResponseBodyCost = timeCounter.count();

            if (sock != null) {
                long readCount = sock.getReadCount();
                long writeCount = sock.getWriteCount();

                responseBodySize = readSizeCounter.count(readCount);
                requestSize = writeSizeCounter.count(writeCount);

                totalReadSize = readSizeCounterCopy.count(readCount);
                totalWriteSize = writeSizeCounterCopy.count(writeCount);
            }
        }

        @Override
        public void responseFailed(Call call, IOException ioe) {
            error = ioe;
        }

        @Override
        public void callEnd(Call call) {
            RoundTripLogger.DefaultLogger.getInstance().log(RoundTripLog.this);
        }

        @Override
        public void callFailed(Call call, IOException ioe) {
            error = ioe;
        }

        @NotNull
        @Override
        public String toString() {
            return RoundTripLog.this.toString();
        }
    }

    static class Counter {
        private long start;

        Counter(long start) {
            this.start = start;
        }

        Counter() {
            this(0);
        }

        long count(long end) {
            long tempResult = end - start;
            start = end;
            return tempResult;
        }

        Counter copy() {
            return new Counter(start);
        }
    }

    static class TimeCounter extends Counter {
        TimeCounter() {
            super(SystemClock.uptimeMillis());
        }

        long count() {
            return count(SystemClock.uptimeMillis());
        }
    }
}


