/*******************************************************************************
 * Copyright (c) 2015 EmbSysRegView
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ravenclaw78 - initial API and implementation
 *******************************************************************************/
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
