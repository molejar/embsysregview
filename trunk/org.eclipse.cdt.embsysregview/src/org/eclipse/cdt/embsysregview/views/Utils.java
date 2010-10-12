package org.eclipse.cdt.embsysregview.views;

public class Utils {
	/**
	 * Converts from big endian to little endian and the other way round
	 * 
	 * @param value 32 bit big endian value
	 * @return 32 bit little endian value
	 * 
	 * In:  0xAABBCCDD
	 * Out: 0xDDCCBBAA
	 * 
	 */
	public static long convertbig2little(long value) {
		long a,b,c,d;
		
		a=value>>24;
		b=(value & (~0xFF00FFFFL))>>8;
		c=(value & (~0xFFFF00FFL))<<8;
		d=(value & (~0xFFFFFF00L))<<24;

		return( a | b | c | d );
	}
}
