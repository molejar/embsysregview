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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataReadMemory;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIMemory;

public class TreeRegister extends TreeParent{
	private long old_value;
	private long value=-1;
	private long resetValue;
	private String type;
	private long registerAddress;
	private boolean retrievalActive = false;
	private int size=4;
	
	public boolean isReadWrite()
	{
		return (getType().toUpperCase().equals("RW") || 
				getType().toUpperCase().equals("RW1") || 
				getType().toUpperCase().equals("RW1C") || 
				getType().toUpperCase().equals("RW1S") || 
				getType().toUpperCase().equals("RWH"));
	}
	
	public boolean isReadOnly()
	{
		return (getType().toUpperCase().equals("RO") || 
				getType().toUpperCase().equals("RC") ||
				getType().toUpperCase().equals("R"));
	}
	
	public boolean isWriteOnly()
	{
		return (getType().toUpperCase().equals("WO") || 
				getType().toUpperCase().equals("W") || 
				getType().toUpperCase().equals("W1C") || 
				getType().toUpperCase().equals("W1S") || 
				getType().toUpperCase().equals("W1"));
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

	public TreeRegister(String name, String description, long registerAddress, long resetValue, String type, int size ) {
		super(name, description);
		this.registerAddress=registerAddress;
		this.resetValue=resetValue;
		this.type=type;		
		this.size=size;
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
	public void updateValue( MISession miSession )
	{
		if(retrievalActive && !isWriteOnly())
		{
			old_value=value;
			TreeElement parent = this.getParent();
			if (parent instanceof TreeRegisterGroup) {
				IAdaptable context = DebugUITools.getDebugContext();
				if(context!=null)
				{
					if ( miSession != null ) {
						CommandFactory factory = miSession.getCommandFactory();
						MIDataReadMemory mem = factory.createMIDataReadMemory( 0, Long.toString(registerAddress),
						                     								   MIFormat.HEXADECIMAL, getByteSize(), 1, 1, null );
						value = -1;
						try {
						   miSession.postCommand(mem);
						   MIDataReadMemoryInfo info = mem.getMIDataReadMemoryInfo();
						   if (info != null) {
							  MIMemory[] memories = info.getMemories();
							  long[] val = memories[0].getData();
							  value = val[0];
						   }
						} catch (MIException e) {
							//throw new MI2CDIException(e);
						}
					} else {
						value = -1;
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
	
	public int getBitSize() 
	{
		return size*8;
	}
	
	public int getByteSize() 
	{
		return size;
	}

	public void clearValue()
	{
		value=-1;
	}

	public void toggleRetrieval() {
		retrievalActive = !retrievalActive;
	}
}
