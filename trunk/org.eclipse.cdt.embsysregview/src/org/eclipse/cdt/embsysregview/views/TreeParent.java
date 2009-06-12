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
//  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

package org.eclipse.cdt.embsysregview.views;

import java.util.ArrayList;

public class TreeParent extends TreeElement {
	private ArrayList<TreeElement> children;

	public TreeParent(String name, String description) {
		super(name, description);
		children = new ArrayList<TreeElement>();
	}

	public void addChild(TreeElement child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(TreeElement child) {
		children.remove(child);
		child.setParent(null);
	}

	public TreeElement[] getChildren() {
		return (TreeElement[]) children
				.toArray(new TreeElement[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

}
