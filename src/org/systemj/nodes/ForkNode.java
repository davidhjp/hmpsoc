package org.systemj.nodes;

import java.io.PrintWriter;

import org.systemj.MemoryPointer;

public class ForkNode extends BaseGRCNode {

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		
		
		
		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode+1, cdi);
		}
	}

}
