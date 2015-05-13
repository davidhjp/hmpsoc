package org.systemj.nodes;

import java.util.ArrayList;
import java.util.List;

public class ActionNode extends BaseGRCNode {
	public enum TYPE{
		GROUPED_JAVA, JAVA, EMIT, SIG_DECL
	}
	private TYPE type = TYPE.JAVA;

	private String stmt;
	private List<String> stmts = new ArrayList<String>(); 
	private String SigName;
	private String SigType;
	private String EmitVal;
	private boolean beforeTestNode = false;
	
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
		str += ind+"ActionNode, Type: "+this.type+", JOPID: "+jopid+"\n";
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
	
}





