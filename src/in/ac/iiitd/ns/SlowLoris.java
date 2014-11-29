package in.ac.iiitd.ns;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SlowLoris {
	
	// Global Parameters for statistics;
	private int pending;
	private int connected;
	private int error;
	private int closed;
	private boolean isAvailable;
	
	// Global parameters for attacking a hostname
	private final String hostname;
	private final int port;
	private final int connections;
	private final int interval;	
	private final int time;
	private final int rate; 
	
	/** List of Default Command Line Arguments */
	final static HashMap<String, String> CLA = new HashMap<String, String>();
	
	final static String USEAGE = "-h hostname -p port "
			+ "-c No_of_connections -i Interval_Between_Headers_in_ms"
			+ "-t time_Duration -r connections_per_s";
	
	private final Executor exec;
	
	static{
		CLA.put("-h", "localhost");
		CLA.put("-p", "80");
		CLA.put("-c", "50");
		CLA.put("-i", "10000");
		CLA.put("-t", "240");
		CLA.put("-r", "50");
	}
	
	public static enum UPDATE_TYPE{
		ERROR, CLOSED, CONNECTED
	}
	
	public synchronized void update(UPDATE_TYPE type){
		
		switch(type){
		case CONNECTED: 
			pending--;
			connected++;
			break;
		case ERROR:
			error++;
			connected--;
			break;
		case CLOSED:
			connected--;
			closed++;
			break;
		default:
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
		
		this.hostname = CLA.get("-h");
		this.port = getCLAInt("-p");
		this.connections = getCLAInt("-c");
		this.interval = getCLAInt("-i");
		this.time = getCLAInt("-t") * 1000;
		this.rate = getCLAInt("-r");
		
		pending = connections;
		connected = 0;
		error = 0;
		closed = 0;
		isAvailable = true;		
	}
	
	public void attack(){
		long sTime = System.currentTimeMillis();
		
		while(System.currentTimeMillis() - sTime < time){
			
		}
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
		} 
		else{
			for(int i = 0; i < args.length; i += 2){
				CLA.put(args[i], args[i+1]);
			}
		}
		
	}
	
	

}
