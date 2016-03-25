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
			int cdi, BaseGRCNode directParent) {
		pw.println("; Forking reactions");
		// Storing PC before forking.. needed for interleaving
		{
			long pc_ptr = mp.getProgramCounterPointer();
			long ttnum = this.getThnum() - mp.getToplevelThnum();
			pc_ptr += ttnum;
			pw.println("  STRPC $"+Long.toHexString(pc_ptr)+"; Storing PC for this fork node until normal termination");
		}
		
		termcode++;
	
		for(int i=0; i<this.getNumChildren(); i++){
			BaseGRCNode child = this.getChild(i);
			
			// Need to jump to the pos if data-call is pending
			long pc_ptr = mp.getProgramCounterPointer();
			long ttnum = child.getThnum() - mp.getToplevelThnum();
			pc_ptr += ttnum;
			pw.println("  LDR R10 $"+Long.toHexString(pc_ptr));
			String label = "NOTPENDING_CD"+cdi+"_"+(mp.cc++);
			pw.println("  PRESENT R10 "+label);
			pw.println("  JMP R10");
			pw.print(label);
			
			child.weirdPrint(pw, mp, termcode, cdi, this);
		}
		
		// Traversing JoinNode
		
		pw.println("  LDR R10 $"+(Long.toHexString(termcode+mp.getTerminateCodePointer()))+"; Loading term code");
		termcode--;
		
//		for(BaseGRCNode parent : jn.getParents()){
//			TerminateNode tn = (TerminateNode) parent;
//			if(tn.getTermcode() == TerminateNode.MAX_TERM){
//				pw.println("  SUBV R1 R10 #$"+Integer.toHexString(TerminateNode.MAX_TERM));
//				pw.println("  PRESENT R1 AJOIN"+cdi+"; Pausing cd");
//				break;
//			}
//		}
		
		HashMap<Integer, String> lbs = new HashMap<Integer, String>();
		for(BaseGRCNode child : jn.getChildren()){
			long cc = (mp.cc++);
			int tncode = 0;
			String label = null;
			if(child instanceof TerminateNode){
				TerminateNode tn = (TerminateNode)child;
				tncode = tn.getTermcode();
				label = "JOINBR"+cc+"CD"+cdi+"CODE"+tncode;
			}
			else{
				tncode = 0;
				label = "JOINBR"+cc+"CD"+cdi+"CODE"+tncode;
			}
			if(!lbs.containsKey(tncode)){
				pw.println("  SUBV R1 R10 #"+tncode);
				pw.println("  PRESENT R1 "+label+"; joinchild");
				lbs.put(tncode, label);
			}
		}
		
		String joinlabel = "JOIN"+(mp.cc++)+"CD"+cdi;
		
		for(BaseGRCNode child : jn.getChildren()){
			String label = null;
			if(child instanceof TerminateNode){
				label = lbs.get(((TerminateNode) child).getTermcode());
			}
			else{
				label = lbs.get(0);
			}
			pw.print(label);
			child.weirdPrint(pw, mp, termcode, cdi, this);
			pw.println("  JMP "+joinlabel);
		}
		pw.println(joinlabel);
	}
}
