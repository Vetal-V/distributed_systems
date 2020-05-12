package lpi.client.mq;

import javax.jms.*;
import java.io.*;
import java.util.*;
import javax.jms.JMSException;
import javax.jms.TextMessage;

public class CommandHandler implements Closeable {

    private boolean loginCheck = false; //boolean variable for checking log in
    private String targetUrl; //target URL of server
    private Timer timer; //object class of Timer

    private javax.jms.Session session;
    private javax.jms.Connection connection;

    CommandHandler(javax.jms.Session session, javax.jms.Connection connection){ //constructor of class CommandHandler
        this.session = session;
        this.connection = connection;
    }

    private synchronized javax.jms.Message getResponse(javax.jms.Message msg, String queueName) throws JMSException {
        // create an object specifying the Destination to which the message will be sent:
        javax.jms.Destination targetQueue = session.createQueue(queueName);
        // create an object specifying the Destination where the response will be received:
        javax.jms.Destination replyQueue = this.session.createTemporaryQueue();
        // create a producer on targetQueue, which will allow to send the request message:
        javax.jms.MessageProducer producer = session.createProducer(targetQueue);
        // create a consumer on replyQueue, which will allow to receive the answer:
        javax.jms.MessageConsumer consumer = session.createConsumer(replyQueue);
        msg.setJMSReplyTo(replyQueue);  // specify JMSReplyTo attribute:
        producer.send(msg); // send the message using producer:
        javax.jms.Message replyMsg = consumer.receive(1500); // await the reply using consumer:

        consumer.close();
        producer.close();

        return replyMsg;
    }

    public static void responseError(Message responseError) throws JMSException { //method to handle error response
        System.out.println("Unexpected error!");
        if (responseError instanceof TextMessage) { // expect the text message as the content
            String content = ((TextMessage) responseError).getText(); // obtaining content
            System.out.println(content);
        }

        if (responseError instanceof MapMessage) { // expect the text message as the content
            String content = ((MapMessage) responseError).getString("message"); // obtaining content
            System.out.println(content);
        }
        System.out.println();
    }

    boolean checkLogin(boolean loginCheck){ //method to check the logging of user
        if (loginCheck)
            return true;
        else
            return false;
    }

    void run() throws IOException, JMSException { //method run of class CommandHandler
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  //input buffer stream from user

        boolean ifLoop = true;
        while (ifLoop) { //loop for entering command
            System.out.println("\nEnter the command (help - list of available commands): ");
            String command = br.readLine(); //read command of user

            switch (command) {
                case "exit": //complete client execution
                    ifLoop = false;
                    if (checkLogin(loginCheck)) {
                        Message msg = session.createMessage(); // an empty message
                        Message response = getResponse(msg, "chat.exit");

                        if (!(response instanceof TextMessage)) {
                            System.out.println("Successful logout.");
                        } else {
                            responseError(response);
                        }
                    }
                    loginCheck = false;
                    close(); // closing a session
                    break;

                case "ping": //command ping
                    System.out.println(" You choose the command ping.");

                    javax.jms.Message msg = null; // an empty message
                    try {
                        msg = session.createMessage();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    assert msg != null;
                    javax.jms.Message pingResponse = getResponse(msg, "chat.diag.ping");

                    if (!(pingResponse instanceof TextMessage)){
                        System.out.println("Successfully ping.\n");
                    } else {
                        responseError(pingResponse);
                    }
                    break;

                case "echo": //command echo
                    System.out.println(" You choose the command echo.");
                    System.out.print(" Enter the echo text: ");
                    String echoText = br.readLine(); //read echo text from user

                    TextMessage msgEcho = session.createTextMessage(echoText); // a message that contains a string

                    javax.jms.Message echoResponse = getResponse(msgEcho, "chat.diag.echo");

                    if (echoResponse instanceof TextMessage) {
                        // expect the text message as the content
                        String content = ((TextMessage) echoResponse).getText(); // obtaining content
                        System.out.println("Received from server: " + content);
                    } else { //unexpected error
                        System.out.println("Unexpected message type: " + echoResponse.getClass() );
                    }
                    break;

                case "login": //command login
                    System.out.println(" You choose the command login.");
                    System.out.print(" Enter the login: ");
                    String loginName = br.readLine(); //read login from user
                    System.out.print(" Enter the password: ");
                    String loginPass = br.readLine(); //read password from user

                    javax.jms.MapMessage loginRequest = session.createMapMessage();
                    loginRequest.setString("login", loginName);
                    loginRequest.setString("password", loginPass);

                    javax.jms.Message loginResponse = getResponse(loginRequest, "chat.login");
                    if (loginResponse instanceof MapMessage) {
                        if (((javax.jms.MapMessage) loginResponse).getBoolean("success")) {// successfully logged in
                            System.out.println(((javax.jms.MapMessage) loginResponse).getString("message"));
                            loginCheck = true;


                        } else { // user failed to login, check the "message" for details.
                            loginCheck = false;
                            System.out.println("Failed to login: " + ((javax.jms.MapMessage) loginResponse).getString("message"));
                        }
                    } else
                        responseError(loginResponse);
                    break;

                case "list": //command list
                    System.out.println("You choose the command list.");
                    if (checkLogin(loginCheck)) {
                        javax.jms.Message msgList = null; // an empty message
                        try {
                            msgList = session.createMessage();
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }

                        assert msgList != null;
                        javax.jms.Message listResponse = getResponse(msgList, "chat.listUsers");
                        Serializable objList = ((ObjectMessage)listResponse).getObject();
                        if (objList != null && objList instanceof String[]) {
                            String[] users = (String[])objList;
                            System.out.print("List of user names: ");
                            for (String s : users) System.out.print(s + " ");
                        } else { // no, there’s not a list of objects, handle error.
                            System.out.println("Unexpected content: " + objList);
                        }
                    }
                    break;

                case "msg": //command msg
                    System.out.println("You choose the command msg.");
                    if (checkLogin(loginCheck)) {
//                        MapMessage msg = session.createMapMessage();
//                        msg.setString("receiver", receiver);
//                        msg.setString("message", String.join(" ", messageContent));
//
//                        Message response = getResponse(msg, QueueName.SEND_MSG);
//
//                        if (response instanceof MapMessage){
//                            // print success message or error
//                            System.out.println(((MapMessage) response).getString("message"));
//                            if (!((MapMessage) response).getBoolean("success")) {
//                                // when error
//                                System.out.println("Please retry!");
//                            }
//                            System.out.println();
//                        } else {
//                            checkUnexpectedError(response);
//                        }
                    }
                    break;

                case "recMsg": //command receive message
                    if (checkLogin(loginCheck)) {

                    }
                    break;

                case "file": //command file
                    if (checkLogin(loginCheck)) {
                        System.out.println("You choose the command file.");
                        System.out.print("Enter the login of receiver: ");
                        String fileReceiver = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the name of file: ");
                        String fileName = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the path to file: ");
                        String filePath = br.readLine(); //read the path to file from user


                    }
                    break;

                case "recFile": //command receive file
                    if (checkLogin(loginCheck)){
                        System.out.println("You choose the command receive file.");
                        String pathFile = "./receiveFile/"; //path to received file

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

    @Override
    public void close() {
        if (session != null){
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        if (connection != null){
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Connection close.");
        System.exit(0);
    }
}


//class ReceiveTimer extends TimerTask {
//    private String targetUrl;  //target URL of server
//    private javax.ws.rs.client.Client client;  //object of class javax.ws.rs.client.Client
//    private String userName;  //login name of current user
//    //list string for msg
//    public static String[] senderName = new String[50];
//    public static String[] msgText = new String[50];
//    //list string for file
//    public static String[] senderFile = new String[50];
//    public static String[] fileName= new String[50];
//    public static String[] fileContent= new String[50];
//
//    private static int countMsg; //count of waiting message
//    private static int countFile; //count of waiting file
//
//    private static ArrayList<String> current = new ArrayList<>(); //array list of current user
//    private static ArrayList <String> old = new ArrayList<>(); //array list of old user
//    private static int count = 0;
//
//    ReceiveTimer(javax.ws.rs.client.Client client, String targetUrl, String userName){ //constructor
//        this.client = client;
//        this.targetUrl = targetUrl;
//        this.userName = userName;
//    }
//
//    public static int getCountMsg(){ //getter of private field countMsg
//        return countMsg;
//    }
//    public static void setCountMsg(int countMsg){ //setter of private field countMsg
//        ReceiveTimer.countMsg = countMsg;
//    }
//    public static int getCountFile() { //getter of private field countFile
//        return countFile;
//    }
//    public static void setCountFile(int countFile){ //setter of private field countFile
//        ReceiveTimer.countFile = countFile;
//    }
//
//    @Override
//    public void run() { //run method of timer
//        //check new message
//        Response msgIdResponse = client.target(targetUrl+userName+"/messages") //command receive id of msg to server
//                .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
//        if (msgIdResponse.getStatus() == 200) { //handle answer
//            String responseAsString = msgIdResponse.readEntity(String.class);
//            responseAsString = responseAsString.substring(10, responseAsString.length() - 2);
//            responseAsString = responseAsString.replaceAll("\"", "");
//            String[] stringIdMsg = responseAsString.split(",");
//
//            if (stringIdMsg.length > 0){ //new msg exist
//                countMsg ++;
//                System.out.println("You have unread message: " + countMsg + ". To see the message enter the command recMsg.");
//                Response msgResponse = client.target(targetUrl+userName+"/messages/"+ stringIdMsg[0]) //command receive messages
//                    .request(MediaType.APPLICATION_JSON_TYPE)
//                    .get(Response.class);
//                String msgResponseAsString = msgResponse.readEntity(String.class);
//                String[] msgAndSenser = msgResponseAsString.split(",");
//                msgText[countMsg - 1] = msgAndSenser[0];
//                senderName[countMsg - 1] = msgAndSenser[1];
//                msgText[countMsg - 1] = msgText[countMsg - 1].substring(12, msgText[countMsg - 1].length() - 1);
//                senderName[countMsg - 1]= senderName[countMsg - 1].substring(10, senderName[countMsg - 1].length() - 2);
//
//                client.target(targetUrl+userName+"/messages/"+ stringIdMsg[0]) //delete read message
//                        .request().delete();
//            }
//        }
//
//        //check new file
//        Response fileIdResponse = client.target(targetUrl+userName+"/files")  //command receive id of files to server
//                .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
//        if (fileIdResponse.getStatus() == 200) {
//            String responseAsString = fileIdResponse.readEntity(String.class);
//            responseAsString = responseAsString.substring(10, responseAsString.length() - 2);
//            responseAsString = responseAsString.replaceAll("\"", "");
//            String[] stringIdFile = responseAsString.split(",");
//
//            if (stringIdFile.length > 0) { //new file exist
//                countFile++;
//                System.out.println("You have waiting file: " + countFile + ". To see waiting file enter the command recFile.");
//                Response msgResponse = client.target(targetUrl + userName + "/files/" + stringIdFile[0]) //command receive file
//                        .request(MediaType.APPLICATION_JSON_TYPE)
//                        .get(Response.class);
//                String fileResponseAsString = msgResponse.readEntity(String.class);
//                String[] fileAndSenser = fileResponseAsString.split(",");
//
//                senderFile[countFile-1] = fileAndSenser[2];
//                fileName[countFile-1] = fileAndSenser[1];
//                fileContent[countFile-1] = fileAndSenser[0];
//                senderFile[countFile-1] = senderFile[countFile-1].substring(10, senderFile[countFile-1].length() - 2);
//                fileName[countFile-1] =  fileName[countFile-1].substring(12, fileName[countFile-1].length() - 1);
//                fileContent[countFile-1] =  fileContent[countFile-1].substring(12, fileContent[countFile-1].length() - 1);
//
//                String fileContentString = fileContent[countFile-1];
//                java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
//                byte[] decodedContent = decoder.decode(fileContentString);
//                fileContent[countFile-1] = new String(decodedContent);
//
//                client.target(targetUrl + userName + "/files/" + stringIdFile[0])
//                        .request().delete();
//            }
//        }
//
//        //check new or log out user
//        List<String> listUser = null;
//        Response listResponse = client.target(targetUrl + "users"). //command to get list of user
//                request(MediaType.APPLICATION_JSON_TYPE)
//                .get(Response.class);
//        if (listResponse.getStatus() == Response.Status.OK.getStatusCode()) {
//            String responseAsString = listResponse.readEntity(String.class);
//            try{
//                responseAsString = responseAsString.substring(10, responseAsString.length() - 2);
//            } catch (StringIndexOutOfBoundsException ignored){}
//            responseAsString = responseAsString.replaceAll("\"", "");
//            String[] stringUserList = responseAsString.split(",");
//            listUser = Arrays.asList(stringUserList);
//        }
//
//        assert false;
//        String[] listUsers = listUser.toArray(new String[0]);
//        if (count == 0){ //record users to array list on first iteration
//            assert listUsers != null;
//            for(String i: listUsers){
//                current.add(i);
//                old.add(i);
//            }
//        } else { //second and subsequent iterations
//            current.clear();
//            assert listUsers != null;
//            Collections.addAll(current, listUsers);
//
//            if(current.size() > old.size()){ //new user
//                current.removeAll(old);
//                System.out.println("The user " + current.get(0) + " connected to the server.");
//
//                current.clear(); //clear array list
//                old.clear();
//                for(String i: listUsers){
//                    current.add(i);
//                    old.add(i);
//                }
//
//            } else if (current.size() < old.size()){ //logout user
//                old.removeAll(current);
//                System.out.println("The user " + old.get(0) + " disconnected from server.");
//                old.clear(); //clear array list
//                current.clear();
//                for(String i: listUsers) {
//                    current.add(i);
//                    old.add(i);
//                }
//            }
//        }
//        if (count == 0) //detect first iteration
//            count++;
//    }
//}