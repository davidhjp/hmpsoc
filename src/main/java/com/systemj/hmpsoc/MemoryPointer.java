package com.systemj.hmpsoc;

import java.util.HashMap;
import java.util.Map;

public class MemoryPointer {
	public static final int WORD_SIZE = 16;
	public static final int ptrInputSignal        = 0;
	public static final int ptrOutputSignal       = 1;
	public static final int ptrDataLock           = 2;
	public static final int ptrInternalSignal     = 3;
	public static final int ptrPreInternalSignal  = 4;
	public static final int ptrPreInputSignal     = 5;
	public static final int ptrPreOutputSignal    = 6;
	public static final int ptrProgramCounter     = 7;
	public static final int ptrTerminateCode      = 8;
	public static final int ptrSwitchNode         = 9;
	
	public static final int ptrLastAddressPlusOne = 10; // This must be fixed
	private Long[] ptrMemory = new Long[11];
	public Map<String, Integer> signalMap = new HashMap<String, Integer>();
	public Map<String, Integer> insignalMap = new HashMap<String, Integer>();
	public Map<String, Integer> osignalMap = new HashMap<String, Integer>();
	public Map<String, Integer> switchMap = new HashMap<String, Integer>();
	
	public long cc = 0;
	private int toplevelThnum = 0;
	
	
	public int getToplevelThnum() {
		return toplevelThnum;
	}
	public void setToplevelThnum(int toplevel_thnum) {
		this.toplevelThnum = toplevel_thnum;
	}
	public long getSizeSwitchNode(){
		return ptrMemory[ptrSwitchNode+1] - ptrMemory[ptrSwitchNode];
	}
	
	public long getSizeTerminateCode(){
		return ptrMemory[ptrTerminateCode+1] - ptrMemory[ptrTerminateCode];
	}
	
	public long getSizeProgramCounter(){
		return ptrMemory[ptrProgramCounter+1] - ptrMemory[ptrProgramCounter];
	}
	
	public long getSizePreOutputSignal(){
		return ptrMemory[ptrPreOutputSignal+1] - ptrMemory[ptrPreOutputSignal];
	}
	
	public long getSizePreInputSignal(){
		return ptrMemory[ptrPreInputSignal+1] - ptrMemory[ptrPreInputSignal];
	}
	
	public long getSizePreInternalSignal(){
		return ptrMemory[ptrPreInternalSignal+1] - ptrMemory[ptrPreInternalSignal];
	}
	
	public long getSizeInternalSignal(){
		return ptrMemory[ptrInternalSignal+1] - ptrMemory[ptrInternalSignal];
	}
	
	public long getSizeDataLock(){
		return ptrMemory[ptrDataLock+1] - ptrMemory[ptrDataLock];
	}
	
	public long getSizeOutputSignal(){
		return ptrMemory[ptrOutputSignal+1] - ptrMemory[ptrOutputSignal];
	}
	
	public long getSizeInputSignal(){
		return ptrMemory[ptrInputSignal+1] - ptrMemory[ptrInputSignal];
	}
	
	
	public long getLastAddr(){
		return ptrMemory[ptrLastAddressPlusOne];
	}
	public void setLastAddr(long i){
		ptrMemory[ptrLastAddressPlusOne] = i;	
	}
	
	public long getSwitchNodePointer(){
		return ptrMemory[ptrSwitchNode];
	}
	public void setSwitchNodePointer(long i){
		ptrMemory[ptrSwitchNode] = i;
	}
	
	public long getTerminateCodePointer(){
		return ptrMemory[ptrTerminateCode];
	}
	public void setTermianteCodePointer(long i){
		ptrMemory[ptrTerminateCode] = i;
	}
	
	public long getProgramCounterPointer(){
		return ptrMemory[ptrProgramCounter];
	}
	public void setProgramCounterPointer(long i){
		ptrMemory[ptrProgramCounter] = i;
	}
	
	public long getPreOutputSignalPointer(){
		return ptrMemory[ptrPreOutputSignal];
	}
	public void setPreOutputSignalPointer(long i){
		ptrMemory[ptrPreOutputSignal] = i;
	}
	
	public long getPreInputSignalPointer(){
		return ptrMemory[ptrPreInputSignal];
	}
	public void setPreInputSignalPointer(long i){
		ptrMemory[ptrPreInputSignal] = i;
	}
	
	public long getPreInternalSignalPointer(){
		return ptrMemory[ptrPreInternalSignal];
	}
	public void setPreInternalSignalPointer(long i){
		ptrMemory[ptrPreInternalSignal] = i;
	}
	
	public long getInternalSignalPointer(){
		return ptrMemory[ptrInternalSignal];
	}
	public void setInternalSignalPointer(long i){
		ptrMemory[ptrInternalSignal] = i;
	}
	
	public long getDataLockPointer(){
		return ptrMemory[ptrDataLock];
	}
	public void setDataLockPointer(long i){
		ptrMemory[ptrDataLock] = i;
	}
	
	public long getInputSignalPointer(){
		return ptrMemory[ptrInputSignal];
	}
	public void setInputSignalPointer(long i){
		ptrMemory[ptrInputSignal] = i;
	}
	
	public long getOutputSignalPointer(){
		return ptrMemory[ptrOutputSignal];
	}
	public void setOutputSignalPointer(long i){
		ptrMemory[ptrOutputSignal] = i;
	}
}
