package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.systemj.hmpsoc.DeclaredObjects;
import com.systemj.hmpsoc.MemoryPointer;

import args.Helper;

public class ActionNode extends BaseGRCNode {
	public enum TYPE{
		GROUPED_JAVA, JAVA, EMIT, SIG_DECL, EXIT
	}

	private static int nextJopId = 1;
	private static synchronized int getNextJopId() {
		int numJops = Helper.pMap.nJOP;
		if (numJops <= 1) return 0;
		int jopId = nextJopId;
		nextJopId = (nextJopId+1) % numJops;
		if (nextJopId == 0) nextJopId = 1;
		return jopId;
	}

	public int getJOPIDDist() {
		int i = this.getThnum() % (Helper.pMap.nJOP-1);
		return i+1;
	}


	private TYPE type = TYPE.JAVA;

	private String stmt;
	private List<String> stmts = new ArrayList<String>(); 
	private String SigName;
	private String SigType;
	private String EmitVal;
	private boolean beforeTestNode = false;
	private String Capturing;
	private int ExitCode;
	
	public String getCapturing() {
		return Capturing;
	}

	public void setCapturing(String capturing) {
		Capturing = capturing;
	}

	public int getExitCode() {
		return ExitCode;
	}

	public void setExitCode(int exitCode) {
		ExitCode = exitCode;
	}

	public String getSigType() {
		return SigType;
	}

	public void setSigType(String sigType) {
		SigType = sigType;
	}
	
	public String getSigName() {
		return SigName;
	}

	public void setSigName(String sigName) {
		SigName = sigName;
	}

	public String getEmitVal() {
		return EmitVal;
	}
	
	public boolean hasEmitVal(){
		return EmitVal != null;
	}

	public void setEmitVal(String emitVal) {
		EmitVal = emitVal;
	}

	public boolean isGrouped() {
		return type == TYPE.GROUPED_JAVA;
	}
	
	public TYPE getActionType() {
		return type;
	}

	public void setActionType(TYPE type) {
		this.type = type;
	}


	public List<String> getStmts() {
		return stmts;
	}

	public void setStmts(List<String> stmts) {
		this.stmts = stmts;
	}

	public void addStmt(String s){
		stmts.add(s);
	}
	
	public String getStmt(int i){
		return stmts.get(i);
	}
	
	public int getNumStmts(){
		return stmts.size();
	}

	public String getStmt() {
		return stmt;
	}

	public void setStmt(String stmt) {
		this.stmt = stmt;
	}

	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind+"ActionNode, Type: "+this.type+", Case: "+this.casenumber+", JOPID: "+jopid+", ID: "+id+"\n";
		ind = getIndent(indent);
		
		if(!this.isGrouped()){
			if(this.type == TYPE.EMIT){
				str += ind+"Emit: "+this.SigName+", Type: "+this.SigType+"\n";
			}
			if(stmt !=null)
				str += ind+stmt+"\n";
		}
		else{
			for(String s : stmts){
				str += ind+s+"\n";
			}
		}
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		return str;
	}
	
	
	private int casenumber = -1;
	private int jopid = 0;

	public int getCasenumber() {
		return casenumber;
	}

	public void setCasenumber(int casenumber) {
		this.casenumber = casenumber;
	}

	public int getJopid() {
		return jopid;
	}

	public void setJopid(int jopid) {
		this.jopid = jopid;
	}

	public boolean isBeforeTestNode() {
		return beforeTestNode;
	}

	public void setBeforeTestNode(boolean beforeTestNode) {
		this.beforeTestNode = beforeTestNode;
	}
	
	private void genTLElseLabel(MemoryPointer mp, int cdi) {
		((TestLock)this.getChild(0)).generateElseLabel(mp, cdi);
	}
	
	private void printJOPPending(PrintWriter pw, MemoryPointer mp){
		long pc_ptr = mp.getProgramCounterPointer();
		long ttnum = this.thnum - mp.getToplevelThnum();
		pc_ptr += ttnum;
		pw.println("  STRPC $"+Long.toHexString(pc_ptr)+"; JOP availabilty check");
		pw.println("  LSIP R10");
		pw.println("  AND R1 R10 #1");
		pw.println("  SUBV R10 R1 #1");
		String nf = ((TestLock)this.getChild(0)).getElseLabel();
		pw.println("  PRESENT R10 "+nf+"; checking if it is okay to launch dcall");
//		pw.println("  STR R11 $"+Long.toHexString(pc_ptr)+"; Clearing PC");
	}
	
	private void printDataCall(PrintWriter pw, MemoryPointer mp, DeclaredObjects doo, int cdi) {
		long dl_ptr = mp.getDataLockPointer();
		long tnum = this.getThnum() - mp.getToplevelThnum();
		int jopId = 0;

		if (Helper.getSingleArgInstance().hasOption(Helper.DIST_MEM_OPTION))
			jopId = getJOPIDDist();
		else
			jopId = getNextJopId();

		dl_ptr += tnum;
		pw.println("  STR R11 $" + Long.toHexString(dl_ptr) + "; Thread is locked");
		boolean dyn = Helper.getSingleArgInstance().hasOption(Helper.DYN_DISPATCH_OPTION);
		if(dyn) genTLElseLabel(mp, cdi);
		if (Helper.getSingleArgInstance().hasOption(Helper.COMPILE_ONLY_OPTION)) {
			if(dyn) printJOPPending(pw, mp);
			pw.println("  LDR R10 @Datacall(\"" + doo.getCDName() + "\", \"" + jopId + "\", \"" + casenumber + "\") " + dCallAnnotFormat());
			pw.println("  DCALLNB R10 #$" + Long.toHexString(0x8000 | (jopId << 8) | (cdi & 0xFF)) + "; DCALL - jop=" + jopId + ", cd=" + cdi + ", casenumber=" + casenumber);
		} else {
			if(dyn) printJOPPending(pw, mp);
			pw.println("  LDR R10 #" + casenumber);
			pw.println("  DCALLNB R10 #$" + Long.toHexString(0x8000 | (jopId << 8) | (cdi & 0xFF)) + "; DCALL - jop=" + jopId + ", cd=" + cdi + ", casenumber=" + casenumber);
		}
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi, BaseGRCNode directParent, DeclaredObjects doo) {
		
		switch(type){
			case EMIT:
				if(mp.osignalMap.containsKey(this.SigName)){
					long c = mp.osignalMap.get(SigName);
					long c2 = 1 << c;
					pw.println("  LDR R10 $"+Long.toHexString(mp.getOutputSignalPointer()));
					pw.println("  OR R10 R10 #$"+Long.toHexString(c2));
					pw.println("  STR R10 $"+Long.toHexString(mp.getOutputSignalPointer())+"; Emitted OSig "+SigName);
				}
				else if(mp.signalMap.containsKey(this.SigName)){
					int c = mp.signalMap.get(SigName);
					long c1 = (c / mp.WORD_SIZE) + mp.getInternalSignalPointer();
					long c2 = 1 << (c % mp.WORD_SIZE);
					pw.println("  LDR R10 $"+Long.toHexString(c1));
					pw.println("  OR R10 R10 #$"+Long.toHexString(c2));
					pw.println("  STR R10 $"+Long.toHexString(c1)+"; Emitted IntSig "+SigName);
				}
				else throw new RuntimeException("Could not resolve the signal: "+SigName);

				if(this.hasEmitVal()){
					printDataCall(pw, mp, doo, cdi);
				}
				break;
			case GROUPED_JAVA:
			case JAVA:
				printDataCall(pw, mp, doo, cdi);
				break;
			case SIG_DECL:
			case EXIT:
				// Don't do anything
				break;
		}
		
		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode, cdi, this, doo);
		}
		
	}
	
}





