package ds.assign.entropy;

import ds.assign.entropy.PoissonProcess;
import ds.assign.entropy.RequestGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Peer {
    String host;
    Logger logger;
    PoissonProcess poissonProcess;


    public Peer(String hostname) {
        host = hostname;
        Random rng = new Random();
        double lambda = 10.0 ;


        logger = Logger.getLogger("logfile");
        try {
            FileHandler handler = new FileHandler("./" + hostname + "_peer.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        poissonProcess = new PoissonProcess(lambda, rng);
    }

    public static void main(String[] args) throws Exception {
        Peer peer = new Peer(args[0]);
        System.out.printf("new peer @ host=%s\n", args[0]);
        Server server = new Server(args[0], Integer.parseInt(args[1]), peer.logger, args[2], Integer.parseInt(args[3]));
        new Thread(server).start();
        RequestGenerator requestGenerator = new RequestGenerator(args[0], peer.logger, peer.poissonProcess, Integer.parseInt(args[1]),server);
        new Thread(requestGenerator).start();

    }


}

class Server implements Runnable {
    String host;
    int port;
    ServerSocket server;
    Logger logger;

    Queue<String> words = new LinkedList<>();
    Queue<String> words2 = new LinkedList<>();
    String hostNext;
    int portNext;


    public Server(String host, int port, Logger logger, String hostNext, int portNext) throws Exception {
        this.host = host;
        this.port = port;
        this.logger = logger;
        server = new ServerSocket(port, 1, InetAddress.getByName(host));
        this.hostNext = hostNext;
        this.portNext = portNext;


    }

    public void addWords(String operation) {
        words.add(operation);
    }



    @Override
    public void run() {

        logger.info("server: endpoint running at port " + port + " ...");
        while (true) {
            try {

                Socket client = server.accept();

                String clientAddress = client.getInetAddress().getHostAddress();
                logger.info("server: new connection from " + clientAddress);




            } catch (IOException io) {
                io.printStackTrace();
            }

            synchronized (words) {



                while (!words.isEmpty()) {

                    try {
                        String req = words.poll();
                        words2.add(req);
                        Socket socket = new Socket("localhost", 3000);
                        PrintWriter outCalc = new PrintWriter(socket.getOutputStream(), true);
                        outCalc.println(req);
                        outCalc.flush();

                        System.out.println("List of words " + words2);
                        System.out.flush();
                        socket.close();
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }

            }

            try {
                Socket socket = new Socket(hostNext, portNext);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                out.flush();
                socket.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }
}