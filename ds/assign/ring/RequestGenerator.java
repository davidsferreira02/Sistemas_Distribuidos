package ds.assign.ring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Logger;

public class RequestGenerator implements Runnable {
    private final String host;
    private final Logger logger;
    private final PoissonProcess poissonProcess;

    private final int localPort;
    private final Server server;


    public RequestGenerator(String host, Logger logger, PoissonProcess poissonProcess,int localPort,Server server) {
        this.host = host;
        this.logger = logger;
        this.poissonProcess = poissonProcess;
        this.localPort=localPort;
        this.server=server;

    }

    @Override
    public void run() {
        while (true) {
            double interArrivalTime = poissonProcess.timeForNextEvent() * 1000; // Converting to milliseconds
            try {
                Thread.sleep((long) interArrivalTime);
                String request = generateRandomRequest();
                sendRequestToServer(request,localPort);
                synchronized (request){
                    server.addOperations(request);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateRandomRequest() {

        String[] operations = { "add", "sub", "mul", "div" };

        Random random = new Random();
        String operation = operations[random.nextInt(operations.length)];


        double arg1 = random.nextDouble() * 100;
        double arg2 = random.nextDouble() * 100;

        String request = String.format("%s:%.2f:%.2f", operation, arg1, arg2).replace(',', '.');

        return request;
    }


   private void sendRequestToServer(String request,int serverPort) {
        String serverAddress = "localhost";


        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {


            out.println(request);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
