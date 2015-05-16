package systemj.lib.emb;
import systemj.interfaces.*;
import systemj.lib.emb.input_Channel;

public class output_Channel extends GenericChannel{
	public input_Channel partner;
	private int preempted = 0;
	private int w_s = 0;
	private int w_r = 0;
	public output_Channel(){}
	public void set_value(Object in){this.value = in; this.modified = true;}
	public Object get_value(){return this.value; }
	public int refresh(){
		this.value = null;
		this.w_s = 0;
		this.w_r = 0;
		set_preempted();
		this.modified = true;
		return 1;
	}
	public void set_w_s(int in){this.w_s = in; this.modified = true;}
	public void set_w_r(int in){this.w_r = in; this.modified = true;}
	public int get_w_s(){return this.w_s;}
	public int get_w_r(){return this.w_r;}
	private int get_r_r(){return init ? partner.get_r_r() : 0;}
	public void update_w_r(){
		if(init){
			if(this.preempted == partner.get_preempted_val()) 
				this.w_r = get_r_r();
		}
	}
	public void set_partner(input_Channel partner){isLocal = true; this.partner = partner;}
	public int get_preempted_val(){return this.preempted; }
	public void set_preempted() {++this.preempted; ; this.modified = true;}
	
	/**
	 * Tests whether partner input channel is preempted or re-initialized
	 * @return <b>true</b> - when partner is preempted or re-initialized <br> <b>false</b> - otherwise
	 */
	public boolean get_preempted(){
		// Now output channel is preempted when the input channel is re-initialized (i.e. w_s < w_r)
		if(init){
			if(partner.get_preempted_val() > this.preempted || this.w_s < this.w_r) 
				return true; 
		}
		return false;
	}

	// Modular
	public void set_preempted(int num){this.preempted=num; this.modified = true;}
	public void gethook(){
		if(init){
			if(!isLocal && incoming){
				this.getBuffer();
				// Little trick to make sure that partner channel knows preemption status of this channel
				if(partner.get_preempted_val() < this.preempted) 
					modified = true;
			}
		}
	}
	public synchronized void getBuffer(){
		partner.set_r_s(((Integer)toReceive[1]).intValue());
		partner.set_r_r(((Integer)toReceive[2]).intValue());
		partner.set_preempted(((Integer)toReceive[3]).intValue());
		incoming = false;
	}
	
	public void sethook(){
		if(init){
			if(!isLocal && this.modified){
				//	toSend[0] = Boolean.TRUE;
				Object[] toSend = new Object[5];  // Creating an Object!!
				toSend[0] = PartnerName;
				toSend[1] = new Integer(this.get_w_s()); // Creating an Object!!
				toSend[2] = new Integer(this.get_w_r());
				toSend[3] = new Integer(this.get_preempted_val());
				if(value != null)
					toSend[4] = value;
				if(super.pushToQueue(toSend))
					this.modified = false; // This is set to false ONLY if the data is received by other side
			}
		}
	}
	
	public void setDistributed(){ isLocal = false; partner = new input_Channel(); }
}
