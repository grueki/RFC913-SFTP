package Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

class ServerClientThread extends Thread {
    String STATUS_SUCCESS = "+";
    String STATUS_ERROR = "-";
    String STATUS_LOGGEDIN = "!";

    Socket clientSocket;
    int clientNumber;
    String status;
    String serverMsg;

    ServerClientThread(Socket inSocket, int count) {
        clientSocket = inSocket;
        clientNumber = count;
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());

            String clientMsg = "";
            String[] clientCmd;

            InetAddress ip = InetAddress.getLocalHost();
            String hostname = ip.getHostName();
            outToClient.writeBytes("+" + hostname + " RFC-913 SFTP\n");

            while(true) {
                clientMsg = inFromClient.readLine();
                clientCmd = clientMsg.split("\\s+");
                switch (clientCmd[0].toUpperCase()) {
                    case "USER":
                        USER();
                        break;
                    case "ACCT":
                        ACCT();
                        break;
                    case "PASS":
                        PASS();
                        break;
                    case "TYPE":
                        TYPE();
                        break;
                    case "LIST":
                        LIST();
                        break;
                    case "CDIR":
                        CDIR();
                        break;
                    case "KILL":
                        KILL();
                        break;
                    case "NAME":
                        NAME();
                        break;
                    case "DONE":
                        DONE();
                        break;
                    case "RETR":
                        RETR();
                        break;
                    case "STOR":
                        STOR();
                        break;
                    default:
                        status = STATUS_ERROR;
                        serverMsg = "Invalid command";
                        break;
                }
                outToClient.writeBytes(status + serverMsg + "\n");
            }

//            inFromClient.close();
//            outToClient.close();
//            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Client " + clientNumber + " has disconnected.");
        }
    }

    public void USER() {
        System.out.println("User command");
        status = STATUS_SUCCESS;
        serverMsg = "User command sent to server!";
    }

    public void ACCT() {
        System.out.println("Account command");
        status = STATUS_SUCCESS;
        serverMsg = "Account command sent to server!";
    }

    public void PASS() {
        System.out.println("Password command");
        status = STATUS_SUCCESS;
        serverMsg = "Password command sent to server!";
    }

    public void TYPE() {
        System.out.println("Type command");
        status = STATUS_SUCCESS;
        serverMsg = "Type command sent to server!";
    }

    public void LIST() {
        System.out.println("List command");
        status = STATUS_SUCCESS;
        serverMsg = "List command sent to server!";
    }

    public void CDIR() {
        System.out.println("Current directory command");
        status = STATUS_SUCCESS;
        serverMsg = "Current directory command sent to server!";
    }

    public void KILL() {
        System.out.println("Delete command");
        status = STATUS_SUCCESS;
        serverMsg = "Delete command sent to server!";
    }

    public void NAME() {
        System.out.println("Rename command");
        status = STATUS_SUCCESS;
        serverMsg = "Rename command sent to server!";
    }

    public void DONE() {
        System.out.println("Done command");
        status = STATUS_SUCCESS;
        serverMsg = "Done command sent to server!";
    }

    public void RETR() {
        System.out.println("Request command");
        status = STATUS_SUCCESS;
        serverMsg = "Request command sent to server!";
    }

    public void STOR() {
        System.out.println("Store command");
        status = STATUS_SUCCESS;
        serverMsg = "Store command sent to server!";
    }

}