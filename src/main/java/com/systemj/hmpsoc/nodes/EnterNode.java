package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;

import com.systemj.hmpsoc.MemoryPointer;

public class EnterNode extends BaseGRCNode {
	
	private String statename;
	private String statecode;

	public String getStatename() {
		return statename;
	}

	public void setStatename(String statename) {
		this.statename = statename;
	}

	public String getStatecode() {
		return statecode;
	}

	public void setStatecode(String statecode) {
		this.statecode = statecode;
	}

	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"EnterNode, ID: "+id+"\n";
		ind = getIndent(indent);
		str += ind + "Statename: "+ statename + "\n";
		str += ind + "Statecode: " + statecode + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi, BaseGRCNode directParent) {
		pw.println("  LDR R10 #"+statename.toLowerCase()+"_"+statecode);
		pw.println("  STR R10 $"+Long.toHexString(mp.getSwitchNodePointer()+mp.switchMap.get(statename))+"; encoding "+statename);
		
		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode, cdi, this);
		}
	}

}
