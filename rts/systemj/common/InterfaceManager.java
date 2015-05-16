package systemj.common;

import java.util.Hashtable;
import java.util.Vector;

import systemj.common.util.LinkQueue;
import systemj.interfaces.GenericChannel;
import systemj.interfaces.GenericInterface;

/**
 * Manages routing
 * 
 * Must be compatible with CLDC 1.1
 * @author hpar081
 *
 */
public class InterfaceManager {
	private String ssname;
	private final LinkQueue OutQueue = new LinkQueue();
	private Vector LocalInterface = new Vector();;
	private Interconnection ic = new Interconnection();
	private Hashtable cdlocation = new Hashtable();
	private Hashtable chanins = new Hashtable();
	private Hashtable cachedintf = new Hashtable();
	private Vector unsentdata = new Vector();
	private final int MAX_UNSENT_DATA = 50;
	
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void addCDLocation(String ss, String cd){
		if(cdlocation.containsKey(cd))
			throw new RuntimeException("Tried to add duplicated CD to the map : "+cd);
		
		cdlocation.put(cd, ss);
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	public String getCDLocation(String cd){
		return (String)cdlocation.get(cd);
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void setChannelInstances(Hashtable ci){
		chanins = ci;
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	private Object getChannelInstance(String n){
		return chanins.get(n);
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void setInterconnection(Interconnection ic){
		this.ic = ic;
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	public void setLocalInterface(String ssname){
		LocalInterface = ic.getInterfaces(ssname);
		this.ssname = ssname;
	}

	// invokeReceivingThread() does not have to invoke a thread (implementation dependent)
	/**
	 * Internal use
	 * @param ci
	 */
	public void init(){
		for(int i=0;i<LocalInterface.size(); i++){
			((GenericInterface)LocalInterface.elementAt(i)).invokeReceivingThread();
			((GenericInterface)LocalInterface.elementAt(i)).setInterfaceManager(this);
		}
	}
	
	
	/**
	 * This is called from channel instances
	 * @param o - An object to be interted into the queue
	 * @return True when the channel data has been successfully pushed to the queue, false otherwise
	 */
	public boolean pushToQueue(Object o){
		if(OutQueue.isFull())
			return false;
		else
			return OutQueue.push(o);
	}

	/**
	 * Internal use
	 * @param ci
	 */
	private synchronized void addToUnsent(Object[] o){
		boolean there =false;
		for(int i=0;i<unsentdata.size(); i++){
			if(((Object[])unsentdata.elementAt(i))[0].equals(o[0])){
				unsentdata.setElementAt(o, i);
				return;
			}
		}
	
		if(unsentdata.size() < MAX_UNSENT_DATA)
			unsentdata.addElement(o);
		else
			throw new RuntimeException("Unbounded unsent channel data detected : check XML routing table settings");

	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	private boolean tryToSend(Object[] o){
		String destcd = ((String)o[0]).substring(0, ((String)o[0]).indexOf(".")); // CD name
		String dest = this.getCDLocation(destcd);			   // Corresponding SubSystem name

		GenericInterface gi = null;
		if(cachedintf.containsKey(dest)){
			gi = (GenericInterface)cachedintf.get(dest);
			gi.setup(o);
		}
		else{
			Vector l = ic.getInterfaces(ssname, dest);
			if(l.size() > 0){
				cachedintf.put(dest, l.elementAt(0));
				gi = (GenericInterface)l.elementAt(0);
				gi.setup(o);
			}
			else{
				System.err.println("SubSystem "+dest+" is not reacheable : "+destcd);
				return false;
			}
		}
		
		if(!gi.transmitData()){
			Vector l = ic.getInterfaces(ssname, dest);
			for(int i=0;i<l.size(); i++){
				gi = (GenericInterface)l.elementAt(i);
				gi.setup(o);
				if(gi.transmitData()){
					cachedintf.put(dest, gi);
					return true;
				}
			}
			System.err.println("SubSystem "+dest+" is not reacheable : "+destcd);
			return false;
		}
		else
			return true;
		
	}
	
	/**
	 * this is called from GenericInterface (both threaded non-threaded)
	 * @param o
	 */
	public void forwardChannelData(Object[] o){
		GenericChannel chan = (GenericChannel)getChannelInstance(((String)o[0]));
		String destcd = ((String)o[0]).substring(0, ((String)o[0]).indexOf("."));
		if(chan == null){
			if(this.getCDLocation(destcd).equals(ssname))
				System.out.println("The channel "+o[0]+" not present in the local sub-system - discarding received data");
			else{
				System.out.println("Trying to re-route channel data "+o[0]);
				this.addToUnsent(o);
			}
		}
		else
			chan.setBuffer(o);
	}
	
	/**
	 * Internal use
	 * @param ci
	 */
	private synchronized void resendUnsent(){
		for(int i=0;i<unsentdata.size(); i++){
			boolean done = tryToSend((Object[])unsentdata.elementAt(i));
			if(done){
				unsentdata.removeElementAt(i);
				i--;
			}
		}
	}

	/*
	 * TODO:
	 * 1. Need to pop out the element from the queue
	 * 2. Then choose correct GenericInterface object which can be retrieved from the Interconnection instance 'ic'
	 * 3. Execute GenericInterface.setup(Object[] o) followed by GenericInterface.transmitData()
	 * 
	 */
	private void transmit(){
		if(unsentdata.size() > 0) // is this safe?
			resendUnsent();
		
		while(!OutQueue.isEmpty()){
			Object[] o = (Object[])OutQueue.pop();
			boolean done = tryToSend(o);
			if(!done)
				this.addToUnsent(o);
		}
	}
	
	private void receive(){
		for(int i=0;i<LocalInterface.size(); i++)
			((GenericInterface)LocalInterface.elementAt(i)).receiveData();
	}

	public void run(){
		receive();
		transmit();
	}
	
	
	// For debugging purpose
	public Hashtable getcdmap(){return cdlocation;}
	public void printLocalInterface(){
		System.out.println("\nLocalInterface : ");
		System.out.println(this.LocalInterface);
	}
}
