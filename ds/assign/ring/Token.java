package ds.assign.ring;

import java.io.*;
import java.net.Socket;

public class Token {
    public static void sendToken(String host, int port, String token) {
        try {
            Socket socket = new Socket(host, port);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter out = new PrintWriter(outputStream, true);
            out.println(token);
            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Token <host> <port> <token>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String tokenValue = args[2];


        sendToken(host, port, tokenValue);

        System.out.println("Token sent successfully.");
    }
}
