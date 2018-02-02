//Shubham Patel
//1001376052


import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Process implements Runnable {

  // The process socket
  private static Socket processSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;

  public static void main(String[] args) {

    // The default port.
    int portNumber = 8091;
    // The default host.
    String host = "localhost";

    if (args.length < 2) {
      System.out.println("Running on host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try {
      processSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(processSocket.getOutputStream());
      is = new DataInputStream(processSocket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    /*
     *If everything has been initialized then we want to write some data to the
      socket we have opened a connection to on the port portNumber.
     */
    if (processSocket != null && os != null && is != null) {
      try {

        /* Create a thread to read from the server. */
        new Thread(new Process()).start();
        while (!closed) {
          os.println(inputLine.readLine().trim());
        }
        /*
         Closing the output stream, input stream,and the socket.
         */
        os.close();
        is.close();
        processSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  /*
    Create a thread to read from the server.
   */
 //suppress warnings for is.readLine() Depricated API
 @SuppressWarnings("deprecation")  
  public void run() {
    /*
      Keep on reading from the socket till we receive "Bye" from the
     server. Once we received that then we want to break.
     */
    String responseLine;
    try {
      while ((responseLine = is.readLine()) != null) {
        System.out.println(responseLine);
        if (responseLine.indexOf("***Bye") != -1)
          break;
      }
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}