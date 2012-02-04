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

// could currently be turned into an Interface
public abstract class GDBInterface {
	public abstract boolean hasActiveDebugSession();
	public abstract long readMemory(long laddress, int iByteCount);
	public abstract int writeMemory(long laddress, long lvalue, int iByteCount);
	public abstract void dispose();
	public abstract void addSuspendListener(IGDBInterfaceSuspendListener listener);
	public abstract void addterminateListener(IGDBInterfaceTerminateListener listener);
}
