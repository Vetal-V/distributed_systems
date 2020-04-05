package lpi.client.soap;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class CommandHandler {

    private IChatServer serverProxy; //object of interface IChatServer
    private String sectionId = null; //sting for section ID
    private Timer timer; //object class of Timer

    CommandHandler(IChatServer serverProxy) { //constructor of class CommandHandler
        this.serverProxy = serverProxy;
    }

    boolean checkLogin(String sectionId){ //method to check the logging of user
        if (sectionId != null)
            return true;
         else{
            System.out.println("This command requires login first.");
            return false;
        }

    }

    void run() throws IOException { //method run of class CommandHandler
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  //input buffer stream from user

        boolean ifLoop = true;
        while (ifLoop) { //loop for entering command
            System.out.println("\nEnter the command (help - list of available commands): ");
            String command = br.readLine(); //read command of user

            switch (command) {
                case "exit": //complete client execution
                    ifLoop = false;
                    timer.cancel(); //cancel thread of timer
                    if (sectionId != null){
                        try{
                            serverProxy.exit(sectionId); //call the method exit
                        } catch (ArgumentFault | ServerFault argumentFault) {
                            argumentFault.printStackTrace();
                        }
                        sectionId = null;
                    }
                    break;

                case "ping": //command ping
                    System.out.println(" You choose the command ping.");
                    serverProxy.ping(); //call the method ping
                    System.out.println(" Ping successful.");
                    break;

                case "echo": //command echo
                    System.out.println(" You choose the command echo.");
                    System.out.print(" Enter the echo text: ");
                    String echoText = br.readLine(); //read echo text from user

                    String echoResponse = serverProxy.echo(echoText); //call the method echo
                    System.out.println("Received from server: " + echoResponse);
                    break;

                case "login": //command login
                    System.out.println(" You choose the command login.");
                    System.out.print(" Enter the login: ");
                    String loginName = br.readLine(); //read login from user
                    System.out.print(" Enter the password: ");
                    String loginPass = br.readLine(); //read password from user
                    try{
                        sectionId = serverProxy.login(loginName, loginPass); //call the method login

                        if (sectionId != null){ //successful login
                            System.out.println(" Successful login");

                            ReceiveTimer receive = new ReceiveTimer(this.serverProxy, this.sectionId); //create object of class ReceiveTimer
                            timer = new Timer(true); //init timer daemon
                            timer.scheduleAtFixedRate(receive, 0, 2000); //set frequency  for thread
                        }
                    } catch (ArgumentFault | LoginFault | ServerFault argumentFault) {
                        argumentFault.printStackTrace();
                    }
                    break;

                case "list": //command list
                    System.out.println("You choose the command list.");
                    if (checkLogin(sectionId)) {
                        List<String> listUsers = null; //list of strings to query the list of users
                        try {
                            listUsers = serverProxy.listUsers(sectionId); //call the method listUsers of interface IChatServer
                        } catch (ArgumentFault | ServerFault ex) {
                            System.out.println(ex.getMessage());
                        }

                        if (listUsers != null) { //print the list of active user
                            System.out.print("List of active user name: ");
                            for (String s : listUsers) System.out.print(s + " ");
                        }
                        System.out.println();
                    }
                    break;

                case "msg": //command msg
                    if (checkLogin(sectionId)) {
                        System.out.println("You choose the command msg.");
                        System.out.print("Enter the login of receiver: ");
                        String msgLogin = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the message: ");
                        String msgText = br.readLine(); //read message from user

                        Message msg = new Message(); //create the object of class Message
                        msg.setMessage(msgText); //set the text of message
                        msg.setReceiver(msgLogin); //set the login of receiver
                        try {
                            serverProxy.sendMessage(sectionId, msg); //call the method sendMessage of interface IChatServer
                            System.out.println("Message sent successfully.");
                        } catch (ArgumentFault | ServerFault ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                    break;

                case "recMsg": //command receive message
                    if (checkLogin(sectionId)) {
                        System.out.println("You choose the command receive message.");
                        if (ReceiveTimer.getCountMsg() == 0) { //no message
                            System.out.println("You don't have message.");
                        } else if (ReceiveTimer.getCountMsg() == 1) { //one message
                            System.out.println("You have the message:");
                            System.out.println(" Sender: " + ReceiveTimer.recMsg[0].getSender());
                            System.out.println(" Message: " + ReceiveTimer.recMsg[0].getMessage());
                            ReceiveTimer.setCountMsg(0); //reset the counter
                        } else { //more then one message
                            System.out.println("You have messages:");
                            for (int i = 0; i < ReceiveTimer.getCountMsg(); i++) {
                                System.out.println(" Sender: " + ReceiveTimer.recMsg[i].getSender());
                                System.out.println(" Message: " + ReceiveTimer.recMsg[i].getMessage() + "\n");
                            }
                            ReceiveTimer.setCountMsg(0); //reset the counter
                        }
                    }
                    break;

                case "file": //command file
                    if (checkLogin(sectionId)) {
                        System.out.println("You choose the command file.");
                        System.out.print("Enter the login of receiver: ");
                        String fileReceiver = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the path to file: ");
                        String filePath = br.readLine(); //read the path to file from user
                        File file = null;
                        byte[] fileContent = new byte[0];
                        try{
                            file = new File(filePath); //create the object of class File
                            fileContent = Files.readAllBytes(file.toPath()); //read bytes from file
                        } catch (Exception ignored) {}

                        assert file != null;
                        if (file.exists() && file.isFile()){ //checking for file existence
                            FileInfo fileInfo = new FileInfo(); //create the object of class FileInfo
                            fileInfo.setReceiver(fileReceiver); //set the login of receiver
                            fileInfo.setFilename(file.getName()); //set the filename
                            fileInfo.setFileContent(fileContent); //set the content of file
                            try{
                                serverProxy.sendFile(sectionId, fileInfo); //call the method sendFile of interface IChatServer
                                System.out.println("File sent successfully.");
                            } catch (ArgumentFault | ServerFault argumentFault) {
                                argumentFault.printStackTrace();
                            }

                        } else
                            System.out.println("Wrong path to the file. Please, try again.");
                    }
                    break;

                case "recFile": //command receive file
                    if (checkLogin(sectionId)){
                        System.out.println("You choose the command receive file.");
                        String pathFile = "./receiveFile/"; //path to received file
                        if (ReceiveTimer.getCountFile() == 0) //no file
                            System.out.println("You don't have waiting file.");
                        else {
                            File folderPath = new File(pathFile); //create object of class File
                            if (!folderPath.exists()){ //create folder if not exist
                                boolean dirCreated = folderPath.mkdirs();
                                if (dirCreated){
                                    System.out.println("Folder for files was created.");
                                }
                            } else if (ReceiveTimer.getCountFile() == 1){ // one waiting file
                                System.out.println("You have waiting file:");
                                System.out.println("Sender of file: " + ReceiveTimer.recFile[0].getSender());
                                String fileName = ReceiveTimer.recFile[0].getFilename();
                                System.out.println("Filename: " + fileName);
                                FileOutputStream stream = new FileOutputStream(folderPath.getPath() + '/' + ReceiveTimer.recFile[0].getFilename()); //create object of class FileOutputStream
                                stream.write(ReceiveTimer.recFile[0].fileContent); //write the content to file

                                System.out.println("The content was written to the file " + fileName + " at path ./receiverFile/" + fileName);
                                ReceiveTimer.setCountFile(0); //reset the counter
                            } else { //more than one file
                                String[] fileNames = new String[ReceiveTimer.getCountFile()+1];
                                System.out.println("You have waiting files:");
                                for (int i = 0; i < ReceiveTimer.getCountFile(); i++) {
                                    fileNames[i] = ReceiveTimer.recFile[i].getFilename();
                                    System.out.println("Sender of file: " + ReceiveTimer.recFile[i].getSender());
                                    System.out.println("Filename: " + fileNames[i]);
                                    FileOutputStream stream = new FileOutputStream(folderPath.getPath() + '/' + ReceiveTimer.recFile[0].getFilename()); //create object of class FileOutputStream
                                    stream.write(ReceiveTimer.recFile[0].fileContent); //write the content to files

                                    System.out.println("The content was written to the file " + fileNames[i] + " at path ./receiveFile/" + fileNames[i]);
                                }
                                ReceiveTimer.setCountFile(0); //reset the counter
                            }
                        }
                    }
                    break;

                case "help": //help command
                    System.out.println("Available command:");
                    System.out.println(" exit - shutdown client;");
                    System.out.println(" ping - testing connection;");
                    System.out.println(" echo - testing sending message;");
                    System.out.println(" login - log in on server or create new user;");
                    System.out.println(" list - array with active user names;");
                    System.out.println(" msg - send message to user;");
                    System.out.println(" file - send file to user;");
                    System.out.println(" recMsg - receive message for login user;");
                    System.out.println(" recFile - receive file for login user.");
                    break;

                default: //execute when wrong command
                    System.out.println("\nWrong command. Please, try again.");
            }
        }
    }
}


class ReceiveTimer extends TimerTask {

    private IChatServer serverProxy;
    private String sectionId;

    private static int countMsg; //count of waiting message
    private static int countFile; //count of waiting file

    public static FileInfo[] recFile = new FileInfo[25]; //list of object IServer.FileInfo
    public static Message[] recMsg = new Message[50]; //list of object IServer.Message

    private static ArrayList<String> current = new ArrayList<>(); //array list of current user
    private static ArrayList <String> old = new ArrayList<>(); //array list of old user
    private static int count = 0;

    ReceiveTimer(IChatServer proxy, String loginId){ //constructor
        this.serverProxy = proxy;
        this.sectionId = loginId;
    }

    public static int getCountMsg(){ //getter of private field countMsg
        return countMsg;
    }
    public static void setCountMsg(int countMsg){ //setter of private field countMsg
        ReceiveTimer.countMsg = countMsg;
    }
    public static int getCountFile() { //getter of private field countFile
        return countFile;
    }
    public static void setCountFile(int countFile){ //setter of private field countFile
        ReceiveTimer.countFile = countFile;
    }

    @Override
    public void run() throws NullPointerException { //run method of timer
        //check waiting message
        Message recMsg = null;
        try {
            recMsg = serverProxy.receiveMessage(sectionId);
        } catch (ArgumentFault | ServerFault e) {
            e.printStackTrace();
        }

        if (recMsg != null){ //message exist
            countMsg++;
            try{
                ReceiveTimer.recMsg[countMsg-1] = recMsg;
            } catch (NullPointerException ignored) {}
            System.out.println("You have unread message: " + countMsg + ". To see the message enter the command recMsg.");
        }

        //check waiting file
        FileInfo recFile = null;
        try {
            recFile = serverProxy.receiveFile(sectionId);
        } catch (ArgumentFault | ServerFault argumentFault) {
            argumentFault.printStackTrace();
        }

        if (recFile != null){ //file exist
            countFile++;
            try{
                ReceiveTimer.recFile[countFile-1] = recFile;
            } catch (NullPointerException ignored) {}
            System.out.println("You have waiting file: " + countFile + ". To see waiting file enter the command recFile.");
        }

        //check new or log out user
        List<String> listUser = null;
        try{
            listUser = serverProxy.listUsers(sectionId);
        } catch (ArgumentFault | ServerFault argumentFault) {
            argumentFault.printStackTrace();
        }

        assert false;
        String[] listUsers = listUser.toArray(new String[0]);
        if (count == 0){ //record users to array list on first iteration
            assert listUsers != null;
            for(String i: listUsers){
                current.add(i);
                old.add(i);
            }
        } else { //second and subsequent iterations
            current.clear();
            assert listUsers != null;
            Collections.addAll(current, listUsers);

            if(current.size() > old.size()){ //new user
                current.removeAll(old);
                System.out.print("The user " + current.get(0) + " connected to the server.");

                //bonus task
                Message msg = new Message(); //create the object of class Message
                msg.setMessage("Hello there, " + current.get(0) +"."); //set the message
                msg.setReceiver(current.get(0)); //set the receiver of message
                try {
                    serverProxy.sendMessage(sectionId, msg); //call the method sendMessage of interface IChatServer
                    System.out.println(" Welcome message sent successfully.");
                } catch (ArgumentFault | ServerFault ex) {
                    System.out.println(ex.getMessage());
                }

                current.clear(); //clear array list
                old.clear();
                for(String i: listUsers){
                    current.add(i);
                    old.add(i);
                }

            } else if (current.size() < old.size()){ //logout user
                old.removeAll(current);
                System.out.println("The user " + old.get(0) + " disconnected from server.");
                old.clear(); //clear array list
                current.clear();
                for(String i: listUsers) {
                    current.add(i);
                    old.add(i);
                }
            }
        }
        if (count == 0) //detect first iteration
            count++;
    }
}