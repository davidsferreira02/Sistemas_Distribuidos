package ds.assign.entropy;

import ds.assign.entropy.PoissonProcess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class RequestGenerator implements Runnable {
    private final String host;
    private final Logger logger;
    private final PoissonProcess poissonProcess;

    private final int localPort;
    private final Server server;

    private ArrayList<String> wordsList;


    public RequestGenerator(String host, Logger logger, PoissonProcess poissonProcess, int localPort, Server server) {
        this.host = host;
        this.logger = logger;
        this.poissonProcess = poissonProcess;
        this.localPort=localPort;
        this.server=server;
        wordsList = new ArrayList<>();
        loadWordsFromFile("ds/assign/entropy/dictionary.txt");
    }

    @Override
    public void run() {
        while (true) {
            double interArrivalTime = poissonProcess.timeForNextEvent() * 1000*60; // Converting to milliseconds
            try {
                Thread.sleep((long) interArrivalTime);
                String request = generateRandomRequest();
                sendRequestToServer(request,localPort);
                synchronized (request){
                    server.addWords(request);

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void loadWordsFromFile(String filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                wordsList.add(line.trim());
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private String generateRandomRequest() {
        Random random = new Random();
        String newWord = wordsList.get(random.nextInt(wordsList.size()));




        String request = String.format("%s", newWord);

        return request;
    }


   private void sendRequestToServer(String request,int serverPort) {
        String serverAddress = "localhost";
        try {
            Socket socket = new Socket(serverAddress, serverPort);

             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(request);
                out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
