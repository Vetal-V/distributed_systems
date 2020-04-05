
package lpi.client.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the lpi.client.soap package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ServerFault_QNAME = new QName("http://soap.server.lpi/", "ServerFault");
    private final static QName _ListUsers_QNAME = new QName("http://soap.server.lpi/", "listUsers");
    private final static QName _ReceiveMessageResponse_QNAME = new QName("http://soap.server.lpi/", "receiveMessageResponse");
    private final static QName _ArgumentFault_QNAME = new QName("http://soap.server.lpi/", "ArgumentFault");
    private final static QName _SendFile_QNAME = new QName("http://soap.server.lpi/", "sendFile");
    private final static QName _Exit_QNAME = new QName("http://soap.server.lpi/", "exit");
    private final static QName _ListUsersResponse_QNAME = new QName("http://soap.server.lpi/", "listUsersResponse");
    private final static QName _SendMessageResponse_QNAME = new QName("http://soap.server.lpi/", "sendMessageResponse");
    private final static QName _EchoResponse_QNAME = new QName("http://soap.server.lpi/", "echoResponse");
    private final static QName _PingResponse_QNAME = new QName("http://soap.server.lpi/", "pingResponse");
    private final static QName _Ping_QNAME = new QName("http://soap.server.lpi/", "ping");
    private final static QName _ReceiveFile_QNAME = new QName("http://soap.server.lpi/", "receiveFile");
    private final static QName _LoginFault_QNAME = new QName("http://soap.server.lpi/", "LoginFault");
    private final static QName _SendFileResponse_QNAME = new QName("http://soap.server.lpi/", "sendFileResponse");
    private final static QName _ExitResponse_QNAME = new QName("http://soap.server.lpi/", "exitResponse");
    private final static QName _LoginResponse_QNAME = new QName("http://soap.server.lpi/", "loginResponse");
    private final static QName _ReceiveFileResponse_QNAME = new QName("http://soap.server.lpi/", "receiveFileResponse");
    private final static QName _ReceiveMessage_QNAME = new QName("http://soap.server.lpi/", "receiveMessage");
    private final static QName _Login_QNAME = new QName("http://soap.server.lpi/", "login");
    private final static QName _Message_QNAME = new QName("http://soap.server.lpi/", "message");
    private final static QName _Echo_QNAME = new QName("http://soap.server.lpi/", "echo");
    private final static QName _FileInfo_QNAME = new QName("http://soap.server.lpi/", "fileInfo");
    private final static QName _SendMessage_QNAME = new QName("http://soap.server.lpi/", "sendMessage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: lpi.client.soap
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExitResponse }
     * 
     */
    public ExitResponse createExitResponse() {
        return new ExitResponse();
    }

    /**
     * Create an instance of {@link LoginResponse }
     * 
     */
    public LoginResponse createLoginResponse() {
        return new LoginResponse();
    }

    /**
     * Create an instance of {@link ReceiveFileResponse }
     * 
     */
    public ReceiveFileResponse createReceiveFileResponse() {
        return new ReceiveFileResponse();
    }

    /**
     * Create an instance of {@link ReceiveMessage }
     * 
     */
    public ReceiveMessage createReceiveMessage() {
        return new ReceiveMessage();
    }

    /**
     * Create an instance of {@link SendFileResponse }
     * 
     */
    public SendFileResponse createSendFileResponse() {
        return new SendFileResponse();
    }

    /**
     * Create an instance of {@link Ping }
     * 
     */
    public Ping createPing() {
        return new Ping();
    }

    /**
     * Create an instance of {@link ReceiveFile }
     * 
     */
    public ReceiveFile createReceiveFile() {
        return new ReceiveFile();
    }

    /**
     * Create an instance of {@link LoginException }
     * 
     */
    public LoginException createLoginException() {
        return new LoginException();
    }

    /**
     * Create an instance of {@link FileInfo }
     * 
     */
    public FileInfo createFileInfo() {
        return new FileInfo();
    }

    /**
     * Create an instance of {@link SendMessage }
     * 
     */
    public SendMessage createSendMessage() {
        return new SendMessage();
    }

    /**
     * Create an instance of {@link Echo }
     * 
     */
    public Echo createEcho() {
        return new Echo();
    }

    /**
     * Create an instance of {@link Login }
     * 
     */
    public Login createLogin() {
        return new Login();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link SendFile }
     * 
     */
    public SendFile createSendFile() {
        return new SendFile();
    }

    /**
     * Create an instance of {@link Exit }
     * 
     */
    public Exit createExit() {
        return new Exit();
    }

    /**
     * Create an instance of {@link ListUsersResponse }
     * 
     */
    public ListUsersResponse createListUsersResponse() {
        return new ListUsersResponse();
    }

    /**
     * Create an instance of {@link ArgumentException }
     * 
     */
    public ArgumentException createArgumentException() {
        return new ArgumentException();
    }

    /**
     * Create an instance of {@link ReceiveMessageResponse }
     * 
     */
    public ReceiveMessageResponse createReceiveMessageResponse() {
        return new ReceiveMessageResponse();
    }

    /**
     * Create an instance of {@link ServerException }
     * 
     */
    public ServerException createServerException() {
        return new ServerException();
    }

    /**
     * Create an instance of {@link ListUsers }
     * 
     */
    public ListUsers createListUsers() {
        return new ListUsers();
    }

    /**
     * Create an instance of {@link EchoResponse }
     * 
     */
    public EchoResponse createEchoResponse() {
        return new EchoResponse();
    }

    /**
     * Create an instance of {@link PingResponse }
     * 
     */
    public PingResponse createPingResponse() {
        return new PingResponse();
    }

    /**
     * Create an instance of {@link SendMessageResponse }
     * 
     */
    public SendMessageResponse createSendMessageResponse() {
        return new SendMessageResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServerException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "ServerFault")
    public JAXBElement<ServerException> createServerFault(ServerException value) {
        return new JAXBElement<ServerException>(_ServerFault_QNAME, ServerException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListUsers }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "listUsers")
    public JAXBElement<ListUsers> createListUsers(ListUsers value) {
        return new JAXBElement<ListUsers>(_ListUsers_QNAME, ListUsers.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReceiveMessageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "receiveMessageResponse")
    public JAXBElement<ReceiveMessageResponse> createReceiveMessageResponse(ReceiveMessageResponse value) {
        return new JAXBElement<ReceiveMessageResponse>(_ReceiveMessageResponse_QNAME, ReceiveMessageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArgumentException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "ArgumentFault")
    public JAXBElement<ArgumentException> createArgumentFault(ArgumentException value) {
        return new JAXBElement<ArgumentException>(_ArgumentFault_QNAME, ArgumentException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendFile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "sendFile")
    public JAXBElement<SendFile> createSendFile(SendFile value) {
        return new JAXBElement<SendFile>(_SendFile_QNAME, SendFile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exit }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "exit")
    public JAXBElement<Exit> createExit(Exit value) {
        return new JAXBElement<Exit>(_Exit_QNAME, Exit.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListUsersResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "listUsersResponse")
    public JAXBElement<ListUsersResponse> createListUsersResponse(ListUsersResponse value) {
        return new JAXBElement<ListUsersResponse>(_ListUsersResponse_QNAME, ListUsersResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendMessageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "sendMessageResponse")
    public JAXBElement<SendMessageResponse> createSendMessageResponse(SendMessageResponse value) {
        return new JAXBElement<SendMessageResponse>(_SendMessageResponse_QNAME, SendMessageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "echoResponse")
    public JAXBElement<EchoResponse> createEchoResponse(EchoResponse value) {
        return new JAXBElement<EchoResponse>(_EchoResponse_QNAME, EchoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PingResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "pingResponse")
    public JAXBElement<PingResponse> createPingResponse(PingResponse value) {
        return new JAXBElement<PingResponse>(_PingResponse_QNAME, PingResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ping }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "ping")
    public JAXBElement<Ping> createPing(Ping value) {
        return new JAXBElement<Ping>(_Ping_QNAME, Ping.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReceiveFile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "receiveFile")
    public JAXBElement<ReceiveFile> createReceiveFile(ReceiveFile value) {
        return new JAXBElement<ReceiveFile>(_ReceiveFile_QNAME, ReceiveFile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "LoginFault")
    public JAXBElement<LoginException> createLoginFault(LoginException value) {
        return new JAXBElement<LoginException>(_LoginFault_QNAME, LoginException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendFileResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "sendFileResponse")
    public JAXBElement<SendFileResponse> createSendFileResponse(SendFileResponse value) {
        return new JAXBElement<SendFileResponse>(_SendFileResponse_QNAME, SendFileResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExitResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "exitResponse")
    public JAXBElement<ExitResponse> createExitResponse(ExitResponse value) {
        return new JAXBElement<ExitResponse>(_ExitResponse_QNAME, ExitResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "loginResponse")
    public JAXBElement<LoginResponse> createLoginResponse(LoginResponse value) {
        return new JAXBElement<LoginResponse>(_LoginResponse_QNAME, LoginResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReceiveFileResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "receiveFileResponse")
    public JAXBElement<ReceiveFileResponse> createReceiveFileResponse(ReceiveFileResponse value) {
        return new JAXBElement<ReceiveFileResponse>(_ReceiveFileResponse_QNAME, ReceiveFileResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReceiveMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "receiveMessage")
    public JAXBElement<ReceiveMessage> createReceiveMessage(ReceiveMessage value) {
        return new JAXBElement<ReceiveMessage>(_ReceiveMessage_QNAME, ReceiveMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Login }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "login")
    public JAXBElement<Login> createLogin(Login value) {
        return new JAXBElement<Login>(_Login_QNAME, Login.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Message }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "message")
    public JAXBElement<Message> createMessage(Message value) {
        return new JAXBElement<Message>(_Message_QNAME, Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Echo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "echo")
    public JAXBElement<Echo> createEcho(Echo value) {
        return new JAXBElement<Echo>(_Echo_QNAME, Echo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "fileInfo")
    public JAXBElement<FileInfo> createFileInfo(FileInfo value) {
        return new JAXBElement<FileInfo>(_FileInfo_QNAME, FileInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.server.lpi/", name = "sendMessage")
    public JAXBElement<SendMessage> createSendMessage(SendMessage value) {
        return new JAXBElement<SendMessage>(_SendMessage_QNAME, SendMessage.class, null, value);
    }

}
