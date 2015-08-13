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
