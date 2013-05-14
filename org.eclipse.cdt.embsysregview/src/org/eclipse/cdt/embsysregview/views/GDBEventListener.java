package org.eclipse.cdt.embsysregview.views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;

public class GDBEventListener extends GDBInterface implements IDebugEventSetListener {
	private Object currentSession=null;
	private GDBEventProvider provider=null;
	private List<IGDBInterfaceSuspendListener> suspendListener = new ArrayList<IGDBInterfaceSuspendListener>();
	private List<IGDBInterfaceTerminateListener> terminateListener = new ArrayList<IGDBInterfaceTerminateListener>();
	
	public GDBEventListener(){
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
		currentSession=null;
		DebugPlugin.getDefault().removeDebugEventListener(this);
		provider.dispose();
		provider=null;
	}
	
	private void init()
	{
		DebugPlugin.getDefault().addDebugEventListener(this);
		provider = new GDBEventProvider();
	}
	
	public boolean hasActiveDebugSession()
	{
		if(currentSession==null)
			return false;
		else
			return true;
	}
	
	public long readMemory(long laddress, int iByteCount)
	{
		try {
			return GDBSessionTranslator.readMemory(currentSession, laddress, iByteCount);
		} catch (TimeoutException e) {
			System.out.println(e);
		}
		return -1;
	}
	
	public int writeMemory(long laddress, long lvalue, int iByteCount)
	{
		try {
			return GDBSessionTranslator.writeMemory(currentSession, laddress, lvalue, iByteCount);
		} catch (TimeoutException e) {
			System.out.println(e);
		}
		return -1;
	}
	
	/**
	 * Handle DebugEvents
	 */
	@Override
	public void handleDebugEvents(DebugEvent[] events)
	{
		for (DebugEvent event : events) {
			
			Object source = event.getSource();
			Object sourceSession = null;
			Object currentSession = GDBSessionTranslator.getSession();
			if(source instanceof GDBEventProvider.DebugContextChangedDebugEvent){
				sourceSession = ((GDBEventProvider.DebugContextChangedDebugEvent)source).getSession();
			}else if(source instanceof GDBEventProvider.DsfSessionDebugEvent){
				sourceSession = ((GDBEventProvider.DsfSessionDebugEvent)source).getSession();
			}else{
				sourceSession = GDBSessionTranslator.getSession(source);
			}
			if(currentSession != this.currentSession){ //Session has changed;
				this.currentSession = currentSession;
				if(this.currentSession != null){ //new session is active and known (Standard/DSF)
					for(IGDBInterfaceSuspendListener listener:suspendListener)
						listener.gdbSuspendListener();
				}
				else{ //new session is terminated or unknown
					for(IGDBInterfaceTerminateListener listener:terminateListener) 
						listener.gdbTerminateListener();
				}
			}
			//source session should always be not null and the same as current (do not handle events not from current selected session!)
			if(sourceSession == null || sourceSession != currentSession) 
				return;
			
			if (event.getKind() == DebugEvent.SUSPEND) {
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
