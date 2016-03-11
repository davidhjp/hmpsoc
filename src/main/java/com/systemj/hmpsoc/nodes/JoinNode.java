package com.systemj.hmpsoc.nodes;

import java.io.PrintWriter;
import java.util.HashMap;

import com.systemj.hmpsoc.MemoryPointer;

public class JoinNode extends BaseGRCNode {

	@Override
	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
			int cdi) {
		return;
	}

	
	
	
//	@Override
//	public void weirdPrint(PrintWriter pw, MemoryPointer mp, int termcode,
//			int cdi) {
//
//		pw.println("  LDR R1 $"+Integer.toHexString(termcode)+"; Loading term code");
//
//		termcode--;
//
//		HashMap<Integer, String> lbs = new HashMap<Integer, String>();
//		for(BaseGRCNode child : this.getChildren()){
//			long cc = (mp.cc++);
//			int tncode = 0;
//			String label = null;
//			if(child instanceof TerminateNode){
//				TerminateNode tn = (TerminateNode)child;
//				tncode = tn.getTermcode();
//				label = "JOIN"+cc+"CODE"+tncode;
//			}
//			else{
//				tncode = 0;
//				label = "JOIN"+cc+"CODE"+tncode;
//			}
//			pw.println("  SUBV R1 R0 #"+tncode);
//			pw.println("  PRESENT R1 "+label);
//			lbs.put(tncode, label);
//		}
//
//		for(BaseGRCNode child : this.getChildren()){
//			String label = null;
//			if(child instanceof TerminateNode){
//				label = lbs.get(((TerminateNode) child).getTermcode());
//			}
//			else{
//				label = lbs.get(0);
//			}
//			pw.print(label);
//			child.weirdPrint(pw, mp, termcode, cdi);
//		}
//	}

}
