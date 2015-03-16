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
