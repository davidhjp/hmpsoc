package com.systemj.ipc;

import java.util.Hashtable;

/**
 * Must be compatible with CLDC 1.1
 * 
 * @author hpar081
 *
 */
public abstract class GenericInterface implements Runnable {

	public abstract void configure(Hashtable ht);

	/**
	 * Executed once after configure(), if this linke need to have a
	 * thread for communication, this method should spawn one.
	 */
	public abstract void invokeReceivingThread();

	public abstract boolean transmitData(Object[] o);

	/**
	 * Base classes may override this method to manage link
	 * connections/exchange any data with other subsystems. It is the
	 * designer's responsibility to periodically check if the current
	 * link is still connected to the remote subsystem and call
	 * {@link InterfaceManager#disconnect(String)} in case if the link
	 * is closed.
	 */
	public void connect() {
	};

}
