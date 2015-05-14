package org.systemj;

public class MemoryPointer {
	public long ptrInputSignal;
	public long ptrOutputSignal;
	public long ptrDataLock;
	public long ptrPreInternalSignal;
	public long ptrPreInputSignal;
	public long ptrPreOutputSignal;
	public long ptrProgramCounter;
	public long ptrTerminateCode;
	public long ptrSwitchNode;
	public long ptrLastAddressPlusOne;
	
	public long getDataLockSize() { return ptrDataLock - ptrPreInternalSignal;}
	public long getPreInternalSignalSize() { return ptrPreInternalSignal - ptrPreInputSignal; }
	public long getPreInputSignalSize() { return ptrPreInputSignal - ptrPreOutputSignal; }
	public long getPreOutputSignalSize() { return ptrPreOutputSignal - ptrProgramCounter; }
	public long getPrePCSize() { return ptrProgramCounter = ptrTerminateCode; }
	public long getTermSize() { return ptrTerminateCode - ptrSwitchNode; }
	public long getSwitchSize() { return ptrSwitchNode - ptrLastAddressPlusOne; }
}
