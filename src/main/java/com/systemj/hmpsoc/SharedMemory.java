package com.systemj.hmpsoc;

import java.util.HashMap;
import java.util.Map;

public class SharedMemory {

	public static final int DEPTH_CHAN = 2;
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

	public void insertChannel(String chan) {
		MemorySlot ms = new MemorySlot();
		ms.start = pointer;
		ms.depth = DEPTH_CHAN;
		chanMap.put(chan, ms);
		incChannel();
	}

	public void insertSignal(String sig) {
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
}
