package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class ServerClientThread extends Thread {
    String STATUS_SUCCESS = "+";
    String STATUS_ERROR = "-";
    String STATUS_LOGGEDIN = "! ";
    String LOGIN_DB = "login.txt";

    Socket clientSocket;
    int clientNumber;
    String status;
    String serverMsg;
    String userId;
    String userAcc;
    boolean isLoggedIn = false;

    ServerClientThread(Socket inSocket, int count) throws IOException {
        clientSocket = inSocket;
        clientNumber = count;
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());

            InetAddress ip = InetAddress.getLocalHost();
            String hostname = ip.getHostName();
            outToClient.writeBytes("+" + hostname + " RFC-913 SFTP\n");

            loop(inFromClient, outToClient);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Client " + clientNumber + " has disconnected.");
        }
    }

    public void loop(BufferedReader inFromClient,
                     DataOutputStream outToClient) throws IOException {
        String clientMsg;
        String[] clientCmd;
        while((clientMsg = inFromClient.readLine()) != null) {
            clientCmd = clientMsg.split("\\s+");

            try {
                switch (clientCmd[0].toUpperCase()) {
                    case "USER":
                        USER(clientCmd[1]);
                        break;
                    case "ACCT":
                        ACCT(clientCmd[1]);
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
                        DONE(outToClient);
                        return;
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
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                status = STATUS_ERROR;
                serverMsg = "Valid command, but insufficient arguments given. Please try again.";
            }
            outToClient.writeBytes(status + serverMsg + "\n");
        }
    }

    public void USER(String user_input) throws IOException {
        List<String> users = Files.readAllLines(Paths.get(LOGIN_DB));
        boolean foundUser = false;

        String[] userInfo = new String[0];

        for (String user : users) {
            userInfo = user.split(",");
            if (user_input.equals(userInfo[0])) {
                foundUser = true;
                userId = userInfo[0];
                break;
            }
        }

        if (foundUser) {
            if (userInfo.length < 2) {
                status = STATUS_LOGGEDIN;
                isLoggedIn = true;
                serverMsg = "Logged in as " + userId;
            }
            else {
                status = STATUS_SUCCESS;
                serverMsg = "User-id valid, send account and/or password.";
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = user_input + " is not a valid user-id. Please try again.";
        }
    }

    public void ACCT(String acc_input) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(LOGIN_DB));
        String[] userInfo;

        do {
            userInfo = br.readLine().split(",");
        } while (!userInfo[0].equals(userId));

        boolean foundAcc = false;

        if (userInfo.length > 1) {
            for (String account : userInfo[1].split("\\|")) {
                if (acc_input.equals(account)) {
                    foundAcc = true;
                    userAcc = account;
                    break;
                }
            }

            if (foundAcc || userInfo[1].equals("")) {
                if (userInfo.length < 3) {
                    status = STATUS_LOGGEDIN;
                    isLoggedIn = true;
                    serverMsg = "Account valid (or not needed), logged in.";
                } else {
                    status = STATUS_SUCCESS;
                    serverMsg = "Account valid (or not needed), send password.";
                }
            } else {
                status = STATUS_ERROR;
                serverMsg = "Invalid account, try again.";
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "Already logged in. No accounts associated with this user.";
        }
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

    public void DONE(DataOutputStream outputStream) throws IOException {
        System.out.println("Done command");
        status = STATUS_SUCCESS;
        serverMsg = "Connection closed.";
        outputStream.writeBytes(status + serverMsg + "\n");
        clientSocket.close();
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