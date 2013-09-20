package com.cyberlightning.android.coap;

import java.util.BitSet;

public class Convenience {
	
	private byte[] message;
	
	
	public static byte convert(BitSet bits, int offset) {
		  byte value = 0;
		  for (int i = offset; (i < bits.length() && ((i + offset) < 8)) ; ++i) {
		    value += bits.get(i) ? (1 << i) : 0;
		  }
		  return value;
	}
	
	
	
	
	
		
	
}
