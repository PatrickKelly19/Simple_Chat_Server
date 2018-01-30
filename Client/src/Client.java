/*
1. Make the server multi threaded using ThreadPools or Executors with 5 clients max.
2. Design you GUI to accommodate these clients.
3. Rewrite the both the server and client code using NetworkClient and NetworkServer abstract classes and SocketUtils class static methods.
4. All GUI updates should be Thread safe using Threads/SwingUtilities AND SwingWorkers.
5. Make sure  try-catch-finally implemented on the IO streams and sockets.
6. Use the timeout on the ServerSocket and update the GUI with the number of timeouts.
7. Make your code work across different pc's (different ip addresses)
*/

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Client extends NetworkClient {
    private JTextField enterField;
    private JFrame frame;
    private JTextArea displayArea;
    private PrintWriter output;
    private BufferedReader input;
    private String message = "";
    private String chatServer;
    private Socket client;

    // initialize chatServer and set up GUI
    public Client( String host, int port ) {

        super(host, port);
        frame = new JFrame( "Client" );

        chatServer = host; // set server to which this client connects
        Container container = frame.getContentPane();

        // create enterField and register listener
        enterField = new JTextField();
        enterField.setEditable( false );
        enterField.addActionListener(
                new ActionListener() {

                    // send message to server
                    public void actionPerformed( ActionEvent event )
                    {
                        sendData( event.getActionCommand() );
                        enterField.setText( "" );
                    }
                }
        );

        container.add( enterField, BorderLayout.NORTH );

        // create displayArea
        displayArea = new JTextArea();
        container.add( new JScrollPane( displayArea ),
                BorderLayout.CENTER );

        frame.setSize( 300, 150 );
        frame.setVisible( true );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

    } // end Client constructor

    @Override
    protected void handleConnection(Socket client) throws IOException {
        // connect to server, get streams, process connection
        try {
            //connectToServer(); // Step 1: Create a Socket to make connection

            output = SocketUtils.getWriter(client);
            output.flush(); // flush output buffer to send header information

            // set up input stream for objects
            input = SocketUtils.getReader(client);
            processConnection();
        }

        // server closed connection
        catch ( EOFException eofException ) {
            System.err.println( "Client terminated connection" );
        }

        // process problems communicating with server
        catch ( IOException ioException ) {
            ioException.printStackTrace();
        }

        finally {
            closeConnection(); // Step 4: Close connection
        }
    } // end method runClient

    private void processConnection() throws IOException
    {
        // enable enterField so client user can send messages
        setTextFieldEditable(true);

        do { // process messages sent from server

            // read message and display it
            message = input.readLine();
            displayMessage( "\n" + message );

        } while ( !message.equals( "SERVER>>> TERMINATE" ) );

    }

    private void closeConnection()
    {
        displayMessage( "\nClosing connection" );
        setTextFieldEditable( false ); // disable enterField

        try {
            output.close();
            input.close();
            client.close();
        }
        catch( IOException ioException ) {
            ioException.printStackTrace();
        }
    }

    // send message to server
    private void sendData( String message )
    {
        // send object to server
        output.println( "CLIENT>>> " + message );
        output.flush();
        displayMessage( "\nCLIENT>>> " + message );
    }

    // utility method called from other threads to manipulate
    // displayArea in the event-dispatch thread
    private void displayMessage( final String messageToDisplay )
    {
        // display message from GUI thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {  // inner class to ensure GUI updates properly

                    public void run() // updates displayArea
                    {
                        displayArea.append( messageToDisplay );
                        displayArea.setCaretPosition(
                                displayArea.getText().length() );
                    }

                }  // end inner class

        ); // end call to SwingUtilities.invokeLater
    }

    // utility method called from other threads to manipulate
    // enterField in the event-dispatch thread
    private void setTextFieldEditable( final boolean editable )
    {
        // display message from GUI thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {  // inner class to ensure GUI updates properly

                    public void run()  // sets enterField's editability
                    {
                        enterField.setEditable( editable );
                    }

                }  // end inner class

        ); // end call to SwingUtilities.invokeLater
    }

    public static void main( String args[] ) throws IOException {
        String host = "localhost";
        int port = 8080;
        Client app = new Client(host,port);
        app.connect();

    }
}