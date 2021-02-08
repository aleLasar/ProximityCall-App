package com.example.project;

import android.content.Intent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientPart extends Thread{
    private MainActivity mainActivity;
    private Socket socket;
    private String address;
    private int PORT;

    public ClientPart(MainActivity mainActivity, String address, int port){
        this.address = address;
        this.mainActivity = mainActivity;
        this.socket = new Socket();
        this.PORT = port;
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(address, PORT), 500);
            SocketHandler.setSocket(socket);
            mainActivity.startActivity(new Intent(mainActivity, MicWindow.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
