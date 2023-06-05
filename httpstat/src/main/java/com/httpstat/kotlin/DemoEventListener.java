package com.httpstat.kotlin;

import okhttp3.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.WeakHashMap;

public class DemoEventListener extends EventListener {
    private WeakHashMap<Call, EventDelegate> delegates;

    public DemoEventListener() {
        delegates = new WeakHashMap<>();
    }

    @Override
    public void callStart(Call call) {
        EventDelegate delegate = new RoundTripLog().getDelegate();
        delegate.callStart(call);
        delegates.put(call, delegate);
    }

    @Override
    public void dnsStart(Call call, String domainName) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.dnsStart(call, domainName);
        }
    }

    @Override
    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.dnsEnd(call, domainName, inetAddressList);
        }
    }

    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.connectStart(call, inetSocketAddress, proxy);
        }
    }

    @Override
    public void secureConnectStart(Call call) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.secureConnectStart(call);
        }
    }

    @Override
    public void secureConnectEnd(Call call, Handshake handshake) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.secureConnectEnd(call, handshake);
        }
    }

    @Override
    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.connectEnd(call, inetSocketAddress, proxy, protocol);
        }
    }

    @Override
    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol, IOException ioe) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        }
    }

    @Override
    public void connectionAcquired(Call call, Connection connection) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.connectionAcquired(call, connection);
        }
    }

    @Override
    public void connectionReleased(Call call, Connection connection) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.connectionReleased(call, connection);
        }
    }

    @Override
    public void requestHeadersStart(Call call) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.requestHeadersStart(call);
        }
    }

    @Override
    public void requestHeadersEnd(Call call, Request request) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.requestHeadersEnd(call, request);
        }
    }

    @Override
    public void requestBodyStart(Call call) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.requestBodyStart(call);
        }
    }

    @Override
    public void requestBodyEnd(Call call, long byteCount) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.requestBodyEnd(call, byteCount);
        }
    }

    @Override
    public void requestFailed(Call call, IOException ioe) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.requestFailed(call, ioe);
        }
    }

    @Override
    public void responseHeadersStart(Call call) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.responseHeadersStart(call);
        }
    }

    @Override
    public void responseHeadersEnd(Call call, Response response) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.responseHeadersEnd(call, response);
        }
    }

    @Override
    public void responseBodyStart(Call call) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.responseBodyStart(call);
        }
    }

    @Override
    public void responseBodyEnd(Call call, long byteCount) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.responseBodyEnd(call, byteCount);
        }
    }

    @Override
    public void responseFailed(Call call, IOException ioe) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.responseFailed(call, ioe);
        }
    }

    @Override
    public void callEnd(Call call) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.callEnd(call);

            delegates.remove(call);
        }
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        EventDelegate delegate = delegates.get(call);
        if (delegate != null) {
            delegate.callFailed(call, ioe);
        }
    }

}
