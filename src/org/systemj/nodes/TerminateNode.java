package org.systemj.nodes;

import java.io.PrintWriter;

import org.systemj.MemoryPointer;

public class TerminateNode extends BaseGRCNode {
	
	private int tcode;
	
	public TerminateNode(int t) { tcode = t; }
	public TerminateNode() { }
	

	public int getTermcode() {
		return tcode;
	}


	public void setTermcode(int termcode) {
		this.tcode = termcode;
	}


	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"TerminateNode, ID:"+id+"\n";
		ind = getIndent(indent);
		str += ind + "TermCode: "+ tcode + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}
	
	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		long t_ptr = mp.getTerminateCodePointer();
		pw.println("  LDR R0 #"+this.tcode);
		pw.println("  STR R0 $"+Long.toHexString(t_ptr+termcode)+"; term");
		
		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode, cdi);
		}
	}

}
