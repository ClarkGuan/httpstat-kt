package com.httpstat.kotlin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class InnerSocket extends Socket {
    private InnerInputStream inputStream;
    private InnerOutputStream outputStream;

    public InnerSocket() {
        super();
    }

    public InnerSocket(String host, int port) throws IOException, UnknownHostException {
        super(host, port);
    }

    public InnerSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    public InnerSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        super(host, port, localAddr, localPort);
    }

    public InnerSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        super(address, port, localAddr, localPort);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new InnerInputStream(super.getInputStream());
        }
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new InnerOutputStream(super.getOutputStream());
        }
        return outputStream;
    }

    long getReadCount() {
        return inputStream.getCount();
    }

    long getWriteCount() {
        return outputStream.getCount();
    }
}

class InnerInputStream extends InputStream {
    private InputStream inner;

    private long count;

    InnerInputStream(InputStream in) {
        inner = in;
    }

    @Override
    public int read() throws IOException {
        int n = inner.read();
        count += 1;
        return n;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int n = inner.read(b);
        count += n;
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = inner.read(b, off, len);
        count += n;
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        long ret = inner.skip(n);
        count += ret;
        return ret;
    }

    @Override
    public int available() throws IOException {
        return inner.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    long getCount() {
        return count;
    }
}

class InnerOutputStream extends OutputStream {
    private OutputStream inner;

    private long count;

    InnerOutputStream(OutputStream os) {
        inner = os;
    }

    @Override
    public void write(int b) throws IOException {
        inner.write(b);
        count++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        inner.write(b);
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        inner.write(b, off, len);
        count += len;
    }

    @Override
    public void flush() throws IOException {
        inner.flush();
    }

    @Override
    public void close() throws IOException {
        inner.close();
    }

    long getCount() {
        return count;
    }
}