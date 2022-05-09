package com.emmanuel.ktpservice;


import com.emmanuel.utils.IOUtils;
import com.emmanuel.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * The KtpClient class provides simple methods for connecting,
 * sending, and receiving objects or data over a network in synchronous blocking mode.
 */
public class KtpClient {
    private  final Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;


    public KtpClient(Socket socket) {
        this.socket = socket;
    }

    public KtpClient() {
        socket = new Socket();
    }

    /**
     * Gets the underlying socket object of this connection.
     * @return The socket object of the connection.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Gets the ip address of the connected remote client.
     * @return A string value representing the remote ip address.
     */
    public String getRemoteAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    /**
     * Gets the port of the connected remote client.
     * @return An integer value representing the remote port.
     */
    public int getRemotePort() {
        return socket.getPort();
    }

    /**
     * Connects the client to a remote Ktp host using the specified IP address and port number.
     * @param host The IPAddress of the host to which you intend to connect.
     * @param port The port number to which you intend to connect.
     * @throws IOException
     */
    public void connect(String host, int port) throws IOException {
        connect(host, port, 0);
    }

    /**
     * Connects the client to a remote Ktp host using the specified IP address and port number.
     * @param host The IPAddress of the host to which you intend to connect.
     * @param port The port number to which you intend to connect.
     * @param timeout The the timeout value to be used in milliseconds.
     * @throws IOException
     */
    public void connect(String host, int port, int timeout) throws IOException {
        if (port > 65535) {
            throw new IllegalArgumentException("port cannot be greater than 65535!");
        }
        InetAddress inetAddress = InetAddress.getByName(host);
        SocketAddress address = new InetSocketAddress(inetAddress, port);
        socket.connect(address, timeout);
    }

    /**
     * Writes data to the Ktp stream from a byte array..
     * @param data The data to be written to the stream.
     * @throws IOException
     */
    public void write(byte[] data) throws IOException {
        if (outputStream == null) {
            outputStream = socket.getOutputStream();
        }
        byte[] responseBuffer = getWriteData(data);
        outputStream.write(responseBuffer);
        outputStream.flush();
    }

    /**
     * Serializes and writes a java object to the ktp stream.
     * @param value The object to write to the stream.
     * @throws IOException
     */
    public void sendObject(Object value) throws IOException {
        if (outputStream == null) {
            outputStream = socket.getOutputStream();
        }
        if (value == null) {
            // null buffer or simply writing 0 to the stream can be used to send
            // keep-alive messages to the client.
            outputStream.write(new byte[]{0});
            outputStream.flush();
            return;
        }
        String json = Utils.serializeObjectToJson(value);
        byte[] bodyBuffer = json.getBytes(Charset.forName("UTF-8"));
        write(bodyBuffer);
    }

    /**
     * Reads data from the Ktp stream sent from the remote connection.
     * @throws IOException
     */
    public byte[] read() throws IOException {
        if (inputStream == null) {
            inputStream = socket.getInputStream();
        }
        int headerValue = inputStream.read();
        long contentLength;
        switch (headerValue) {
            case -1:
                throw new SocketException();
            case 0:
                return null;
            case 1:
                contentLength = inputStream.read();
                break;
            case 2:
                byte[] contentLengthBuffer = IOUtils.readInputStream(inputStream, 2);
                contentLength = Utils.bytesToUInt16(contentLengthBuffer, true);
                break;
            case 4:
                contentLengthBuffer = IOUtils.readInputStream(inputStream, 4);
                contentLength = Utils.bytesToUInt32(contentLengthBuffer, true);
                break;
            default:
                throw new SocketException("Error reading socket values!");
        }
        return IOUtils.readInputStream(inputStream, (int) contentLength);
    }

    /**
     * Reads an object from the Ktp stream sent from the remote connection and
     * deserializes the object to specified type.
     * @param typeParameterClass The Class type of the object to be read.
     * @throws IOException
     */
    public <T> T receiveData(Class<T> typeParameterClass) throws IOException {
        byte[] data = read();
        if(data == null){
            return null;
        }
        //int byteRead = inputStream.read(bodyBuffer);
        String json = new String(data, "UTF-8");
        //System.out.println(json);
        try {
            return Utils.deserializeObjectFromJson(typeParameterClass, json);
        } catch (Exception e) {
            throw e;
        }
    }

    private static byte[] getWriteData(byte[] bodyBuffer) {
        if (bodyBuffer.length == 1 && bodyBuffer[0] == (byte) 0) {
            return bodyBuffer;
        }
        byte headerByte;
        byte[] contentLengthBuffer;
        int messageSize = bodyBuffer.length;
        if (messageSize <= 255) {
            headerByte = 1;
            contentLengthBuffer = new byte[headerByte];
            contentLengthBuffer[0] = (byte) messageSize;
        } else if (messageSize <= 65535) {
            headerByte = 2;
            contentLengthBuffer = Utils.uInt16ToBytes(messageSize, ByteOrder.BIG_ENDIAN);
        } else {
            headerByte = 4;
            contentLengthBuffer = Utils.uInt32ToBytes(messageSize, ByteOrder.BIG_ENDIAN);
        }
        byte[] responseBuffer = new byte[1 + contentLengthBuffer.length + bodyBuffer.length];
        responseBuffer[0] = headerByte;
        System.arraycopy(contentLengthBuffer, 0, responseBuffer, 1, contentLengthBuffer.length);
        System.arraycopy(bodyBuffer, 0, responseBuffer, 1 + contentLengthBuffer.length, bodyBuffer.length);
        return responseBuffer;
    }

    /**
     * Closes the Ktp connection and frees resources.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
