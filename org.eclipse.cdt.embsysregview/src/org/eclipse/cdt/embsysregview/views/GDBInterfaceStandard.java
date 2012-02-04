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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataReadMemory;
import org.eclipse.cdt.debug.mi.core.command.MIDataWriteMemory;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIMemory;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;

public class GDBInterfaceStandard extends GDBInterface implements IDebugEventSetListener {
	private MISession miSession=null;
	private List<IGDBInterfaceSuspendListener> suspendListener = new ArrayList<IGDBInterfaceSuspendListener>();
	private List<IGDBInterfaceTerminateListener> terminateListener = new ArrayList<IGDBInterfaceTerminateListener>();
	
	public GDBInterfaceStandard(){
		if(!hasActiveDebugSession())
			init();
	}
	
	public void addSuspendListener(IGDBInterfaceSuspendListener listener)
	{
		suspendListener.add(listener);
	}
	
	public void addterminateListener(IGDBInterfaceTerminateListener listener)
	{
		terminateListener.add(listener);
	}
	
	public void dispose()
	{
		miSession=null;
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	private void init()
	{
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	public boolean hasActiveDebugSession()
	{
		if(miSession==null)
			return false;
		else
			return true;
	}
	
	public long readMemory(long laddress, int iByteCount)
	{
		if(!hasActiveDebugSession())
			return -1;
		
		long value=-1;
		
		CommandFactory factory = miSession.getCommandFactory();
		MIDataReadMemory mem = factory.createMIDataReadMemory( 0, Long.toString(laddress),
		                     								   MIFormat.HEXADECIMAL, iByteCount, 1, 1, null );
		try {
		   miSession.postCommand(mem);
		   MIDataReadMemoryInfo info = mem.getMIDataReadMemoryInfo();
		   if (info != null) {
			  MIMemory[] memories = info.getMemories();
			  long[] val = memories[0].getData();
			  value = val[0];
		   }
		} catch (MIException e) {
		}
		return value;
	}
	
	public int writeMemory(long laddress, long lvalue, int iByteCount)
	{
		if(!hasActiveDebugSession())
			return 0;
		
		CommandFactory factory = miSession.getCommandFactory();
		
		String value = "0x" + Long.toHexString(lvalue);
		String address = Long.toString(laddress);
		MIDataWriteMemory mw = factory.createMIDataWriteMemory(0,
			address, MIFormat.HEXADECIMAL, iByteCount, value);
		try {
			miSession.postCommand(mw);
			MIInfo info = mw.getMIInfo();
			
			if (info == null) {
				// TODO: handle ERROR ???
			}
		} catch (MIException e) {

		}
		return iByteCount;
	}
	
	/**
	 * Handle DebugEvents while an active Debug Session
	 * TODO: find a non Discouraged method to get miSession
	 */
	@Override
	public void handleDebugEvents(DebugEvent[] events)
	{
		for (DebugEvent event : events) {
			
			Object source = event.getSource();
			
			if (event.getKind() == DebugEvent.SUSPEND && source instanceof CDebugTarget) {
				System.out.println("DebugEvent " + event.toString());
				
				CDebugTarget debugTarget = (CDebugTarget) source;

				ICDISession cdiSession = debugTarget.getCDISession();
				ICDITarget[] targets = cdiSession.getTargets();

				ICDITarget cdiTarget = null;
				for (int i = 0; i < targets.length; i++) {
					ICDITarget cdiTargetCandidate = targets[i];
					if (cdiTargetCandidate instanceof Target) {
						cdiTarget = cdiTargetCandidate;
						break;
					}
				}
				if (cdiTarget != null) {
					Target miTarget = (Target) cdiTarget;
					miSession = miTarget.getMISession();
				}
				
				for(IGDBInterfaceSuspendListener listener:suspendListener)
					listener.gdbSuspendListener();
			}
			if (event.getKind() == DebugEvent.TERMINATE) {
				for(IGDBInterfaceTerminateListener listener:terminateListener)
					listener.gdbTerminateListener();
			}
		}
	}
	
}
