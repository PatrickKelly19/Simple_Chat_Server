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

public class Server extends MultithreadedServer {
    private JTextField enterField;
    private JTextArea displayArea;
    private PrintWriter output;
    private BufferedReader input;
    private ServerSocket server;
    private Socket connection;
    private int counter = 1;
    private JFrame frame ;

    // set up GUI
    public Server(int port)
    {
        super( port);
        frame = new JFrame("SERVER");
        Container container = frame.getContentPane();

        // create enterField and register listener
        enterField = new JTextField();
        enterField.setEditable( false );
        enterField.addActionListener(
                new ActionListener() {

                    // send message to client
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
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );


    } // end Server constructor

    @Override
    protected void handleConnection(Socket connection) throws IOException {

        this.connection = connection;

        try {

            while ( true ) {

                try {
                    waitForConnection(); // Step 2: Wait for a connection.
                    getStreams();        // Step 3: Get input & output streams.
                    processConnection(); // Step 4: Process connection.
                }

                // process EOFException when client closes connection
                catch ( EOFException eofException ) {
                    System.err.println( "Server terminated connection" );
                }

                finally {
                    closeConnection();   // Step 5: Close connection.
                    ++counter;
                }

            } // end while

        } // end try

        // process problems with I/O
        catch ( IOException ioException ) {
            ioException.printStackTrace();
        }

    }

    // wait for connection to arrive, then display connection info
    private void waitForConnection() throws IOException
    {
        displayMessage( "Waiting for connection\n" );
        displayMessage( "Connection " + counter + " received from: " +
                connection.getInetAddress().getHostName() );
    }

    // get streams to send and receive data
    private void getStreams() throws IOException
    {
        // set up output stream for objects
        output = SocketUtils.getWriter(connection);
        output.flush(); // flush output buffer to send header information

        // set up input stream for objects
        input = SocketUtils.getReader(connection);
    }

    // process connection with client
    private void processConnection() throws IOException
    {
        // send connection successful message to client
        String message = "Connection successful";
        sendData( message );

        // enable enterField so server user can send messages
        setTextFieldEditable( true );

        do { // process messages sent from client

            // read message and display it
            try {
                message = ( String ) input.readLine();
                displayMessage( "\n" + message );
            }

            // catch problems reading from client
            catch ( Exception ee ) {
                displayMessage( "\nUnknown object type received" );
            }

        } while ( !message.equals( "CLIENT>>> TERMINATE" ) );

    } // end method processConnection

    // close streams and socket
    private void closeConnection()
    {
        displayMessage( "\nTerminating connection\n" );
        setTextFieldEditable( false ); // disable enterField

        try {
            output.close();
            input.close();
            connection.close();
        }
        catch( IOException ioException ) {
            ioException.printStackTrace();
        }
    }

    // send message to client
    private void sendData( String message )
    {
        // send object to client
        try {
            output.println( "SERVER>>> " + message);
            output.flush();
            displayMessage( "\nSERVER>>> " + message );
        }

        // process problems sending object
        catch ( Exception ee ) {
            displayArea.append( "\nError writing object" );
        }
    }

    // utility method called from other threads to manipulate
    // displayArea in the event-dispatch thread
    private void displayMessage( final String messageToDisplay )
    {
        // display message from event-dispatch thread of execution
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
        // display message from event-dispatch  thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {  // inner class to ensure GUI updates properly

                    public void run()  // sets enterField's editability
                    {
                        enterField.setEditable( editable );
                    }

                }  // end inner class

        ); // end call to SwingUtilities.invokeLater
    }

    public static void main( String args[] )
    {
        int port = 8080;
        Server application = new Server(port);
        application.listen();
    }
}