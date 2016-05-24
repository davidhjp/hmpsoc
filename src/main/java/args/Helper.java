package args;

import java.util.Map;

import org.apache.commons.cli.CommandLine;


public class Helper {
	public final static String HELP_OPTION = "h";
	public final static String HELP_LONG_OPTION = "help";
	public final static String VERBOSE_OPTION = "v";
	public final static String VERBOSE_LONG_OPTION = "verbose";
	public final static String CONFIG_OPTION = "c";
	public final static String CONFIG_LONG_OPTION = "config";
	public static final String D_OPTION = "d";
	public static final String JOP_RECOP_NUM_OPTION = "j";
	public static final String DIST_MEM_OPTION = "i";
	public static final String DIST_MEM_LONG_OPTION = "distmem";
	public static final String COMPILE_ONLY_OPTION = "S";
	public static final String DYN_DISPATCH_OPTION = "y";
	public static final String METHOD_OPTION = "m";
	public static final String LINK_OPTION = "l";
	public static final String SCHEDULING_OPTION = "s";
	
	public static String SCHED_POLICY = "NONE";
	public static final String SCHED_1 = "1";
	
	public static void setSchedulingPolicty(){
		SCHED_POLICY = cmd.getOptionValue(SCHEDULING_OPTION);
	}
	
	private static CommandLine cmd;
	
	public static void setSingleArgInstance (CommandLine c) {
		if(cmd == null)
			cmd = c;
	}
	
	public static CommandLine getSingleArgInstance() {return cmd;}
	
	public static Mapping pMap = new Mapping();
	
	public static class Mapping {
		public int nJOP = 1;
		public int nReCOP = 1;
		public Map<String, Integer> rAlloc;
	}
	
}
