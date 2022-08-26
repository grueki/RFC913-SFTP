package Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

class Server {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;

    private ServerSocket serverSocket;
//    private Socket clientSocket;
//
//    private BufferedReader inFromClient;
//    private DataOutputStream outToClient;

    class ServerClientThread extends Thread {
        Socket clientSocket;
        int clientNumber;

        ServerClientThread(Socket inSocket, int count) {
            clientSocket = inSocket;
            clientNumber = count;
        }

        public void run() {
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());

                String clientMsg = "";
                String serverMsg = "";

                InetAddress ip = InetAddress.getLocalHost();
                String hostname = ip.getHostName();
                outToClient.writeBytes("+" + hostname + ": Welcome to the server! You're now connected on port " + PORT_NUM + ".\n");

                while(!"DONE".equals(clientMsg)) {
                    clientMsg = inFromClient.readLine();
                    System.out.println("Recieved \"" + clientMsg + "\" from client " + clientNumber);
                    serverMsg = "Thanks for your message: " + clientMsg + "!\n";
                    outToClient.writeBytes(serverMsg);
                    outToClient.flush();
                }
                inFromClient.close();
                outToClient.close();
                clientSocket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                System.out.println("Client " + clientNumber + " has disconnected.");
            }
        }
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT_NUM);
        System.out.println("Server listening on port " + PORT_NUM + "!");

        int counter = 0;
        while (true) {
            counter++;
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client number " + counter + " connected!");
            ServerClientThread newClientThread = new ServerClientThread(clientSocket, counter);
            newClientThread.start();
        }
    }

//    public boolean loop() throws IOException {
//        String inputLine;
//        if ((inputLine = inFromClient.readLine()) != null) {
//            if ("DONE".equals(inputLine)) {
//                return true;
//            }
//            else {
//                outToClient.writeBytes("Recieved your message, \"" + inputLine + "\"! :)\n");
//            }
//        }
//        return false;
//    }

    public void stop() throws IOException {
        serverSocket.close();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        Server myServer = new Server();
        myServer.start();
    }
}
