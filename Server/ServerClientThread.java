package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerClientThread extends Thread {
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    String STATUS_SUCCESS = "+";
    String STATUS_ERROR = "-";
    String STATUS_LOGGEDIN = "! ";
    String LOGIN_DB = "login.txt";

    Socket clientSocket;
    String status;
    String serverMsg;
    String userId;
    String userAcc;
    String transmissionType = "B";
    boolean isLoggedIn = false;

    public ServerClientThread(Socket inSocket) throws IOException {
        clientSocket = inSocket;
    }

    public void run() {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new DataOutputStream(clientSocket.getOutputStream());

            InetAddress ip = InetAddress.getLocalHost();
            String hostname = ip.getHostName();
            outToClient.writeBytes("+" + hostname + " RFC-913 SFTP\n");
            outToClient.flush();

            loop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loop() throws IOException {
        String clientMsg;
        String[] clientCmd;

        while ((clientMsg = inFromClient.readLine()) != null) {
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
                        PASS(clientCmd[1]);
                        break;
                    case "TYPE":
                        TYPE(clientCmd[1]);
                        break;
                    case "LIST":
                        LIST(clientCmd[1]);
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
                        DONE(inFromClient, outToClient);
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
            } catch (ArrayIndexOutOfBoundsException e) {
                status = STATUS_ERROR;
                serverMsg = "Valid command, but insufficient arguments given. Please try again.";
            }
            String response = status + serverMsg + "\n";
            outToClient.writeBytes(response);
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

    public void PASS(String pass_input) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(LOGIN_DB));
        String[] userInfo;

        do {
            userInfo = br.readLine().split(",");
        } while (!userInfo[0].equals(userId));

        boolean passwordSatisfied = false;

        if (userInfo.length > 2) {
            for (String password : userInfo[2].split("\\|")) {
                if (pass_input.equals(password)) {
                    passwordSatisfied = true;
                    break;
                }
            }

            if (passwordSatisfied) {
                if (userInfo[1].equals("") || userAcc != null) {
                    status = STATUS_LOGGEDIN;
                    isLoggedIn = true;
                    serverMsg = "Password is correct (or not needed), logged in.";
                } else {
                    status = STATUS_SUCCESS;
                    serverMsg = "Password valid (or not needed), send account.";
                }
            } else {
                status = STATUS_ERROR;
                serverMsg = "Wrong password, try again.";
            }
        }
        else {
            status = STATUS_ERROR;
            if (isLoggedIn) {
                serverMsg = "Already logged in.";
            }
            else {
                serverMsg = "No passwords associated with this user. Please send account.";
            }
        }
    }

    public void TYPE(String mode) {
        if (isLoggedIn) {
            switch (mode.toUpperCase()) {
                case "A":
                    transmissionType = mode.toUpperCase();
                    status = STATUS_SUCCESS;
                    serverMsg = "Using ASCII mode.";
                    break;
                case "B":
                    transmissionType = "B";
                    status = STATUS_SUCCESS;
                    serverMsg = "Using binary mode.";
                    break;
                case "C":
                    transmissionType = "C";
                    status = STATUS_SUCCESS;
                    serverMsg = "Using continuous mode.";
                    break;
                default:
                    status = STATUS_ERROR;
                    serverMsg = "Invalid type. Please follow the command 'TYPE' with 'A' (ASCII), 'B' (Byte) or 'C' (Continuous) to select transmission mode.";
                    break;
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void LIST(String list_cmd) {
        String[] list_args = list_cmd.split("\\s+");

        String dirToList;

        if (list_args.length > 1) {
            dirToList = list_args[1];
        }
        else {
            dirToList = System.getProperty("user.dir") + File.separator + "..";
        }
        Set<String> listedfiles;

        switch (list_args[0].toUpperCase()) {
            case "F":
                listedfiles = Stream.of(new File(dirToList).listFiles())
                        .filter(file -> !file.isDirectory())
                        .map(File::getName)
                        .collect(Collectors.toSet());
                status = STATUS_SUCCESS;
                serverMsg = String.join(" ", listedfiles);
                break;
            case "V":
                // TODO: make verbose
                listedfiles = Stream.of(new File(dirToList).listFiles())
                        .filter(file -> !file.isDirectory())
                        .map(File::getName)
                        .collect(Collectors.toSet());
                status = STATUS_SUCCESS;
                serverMsg = String.join(" ", listedfiles);
                break;
            default:
                status = STATUS_ERROR;
                serverMsg = "Specify a listing format. Please follow the command 'LIST' with 'F' (standard formatting) or 'V' (verbose formatting).";
                break;
        }
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

    public void DONE(BufferedReader inputStream,
                     DataOutputStream outputStream) throws IOException {
        status = STATUS_SUCCESS;
        serverMsg = "Connection closed.";
        outputStream.writeBytes(status + serverMsg + "\n");
        inputStream.close();
        outputStream.close();
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