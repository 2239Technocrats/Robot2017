package org.usfirst.frc.team2239.robot;

import java.util.*;

public class ControlScheme {
	private final String SP = "SPEED";
	private final String SH = "SHIFT";
	private final String GT = "GRABBER_TOGGLE";
	private final String LR = "LIFT_RAISE";
	private final String LL = "LIFT_LOWER";
	private final String GS = "GRABBER_SUCC";
	private final String GB = "GRABBER_BLOW";
	private final String SS = "SPREADSHEET_SAVE";
	private final String TCX = "TOGGLE_CONTROLLER";
	private final String TCXX = "TOGGLE_CONTROLLER";
	private final String TCXJ = "TOGGLE_CONTROLLER";
	private final String TCJ = "TOGGLE_CONTROLLER";
	private final String TCJJ = "TOGGLE_CONTROLLER";
	
	private final String LS = "LEFT_SIDE";
	private final String RS = "RIGHT_SIDE";
	
	
	public HashMap<String, Integer> x = new HashMap<String, Integer>();
	public HashMap<String, Integer> xx = new HashMap<String, Integer>();
	public HashMap<String, Integer> xj = new HashMap<String, Integer>();
	public HashMap<String, Integer> j = new HashMap<String, Integer>();
	public HashMap<String, Integer> jj = new HashMap<String, Integer>();
	
	public ControlScheme() {
		// buttons
		x.put(SP, 1);
		x.put(SH, 2);
		x.put(LL, 3);
		x.put(LR, 4);
		x.put(GT, 5);
		x.put(GT, 6);
		x.put("NONE1", 7);
		x.put("NONE2", 8);
		x.put("NONE3", 9);
		x.put("NONE4", 10);
		// axis
		x.put("NOTE5", 0);
		x.put(LS, 1);
		x.put(GS, 2);
		x.put(GB, 3);
		x.put("NONE6", 4);
		x.put(RS, 5);
		// POV
		x.put(TCX, 1);
		x.put(TCXX, 2);
		x.put(TCXJ, 3);
		x.put(TCJ, 4);
		x.put(TCJJ, 5);
		x.put("ss", 6);
		x.put("NONE7", 7);
		x.put("NONE8", 8);
		
//		// buttons
//		j.put(SP, 1);
//		j.put(SH, 2);
//		j.put(LL, 3);
//		j.put(LR, 4);
//		j.put(GT, 5);
//		j.put(GT, 6);
//		j.put("NONE", 7);
//		j.put("NONE", 8);
//		j.put("NONE", 9);
//		j.put("NONE", 10);// buttons
//		
//		// axis
//		j.put("NOTE", 0);
//		j.put(LS, 1);
//		j.put(GS, 2);
//		j.put(GB, 3);
//		j.put("NONE", 4);
//		j.put(RS, 5);
//		// POV
//		j.put(TC, 1);
//		j.put("NONE", 2);
//		j.put("NONE", 3);
//		j.put("NONE", 4);
//		j.put(SS, 5);
//		j.put("NONE", 6);
//		j.put("NONE", 7);
//		j.put("NONE", 8);
	}
	
	
	
	
	
	
	
	
	
	
}