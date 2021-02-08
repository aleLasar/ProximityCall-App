package com.example.project;

import android.content.Intent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerPart extends Thread{
    private MainActivity mainActivity;
    private Socket socket;
    private ServerSocket serverSocket;
    private int PORT;

    public ServerPart(MainActivity mainActivity, int port){
        this.mainActivity = mainActivity;
        this.PORT = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            socket = serverSocket.accept();
            SocketHandler.setSocket(socket);
            mainActivity.startActivity(new Intent(mainActivity, MicWindow.class));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
