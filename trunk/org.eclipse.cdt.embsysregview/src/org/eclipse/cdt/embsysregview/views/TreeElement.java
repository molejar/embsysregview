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

public abstract class TreeElement {
	private String name;
	private String description;
	private TreeElement parent;
	
	public TreeElement(String name, String description) {
		this.name=name;
		this.description=description;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setParent(TreeElement parent) {
		this.parent = parent;
	}

	public TreeElement getParent() {
		return parent;
	}

	public String toString() {
		return getName();
	}
}
