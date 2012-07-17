package org.eclipse.cdt.embsysregview.views;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private static Matcher getLineMatcher(String fulltext)
	{
		// Compile the pattern
	    String patternStr = "^(.*)$";
	    Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE);
	    return pattern.matcher(fulltext);
	}
	
	public static String getTextLine(int number, String fulltext)
	{
		Matcher matcher = getLineMatcher(fulltext);
	    
	    for(int i=1;i<=number;i++)
	    {
	    	if(matcher.find() && i==number)
	    		return  matcher.group(1);
	    }
	    return "";
	}
	
	public static String getFirstNotEmptyTextLine(String fulltext)
	{
		Matcher matcher = getLineMatcher(fulltext);
	    
	    while(matcher.find())
	    {
	    	if(!matcher.group(1).equals(""))
	    		return matcher.group(1);
	    }
	    return "";
	}
	
	public static int countTextLines(String fulltext)
	{
		int i=0;
		
		Matcher matcher = getLineMatcher(fulltext);
		
	    while(matcher.find())
	    	i++;
	    
	    return i;
	}
	
	public static int getBitFromValue(int bitnr, long value)
	{
		return (int) ((value>>bitnr)%2);
	}
	
	public static String longtobinarystring(long wert, int bits) {
		StringBuilder text = new StringBuilder();

		for (int i = bits; i > 0; i--) {
			text.append(Math.abs(wert % 2));
			wert /= 2;
		}
		text.reverse();
		return text.toString();
	}
	
	public static String longtoHexString(long wert, int bits) {
		
		int hexdigits=bits/4;
		if(bits%4!=0)
			hexdigits++;
		
		StringBuilder hex = new StringBuilder();
		hex.append("0x");
		String x = Long.toHexString(wert);
		int missingLen = hexdigits - x.length();
		for (int i = 0; i < missingLen; i++)
			hex.append('0');
		hex.append(x.toUpperCase());
		return hex.toString();
	}
	
	// Maximum values for signed data types of the marked bit length.
	public static final BigInteger Max8Bit  = BigInteger.valueOf(Byte.MAX_VALUE).add(BigInteger.ONE);
	public static final BigInteger Max16Bit = BigInteger.valueOf(Short.MAX_VALUE).add(BigInteger.ONE);
	public static final BigInteger Max32Bit = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
	public static final BigInteger Max64Bit = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
	
	// Constants for the architecture bit depth.
	public static final int Arch8Bit  = 8;
	public static final int Arch16Bit = 16;
	public static final int Arch32Bit = 32;
	public static final int Arch64Bit = 64;
	
	/**
	 * Convert a signed value of bitdepth to its unsigned equivilent.
	 * @param signed Signed value.
	 * @param bitdepth Target bit depth.
	 * @return Returns the unsigned value for the signed parameter.
	 */
	public static BigInteger makeUnsigned(BigInteger signed, int bitdepth) {
		BigInteger unsigned  = signed;
		BigInteger MaxSignedValue;
		if(null == signed) return unsigned;
		if(signed.compareTo(BigInteger.ZERO) < 0) {
			switch(bitdepth)
			{
				case Arch8Bit:
					MaxSignedValue = Max8Bit;
					break;
				case Arch16Bit:
					MaxSignedValue = Max16Bit;
					break;
				case Arch32Bit:
					MaxSignedValue = Max32Bit;
					break;
				case Arch64Bit:
					MaxSignedValue = Max64Bit;
					break;
				default:
					MaxSignedValue = Max32Bit;
					break;
			}
			unsigned = MaxSignedValue.add(MaxSignedValue.add(signed));
		}
		
		return unsigned;
	}
}
