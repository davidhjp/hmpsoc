package com.systemj.hmpsoc.util;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Helper {
	public static Logger log;
	
	static {
		log = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
		log.setLevel(Level.WARNING);
	}
}
