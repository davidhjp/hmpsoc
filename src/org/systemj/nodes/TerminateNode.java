package org.systemj.nodes;

public class TerminateNode extends BaseGRCNode {
	
	private int termcode;
	
	public TerminateNode(int t) { termcode = t; }
	public TerminateNode() { }
	

	public int getTermcode() {
		return termcode;
	}


	public void setTermcode(int termcode) {
		this.termcode = termcode;
	}


	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"TerminateNode, ID:"+id+"\n";
		ind = getIndent(indent);
		str += ind + "TermCode: "+ termcode + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

}
