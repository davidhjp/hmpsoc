package systemj.interfaces;

import systemj.common.BaseInterface;

public abstract class GenericChannel extends BaseInterface{
	protected boolean modified = false;
	protected boolean init = false;
	protected Object value;
	public String PartnerName;
	public String Name;
	
	public GenericChannel() {}
	public boolean isInit(){ return init; }
	public void setInit(){ init = true; }
	
	// Modular
	protected Object[] toReceive = new Object[5];
	protected boolean incoming = false;
	
	// To be called by GenericInterface
	public synchronized void setBuffer(Object[] obj){
		if(toReceive.length < obj.length)
			toReceive = new Object[obj.length];
		for(int i=0;i<obj.length; i++)
			toReceive[i] = obj[i];
		incoming = true;
	}
	
	// SMCHAN
	protected boolean isLocal = true;
}
