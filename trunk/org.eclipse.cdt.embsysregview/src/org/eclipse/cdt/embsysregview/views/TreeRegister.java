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

import java.math.BigInteger;
import org.eclipse.cdt.debug.internal.core.CMemoryBlockRetrievalExtension;
import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.DebugUITools;

@SuppressWarnings("restriction")
public class TreeRegister extends TreeParent{
	private long old_value;
	private long value=-1;
	private long resetValue;
	private String type;
	private long registerAddress;
	private boolean retrievalActive = false;
	
	public boolean isReadWrite()
	{
		return (getType().toUpperCase().equals("RW") || 
				getType().toUpperCase().equals("RW1C") || 
				getType().toUpperCase().equals("RW1S"));
	}
	
	public boolean isReadOnly()
	{
		return (getType().toUpperCase().equals("RO") || 
				getType().toUpperCase().equals("RC"));
	}
	
	public boolean isWriteOnly()
	{
		return (getType().toUpperCase().equals("WO") || 
				getType().toUpperCase().equals("W1C"));
	}
	
	public boolean hasValueChanged()
	{
		return (old_value!=value);
	}
	
	public boolean isRetrievalActive() {
		return retrievalActive;
	}

	public void setRetrievalActive(boolean retrievalActive) {
		this.retrievalActive = retrievalActive;
	}

	public long getRegisterAddress() {
		return registerAddress;
	}

	public TreeRegister(String name, String description, long registerAddress, long resetValue, String type ) {
		super(name, description);
		this.registerAddress=registerAddress;
		this.resetValue=resetValue;
		this.type=type;		
	}

	public long getValue() {
		if(isWriteOnly())
			return resetValue;
		else
			return value;
	}

	public long getResetValue() {
		return resetValue;
	}

	public String getType() {
		return type;
	}
	
	/**
	 * Updates the Registers value from live data in the Debug Context
	 */
	public void updateValue()
	{
		if(retrievalActive && !isWriteOnly())
		{
			old_value=value;
			TreeElement parent = this.getParent();
			if (parent instanceof TreeRegisterGroup) {
				IAdaptable context = DebugUITools.getDebugContext();
				if(context!=null)
				{
					IMemoryBlockRetrieval retrieval = MemoryViewUtil.getMemoryBlockRetrieval(context);
					CMemoryBlockRetrievalExtension cdtRetrieval = (CMemoryBlockRetrievalExtension) retrieval;
					try {
						IMemoryBlock mem = (CMemoryBlockExtension)cdtRetrieval.getMemoryBlock(registerAddress, 4);
						MemoryByte[] membyte = ((CMemoryBlockExtension)mem).getBytesFromAddress(BigInteger.valueOf(registerAddress), 4);
					
						value = (long)(membyte[3].getValue() & 0xFF);
						value = ((long)value << 8) + (long)(membyte[2].getValue() & 0xFF);
						value = ((long)value << 8) + (long)(membyte[1].getValue() & 0xFF);
						value = ((long)value << 8) + (long)(membyte[0].getValue() & 0xFF);
											
					} catch (DebugException e) {                                                 
						value=-1;
					}
				}
				else
					value=-1;
			}
			else
				registerAddress=-1;
		}
		else
		{
			old_value=value;
			value=-1;
		}
		
		
		TreeElement[] x = getChildren();
		for (TreeElement treeElement : x)
		{
			((TreeField)treeElement).updateValue();
		}
	}
	
	public void clearValue()
	{
		value=-1;
	}

	public void toggleRetrieval() {
		retrievalActive = !retrievalActive;
	}
}
