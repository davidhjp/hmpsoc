package org.systemj.nodes;

public class TerminateNode extends BaseGRCNode {
	
	private String termcode;
	

	public String getTermcode() {
		return termcode;
	}


	public void setTermcode(String termcode) {
		this.termcode = termcode;
	}


	@Override
	public String dump(int indent) {
		String str = "";
		String ind = getIndent(indent,'-');
		str += ind +"TerminateNode\n";
		ind = getIndent(indent);
		str += ind + "TermCode: "+ termcode + "\n";
		
		for(BaseGRCNode child : children){
			str += child.dump(indent+1);
		}
		
		return str;
	}

}
