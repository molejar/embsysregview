// This file is part of EmbSysRegView.
//
// EmbSysRegView is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// EmbSysRegView is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with EmbSysRegView.  If not, see <http://www.gnu.org/licenses/>.

package org.eclipse.cdt.embsysregview.views;

public class TreeField extends TreeElement {
	private byte bitOffset;
	private byte bitLength;
	private Interpretations interpretations;
	
	public TreeField(String name, String description, byte bitOffset, byte bitLength, Interpretations interpretations) {
		super(name, description);
		this.bitOffset=bitOffset;
		this.bitLength=bitLength;
		this.interpretations=interpretations;
	}
	
	public boolean hasValueChanged()
	{
		return (getOldValue()!=getValue());
	}

	public byte getBitOffset() {
		return bitOffset;
	}

	public byte getBitLength() {
		return bitLength;
	}

	public Interpretations getInterpretations() {
		return interpretations;
	}
	
	private long stripValue(long value) {
		if(value!=-1)
		{
			value >>= bitOffset;					// drop the unnecessary bits "below" the field
			value &= ~(0xFFFFFFFFL << bitLength);	// drop the bits "above" the field
		}
		return value;
	}
	
	private long getOldValue() {
		return stripValue(((TreeRegister)this.getParent()).getOldValue());
	}
	
	public long getValue() {
		return stripValue(((TreeRegister)this.getParent()).getValue());
	}
	
	public void setValue(long value) {
		value <<= bitOffset;
		long regValue = ((TreeRegister)this.getParent()).getValue();
		regValue &= ~(0xFFFFFFFFL >> (32-bitLength) << bitOffset);
		regValue |= value;
		
		((TreeRegister)this.getParent()).setAndWriteValue(regValue);
	}
	
	public String getInterpretation() {		
		if(interpretations.containsKey(getValue())) {
			return interpretations.getInterpretation(getValue());
		} else
			return "";
	}
	
	/*
	 * Check if Field has an actual interpretation for the current value
	 */
	public boolean hasInterpretation() {
		if(this.getInterpretation().equals(""))
			return false;
		else
			return true;
	}

	/*
	 * Check if Field has interpretations at all
	 */
	public boolean hasInterpretations() {
		return interpretations.hasInterpretations();
	}
}
