package lpi.client.mq;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import javax.jms.JMSException;

public class Connect implements Closeable {

    private static final String defaultUrl = "tcp://localhost:61616";
    private String targetUrl;
    private javax.jms.Connection connection;
    javax.jms.Session session;
    javax.jms.Session sessionListener;

    Connect(String[] args) { //constructor
        if (args.length == 1) { //wrong argument
            System.out.println("Wrong argument to start client");
        } else if (args.length == 2) { //argument exist
            targetUrl = "tcp://" + args[0] + ":" + Integer.parseInt(args[1]);
        } else { //no argument, connection with default parameters
            targetUrl = defaultUrl;
        }
    }

    void run() throws IOException, JMSException {

        org.apache.activemq.ActiveMQConnectionFactory connectionActiveMQ = new org.apache.activemq.ActiveMQConnectionFactory(targetUrl);

        connectionActiveMQ.setTrustedPackages(Arrays.asList("lpi.server.mq"));

        boolean isTransact = false; // no transactions will be used.
        int ackMode = javax.jms.Session.AUTO_ACKNOWLEDGE; // automatically acknowledge.

        connection = connectionActiveMQ.createConnection();
        connection.start();



        session = connection.createSession(isTransact, ackMode);
        sessionListener = connection.createSession(isTransact, ackMode);
        CommandHandler commandHandle = new CommandHandler(session, connection, sessionListener); //create object of class CommandHandler
        commandHandle.run(); //run method of CommandHandler

    }
    @Override
    public void close() {
        if (connection != null){
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        if (session != null){
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
