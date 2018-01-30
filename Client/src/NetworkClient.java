import java.net.*;
import java.io.*;

/** A starting point for network clients. You'll need to
 *  override handleConnection, but in most cases connect can
 *  remain unchanged. NetworkClient uses SocketUtils to simplify
 *  the creation of the PrintWriter and BufferedReader.
 *  <p>
 *  From <a href="http://courses.coreservlets.com/Course-Materials/">the
 *  coreservlets.com tutorials on JSF 2, PrimeFaces, Ajax, JavaScript, jQuery, GWT, Android,
 *  Spring, Hibernate, JPA, RESTful Web Services, Hadoop, Spring MVC,
 *  servlets, JSP, Java 8 lambdas and streams (for those that know Java already),
 *  and Java 8 programming (for those new to Java)</a>.
 */

public abstract class NetworkClient {
    private String host;
    private int port;

    public String getHost() {
        return(host);
    }

    public int getPort() {
        return(port);
    }

    /** Register host and port. The connection won't
     *  actually be established until you call
     *  connect.
     */

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** Establishes the connection, then passes the socket
     *  to handleConnection.
     */

    public void connect() {
        try(Socket client = new Socket(host, port)) {
            handleConnection(client);
        } catch(UnknownHostException uhe) {
            System.err.println("Unknown host: " + host);
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe);
        }
    }

    /** This is the method you will override when
     *  making a network client for your task.
     */

    protected abstract void handleConnection(Socket client) throws IOException;
}