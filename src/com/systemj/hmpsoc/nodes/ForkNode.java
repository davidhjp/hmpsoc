package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;
import java.util.HashMap;

import com.systemj.hmpsoc.MemoryPointer;

public class ForkNode extends BaseGRCNode {
	
	private JoinNode jn = null;
	
	public JoinNode getJoin() {
		return jn;
	}

	public void setMatchingJoin(){
		JoinNode j = getMatchingJoinNow(-1);
		if(j == null)
			throw new RuntimeException("Could not resolve fork-join");
		jn = j;
	}

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		pw.println("; Forking reactions");
		termcode++;
	
		for(BaseGRCNode child : this.getChildren()){
			child.weirdPrint(pw, mp, termcode, cdi);
		}
		
		// Traversing JoinNode
		
		pw.println("  LDR R1 $"+(Long.toHexString(termcode+mp.getTerminateCodePointer()))+"; Loading term code");
		termcode--;
		
		for(BaseGRCNode parent : jn.getParents()){
			TerminateNode tn = (TerminateNode) parent;
			if(tn.getTermcode() == TerminateNode.MAX_TERM){
				pw.println("  SUBV R1 R10 #$"+Integer.toHexString(TerminateNode.MAX_TERM));
				pw.println("  PRESENT R1 AJOIN"+cdi+"; Pausing cd");
				break;
			}
		}
		
		HashMap<Integer, String> lbs = new HashMap<Integer, String>();
		for(BaseGRCNode child : jn.getChildren()){
			long cc = (mp.cc++);
			int tncode = 0;
			String label = null;
			if(child instanceof TerminateNode){
				TerminateNode tn = (TerminateNode)child;
				tncode = tn.getTermcode();
				label = "JOIN"+cc+"CD"+cdi+"CODE"+tncode;
			}
			else{
				tncode = 0;
				label = "JOIN"+cc+"CD"+cdi+"CODE"+tncode;
			}
			pw.println("  SUBV R1 R10 #"+tncode);
			pw.println("  PRESENT R1 "+label+"; joinchild");
			lbs.put(tncode, label);
		}
		
		for(BaseGRCNode child : jn.getChildren()){
			String label = null;
			if(child instanceof TerminateNode){
				label = lbs.get(((TerminateNode) child).getTermcode());
			}
			else{
				label = lbs.get(0);
			}
			pw.print(label);
			child.weirdPrint(pw, mp, termcode, cdi);
		}
	}
}
