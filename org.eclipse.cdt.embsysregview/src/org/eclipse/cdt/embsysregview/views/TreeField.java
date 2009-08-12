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

import java.util.HashMap;

public class TreeField extends TreeElement {
	private byte bitOffset;
	private byte bitLength;
	private HashMap <String,String> interpretations;
	private long old_value=-1;
	private long value=-1;
	
	public TreeField(String name, String description, byte bitOffset, byte bitLength, HashMap<String, String> interpretations) {
		super(name, description);
		this.bitOffset=bitOffset;
		this.bitLength=bitLength;
		this.interpretations=interpretations;
	}
	
	public boolean hasValueChanged()
	{
		return (old_value!=value);
	}

	public byte getBitOffset() {
		return bitOffset;
	}

	public byte getBitLength() {
		return bitLength;
	}

	public HashMap<String, String> getInterpretations() {
		return interpretations;
	}
	
	public long getValue() {
		return value;
	}
	
	public void updateValue() {
		long registerValue=-1;
		
		TreeElement parent = this.getParent();
		if (parent instanceof TreeRegister)
		{
			registerValue=((TreeRegister)parent).getValue();
			if(registerValue!=-1)
			{
				registerValue >>= bitOffset;					// drop the unnecessary bits "below" the field
				registerValue &= ~(0xFFFFFFFFL << bitLength);	// drop the bits "above" the field
			}
		}
		old_value=value;
		value=registerValue;
	}
	
	public String getInterpretation() {
		long registerValue=getValue();
		
		String key = String.valueOf(registerValue);
			
		if(interpretations.containsKey(key)) {
			return interpretations.get(key);
		} else
			return "";
	}
}
