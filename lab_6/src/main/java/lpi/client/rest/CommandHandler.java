package lpi.client.rest;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class CommandHandler implements Closeable {

    private boolean sectionId = false; //boolean variable for checking log in
    private String targetUrl; //target URL of server
    private Timer timer; //object class of Timer
    javax.ws.rs.client.Client client; //object of class javax.ws.rs.client.Client
    private String loginName; //login name of current user

    CommandHandler(javax.ws.rs.client.Client client, String targetUrl){ //constructor of class CommandHandler
        this.client = client;
        this.targetUrl = targetUrl;
    }

    @Override
    public void close() throws IOException {
        if (client != null)
            client.close();
        if (timer != null)
            timer.cancel();
        System.out.println("Server close connection.");
        return;
    }

    public static void responseError(int codeError){ //method to handle error response
        if (codeError == 500){
            System.out.println("Internal server error.");
        } else if (codeError == 406){
            System.out.println("Target user has too much pending messages or files.");
        } else if (codeError == 400) {
            System.out.println("The target username or message was not specified properly or are incorrect");
        }
    }

    boolean checkLogin(boolean sectionId){ //method to check the logging of user
        if (sectionId)
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
                    if (client != null)
                        client.close();
                    break;

                case "ping": //command ping
                    System.out.println(" You choose the command ping.");
                    String pingResponse = client.target(targetUrl + "ping")
                            .request(MediaType.TEXT_PLAIN_TYPE).get(String.class); //ping to server
                    System.out.println("Received from server: " + pingResponse + ".\nPing successful.");
                    break;

                case "echo": //command echo
                    System.out.println(" You choose the command echo.");
                    System.out.print(" Enter the echo text: ");
                    String echoText = br.readLine(); //read echo text from user

                    String echoResponse = client.target(targetUrl +"echo")
                            .request(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.text(echoText), String.class); //echo command to server
                    System.out.println("Received from server: " + echoResponse);
                    break;

                case "login": //command login
                    System.out.println(" You choose the command login.");
                    System.out.print(" Enter the login: ");
                    loginName = br.readLine(); //read login from user
                    System.out.print(" Enter the password: ");
                    String loginPass = br.readLine(); //read password from user

                    UserInfo userInfo = new UserInfo(); //object of class UserInfo
                    userInfo.login = loginName;
                    userInfo.password = loginPass;
                    Entity userInfoEntity = Entity.entity(userInfo,
                            MediaType.APPLICATION_JSON_TYPE);
                    Response response = client.target(targetUrl + "user")
                            .request().put(userInfoEntity); //login command to server

                    if (response.getStatus() == Response.Status.CREATED.getStatusCode()) //handle the answer
                        System.out.println("New user registered.");
                    else if (response.getStatus() == Response.Status.ACCEPTED.getStatusCode())
                        System.out.println("Successful login.");
                    else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
                        System.out.println("Invalid login or password.");
                    else if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode())
                        System.out.println("The login or password has to be specified.");
                    else {
                        responseError(response.getStatus()); //run method for other error code
                    }
                    //authenticate all further requests
                    this.client.register(HttpAuthenticationFeature.basic(userInfo.login, userInfo.password));

                    sectionId = true;
                    ReceiveTimer receive = new ReceiveTimer(this.client, this.targetUrl, userInfo.login); //create object of class ReceiveTimer
                    timer = new Timer(true); //init timer daemon
                    timer.scheduleAtFixedRate(receive, 0, 3000); //set frequency  for thread
                    break;

                case "list": //command list
                    System.out.println("You choose the command list.");
                    if (checkLogin(sectionId)) {
                        Response listResponse = client.target(targetUrl + "users"). //list command to server
                                request(MediaType.APPLICATION_JSON_TYPE)
                                .get(Response.class);
                        if (listResponse.getStatus() == Response.Status.OK.getStatusCode()) { //handle answer
                            String responseAsString = listResponse.readEntity(String.class);
                            responseAsString = responseAsString.substring(10, responseAsString.length() - 2);
                            responseAsString = responseAsString.replaceAll("\"", "");
                            String[] stringUserList = responseAsString.split(",");
                            List<String> listUsers = Arrays.asList(stringUserList);

                            //print the list of users
                            System.out.print("List of user names: ");
                            for (String s : listUsers) System.out.print(s + " ");
                        } else {
                            responseError(listResponse.getStatus()); //run method for other error code
                        }
                    }
                    break;

                case "msg": //command msg
                    System.out.println("You choose the command msg.");
                    if (checkLogin(sectionId)) {
                        System.out.print("Enter the login of receiver: ");
                        String msgLogin = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the message: ");
                        String msgText = br.readLine(); //read message from user

                        Response responseOnMessage = client.target(targetUrl + msgLogin + "/messages")
                                .request(MediaType.APPLICATION_JSON_TYPE)
                                .post(Entity.text(msgText)); //msg command to server
                        if (responseOnMessage.getStatus() == 201) { //success sending msg
                            System.out.println("The message is processed.");
                        } else {
                            responseError(responseOnMessage.getStatus()); //run method for other error code
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
                            System.out.println(" Sender: " + ReceiveTimer.senderName[0]);
                            System.out.println(" Message: " + ReceiveTimer.msgText[0]);
                            ReceiveTimer.setCountMsg(0); //reset the counter
                        } else { //more then one message
                            System.out.println("You have messages:");
                            for (int i = 0; i < ReceiveTimer.getCountMsg(); i++) {
                                System.out.println(" Sender: " + ReceiveTimer.senderName[i]);
                                System.out.println(" Message: " + ReceiveTimer.msgText[i] + "\n");
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
                        System.out.print("Enter the name of file: ");
                        String fileName = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the path to file: ");
                        String filePath = br.readLine(); //read the path to file from user

                        File file = new File(filePath);
                        if (file.exists() && file.isFile()){ //checking for file existence
                            FileInfo fileInfo = new FileInfo(); //create the object of class FileInfo
                            fileInfo.sender = loginName;  //set the login of receiver
                            fileInfo.filename =  fileName; //set the filename

                            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder(); //encode content of file
                            fileInfo.content = encoder
                                    .encodeToString(Files.readAllBytes(file.toPath()));

                            Entity fileInfoEntity = Entity.entity(fileInfo,
                                    MediaType.APPLICATION_JSON_TYPE);
                            Response fileSentResponse = client.target(targetUrl  + fileReceiver + "/files")
                                    .request(MediaType.APPLICATION_JSON_TYPE)
                                    .post(fileInfoEntity); //file sent command to server

                            if (fileSentResponse.getStatus() == 201) {
                                System.out.println("The file is processed.");
                            } else {
                                responseError(fileSentResponse.getStatus()); //run method for other error code
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
                            }
                            if (ReceiveTimer.getCountFile() == 1){ // one waiting file
                                System.out.println("You have waiting file:");
                                System.out.println("Sender of file: " + ReceiveTimer.senderFile[0]);
                                String fileName = ReceiveTimer.fileName[0];
                                System.out.println("Filename: " + fileName);

                                PrintWriter out = new PrintWriter(folderPath.getPath() + '/' + ReceiveTimer.fileName[0]); //create object of class PrintWriter
                                out.println(ReceiveTimer.fileContent[0]); //write the content to file
                                out.close();

                                System.out.println("The content was written to the file " + fileName + " at path ./receiverFile/" + fileName);
                                ReceiveTimer.setCountFile(0); //reset the counter
                            } else { //more than one file
                                String[] fileNames = new String[ReceiveTimer.getCountFile()+1];
                                System.out.println("You have waiting files:");
                                for (int i = 0; i < ReceiveTimer.getCountFile(); i++) {
                                    fileNames[i] = ReceiveTimer.fileName[i];
                                    System.out.println("Sender of file: " + ReceiveTimer.senderFile[i]);
                                    System.out.println("Filename: " + fileNames[i]);

                                    PrintWriter out = new PrintWriter(folderPath.getPath() + '/' + ReceiveTimer.fileName[i]); //create object of class PrintWriter
                                    out.println(ReceiveTimer.fileContent[i]); //write the content to files
                                    out.close();
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
    private String targetUrl;  //target URL of server
    private javax.ws.rs.client.Client client;  //object of class javax.ws.rs.client.Client
    private String userName;  //login name of current user

    //list string for msg
    public static String[] senderName = new String[50];
    public static String[] msgText = new String[50];

    //list string for file
    public static String[] senderFile = new String[50];
    public static String[] fileName= new String[50];
    public static String[] fileContent= new String[50];

    private static int countMsg; //count of waiting message
    private static int countFile; //count of waiting file

    private static ArrayList<String> current = new ArrayList<>(); //array list of current user
    private static ArrayList <String> old = new ArrayList<>(); //array list of old user
    private static int count = 0;

    ReceiveTimer(javax.ws.rs.client.Client client, String targetUrl, String userName){ //constructor
        this.client = client;
        this.targetUrl = targetUrl;
        this.userName = userName;
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
    public void run() { //run method of timer
        //check new message
        try{ //check if server no shutdown
            client.target(targetUrl+"ping") //ping to server
                    .request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        } catch (Exception err){
            System.out.println(err.getMessage() + "\nConnection close. Server shutdown");
            if (client != null)
                client.close();
            System.exit(1);
        }

        Response msgIdResponse = client.target(targetUrl+userName+"/messages")
                .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class); //command receive id of msg to server
        if (msgIdResponse.getStatus() == 200) { //handle answer
            String responseAsString = msgIdResponse.readEntity(String.class);
            responseAsString = responseAsString.substring(10, responseAsString.length() - 2);
            responseAsString = responseAsString.replaceAll("\"", "");
            String[] stringIdMsg = responseAsString.split(",");

            if (stringIdMsg.length > 0){ //new msg exist
                countMsg ++;
                System.out.println("You have unread message: " + countMsg + ". To see the message enter the command recMsg.");
                Response msgResponse = client.target(targetUrl+userName+"/messages/"+ stringIdMsg[0])
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Response.class); //command receive messages
                String msgResponseAsString = msgResponse.readEntity(String.class);
                String[] msgAndSenser = msgResponseAsString.split(",");
                msgText[countMsg - 1] = msgAndSenser[0];
                senderName[countMsg - 1] = msgAndSenser[1];
                msgText[countMsg - 1] = msgText[countMsg - 1].substring(12, msgText[countMsg - 1].length() - 1);
                senderName[countMsg - 1]= senderName[countMsg - 1].substring(10, senderName[countMsg - 1].length() - 2);

                client.target(targetUrl+userName+"/messages/"+ stringIdMsg[0])
                        .request().delete(); //delete read message
            }
        }

        //check new file
        Response fileIdResponse = client.target(targetUrl+userName+"/files")
                .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class); //command receive id of files to server
        if (fileIdResponse.getStatus() == 200) {
            String responseAsString = fileIdResponse.readEntity(String.class);
            responseAsString = responseAsString.substring(10, responseAsString.length() - 2);
            responseAsString = responseAsString.replaceAll("\"", "");
            String[] stringIdFile = responseAsString.split(",");

            if (stringIdFile.length > 0) { //new file exist
                countFile++;
                System.out.println("You have waiting file: " + countFile + ". To see waiting file enter the command recFile.");
                Response msgResponse = client.target(targetUrl + userName + "/files/" + stringIdFile[0])
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(Response.class); //command receive file
                String fileResponseAsString = msgResponse.readEntity(String.class);
                String[] fileAndSenser = fileResponseAsString.split(",");

                senderFile[countFile-1] = fileAndSenser[2];
                fileName[countFile-1] = fileAndSenser[1];
                fileContent[countFile-1] = fileAndSenser[0];
                senderFile[countFile-1] = senderFile[countFile-1].substring(10, senderFile[countFile-1].length() - 2);
                fileName[countFile-1] =  fileName[countFile-1].substring(12, fileName[countFile-1].length() - 1);
                fileContent[countFile-1] =  fileContent[countFile-1].substring(12, fileContent[countFile-1].length() - 1);

                String fileContentString = fileContent[countFile-1];
                java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
                byte[] decodedContent = decoder.decode(fileContentString);
                fileContent[countFile-1] = new String(decodedContent);

                client.target(targetUrl + userName + "/files/" + stringIdFile[0])
                        .request().delete();
            }
        }

        //check new or log out user
        List<String> listUser = null;
        Response listResponse = client.target(targetUrl + "users").
                request(MediaType.APPLICATION_JSON_TYPE)
                .get(Response.class); //command to get list of user
        if (listResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            String responseAsString = listResponse.readEntity(String.class);
            try{
                responseAsString = responseAsString.substring(10, responseAsString.length() - 2);
            } catch (StringIndexOutOfBoundsException ignored){}
            responseAsString = responseAsString.replaceAll("\"", "");
            String[] stringUserList = responseAsString.split(",");
            listUser = Arrays.asList(stringUserList);
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
                System.out.println("The user " + current.get(0) + " connected to the server.");

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