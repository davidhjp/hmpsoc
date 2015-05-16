package org.systemj.nodes;

import java.io.PrintWriter;

import org.systemj.MemoryPointer;

public class SwitchNode extends BaseGRCNode {
	private String CDName;
	private String Statename;

	public String getStatename() {
		return Statename;
	}

	public void setStatename(String statename) {
		Statename = statename;
	}

	public String getCDName() {
		return CDName;
	}

	public void setCDName(String cDName) {
		CDName = cDName;
	}

	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"SwitchNode\n";
		ind = getIndent(indent);
		str += ind + "Statename: "+ Statename + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode, int cdi) {
		pw.println("  LDR R0 $"+Integer.toHexString(mp.switchMap.get(Statename)));
		pw.println("  JMP R0; Jump to this switch child");
		
		for(int i=0; i<children.size(); i++){
			pw.print(Statename.toLowerCase()+"@"+i);
			this.getChild(i).weirdPrint(pw, mp, termcode, cdi);
		}
	}

	
}
