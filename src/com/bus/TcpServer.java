package com.bus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Vector;

public class TcpServer extends Thread {
    private ServerSocket server;
    private Socket client;
    private String password;
    private boolean is_listening;
    private boolean is_has_partner;

    private ChatBus chat_bus;

    public TcpServer(ChatBus chat_bus) {
        this.server = null;
        this.client = null;
        this.password = null;
        this.is_listening = false;
        this.is_has_partner = false;
        this.chat_bus = chat_bus;
    }

    public void startListeningOnTcpServer(String host, int port, String password) throws IOException {
        if(this.is_listening == false) {
            InetSocketAddress endpoint = new InetSocketAddress(host, port);
            this.password = password;
            this.server = new ServerSocket();
            this.server.bind(endpoint);
            this.is_listening = true;

            new Thread(this).start();
        }
    }

    public void stopListeningOnTcpServer() throws IOException {
        if(this.is_listening == true) {
            this.server.close();
            if(this.client != null) this.client.close();
            this.is_listening = false;
            this.is_has_partner = false;
        }
    }

    public void waitingConnectionFromClient() throws IOException {
        this.client = this.server.accept();
        DataOutputStream dos = new DataOutputStream(this.client.getOutputStream());
        DataInputStream dis = new DataInputStream(this.client.getInputStream());
        String password = dis.readUTF();
        String result = null;

        if(this.is_has_partner) result = "busy";
        else if(this.password.equals(password)) {
            result = "true";
            this.chat_bus.setSocket(this.client);
            this.is_has_partner = true;
        }
        else result = "false";
        dos.writeUTF(result);
    }

    public Vector<String> getAllIpv4AddressesOnLocal() throws SocketException {
        Vector<String> ipv4_addresses = new Vector<>();
        Enumeration networks = NetworkInterface.getNetworkInterfaces();
        while(networks.hasMoreElements()) {
            NetworkInterface sub_networks = (NetworkInterface) networks.nextElement();
            Enumeration inet_addresses = sub_networks.getInetAddresses();
            while(inet_addresses.hasMoreElements()) {
                try {
                    Inet4Address ipv4 = (Inet4Address) inet_addresses.nextElement();
                    ipv4_addresses.add(ipv4.getHostAddress());
                }
                catch(Exception e) {
                    // TODO: pass ip version 6
                }
            }
        }
        return ipv4_addresses;
    }

    @Override
    public void run() {
        while(this.is_listening) {
            try {
                this.waitingConnectionFromClient();
            }
            catch(Exception e) {}
        }
    }

    public boolean isListening() {
        return this.is_listening;
    }

    public boolean isHasPartner() {
        return this.is_has_partner;
    }

    public void setHasPartner(boolean b) {
        this.is_has_partner = b;
    }
}