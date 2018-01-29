package com.example.lib;

import com.africastalking.AfricasTalking;
import com.africastalking.Server;

import java.io.IOException;

public class PaymentServer {
    private static final int RPC_PORT = 35897;
    public static void main(String[] args){
        System.out.print("Starting.......");
        AfricasTalking.initialize("sandbox","70d4efd0f7abb9ff0bc87473a9657bddbc1dd8aae5fb6af6512285e06292ad49");
        Server server = new Server();

        try {
            server.startInsecure(RPC_PORT);
            while (true) {
                Thread.sleep(30000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
