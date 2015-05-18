package org.systemj.nodes;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.systemj.MemoryPointer;

public class ActionNode extends BaseGRCNode {
	public enum TYPE{
		GROUPED_JAVA, JAVA, EMIT, SIG_DECL, EXIT
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
	

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		switch(type){
			case EMIT:
				if(mp.osignalMap.containsKey(this.SigName)){
					long c = mp.osignalMap.get(SigName);
					long c2 = 1 << c;
					pw.println("  LDR R0 $"+Long.toHexString(mp.getOutputSignalPointer()));
					pw.println("  OR R0 R0 #$"+Long.toHexString(c2));
					pw.println("  STR R0 $"+Long.toHexString(mp.getOutputSignalPointer())+"; Emitted OSig "+SigName);
				}
				else if(mp.signalMap.containsKey(this.SigName)){
					int c = mp.signalMap.get(SigName);
					long c1 = (c / mp.WORD_SIZE) + mp.getInternalSignalPointer();
					long c2 = 1 << (c % mp.WORD_SIZE);
					pw.println("  LDR R0 $"+Long.toHexString(c1));
					pw.println("  OR R0 R0 #$"+Long.toHexString(c2));
					pw.println("  STR R0 $"+Long.toHexString(c1)+"; Emitted IntSig "+SigName);
				}
				else throw new RuntimeException("Could not resolve the signal: "+SigName);

				if(this.hasEmitVal()){
					long dl_ptr = mp.getDataLockPointer();
					long tnum = this.getThnum() - mp.getToplevelThnum();
					dl_ptr += tnum;
					long cn = this.casenumber + 32768;
					pw.println("  LDR R11 $"+Long.toHexString(dl_ptr)+"; Thread is locked");
					pw.println("  LDR R0 #$"+Long.toHexString(cn));
					pw.println("  DCALLNB R0; Emit val casenumber "+casenumber);
				}
				
				break;
			case GROUPED_JAVA:
			case JAVA:
				long dl_ptr = mp.getDataLockPointer();
				long tnum = this.getThnum() - mp.getToplevelThnum();
				dl_ptr += tnum;
				long cn = this.casenumber + 32768;
				pw.println("  LDR R11 $"+Long.toHexString(dl_ptr)+"; Thread is locked");
				pw.println("  LDR R0 #$"+Long.toHexString(cn));
				pw.println("  DCALLNB R0; Java casenumber "+casenumber);
				break;
			case SIG_DECL:
			case EXIT:
				// Don't do anything
				break;
		}
		
		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode, cdi);
		}
		
	}
	
}





