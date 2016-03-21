package com.systemj.hmpsoc;

import java.util.HashMap;
import java.util.Map;

public class SharedMemory {

	public static final int DEPTH_CHAN = 3;
	public static final int DEPTH_SIGNAL = 1;

	private long pointer = 0;

	class MemorySlot {
		public long start;
		public long depth;
	}

	private Map<String, MemorySlot> chanMap = new HashMap<>();
	private Map<String, MemorySlot> sigMap = new HashMap<>();

	private void incChannel() {
		pointer += DEPTH_CHAN;
	}

	private void incSignal() {
		pointer += DEPTH_SIGNAL;
	}
	
	public void addChannel(String newchan, String partner) {
		MemorySlot ms = new MemorySlot();
		MemorySlot p = chanMap.get(partner);
		if(p != null){
			ms.start = p.start;
			ms.depth = p.depth;
			chanMap.put(newchan, p);
		} else {
			throw new Error("Tried to link with the unknown channel : " + partner);
		}
	}

	public void addChannel(String chan) {
		MemorySlot ms = new MemorySlot();
		ms.start = pointer;
		ms.depth = DEPTH_CHAN;
		chanMap.put(chan, ms);
		incChannel();
	}

	public void addSignal(String sig) {
		MemorySlot ms = new MemorySlot();
		ms.start = pointer;
		ms.depth = DEPTH_SIGNAL;
		sigMap.put(sig, ms);
		incSignal();
	}

	public MemorySlot getChanMem(String chan) {
		return chanMap.get(chan);
	}

	public boolean hasChan(String chan) {
		return chanMap.containsKey(chan);
	}

	public MemorySlot getSigMem(String sig) {
		return sigMap.get(sig);
	}

	public boolean hasSig(String sig) {
		return sigMap.containsKey(sig);
	}

	public long getPointer() {
		return pointer;
	}
	
	public void incPointer() {
		pointer++;
	}
}
