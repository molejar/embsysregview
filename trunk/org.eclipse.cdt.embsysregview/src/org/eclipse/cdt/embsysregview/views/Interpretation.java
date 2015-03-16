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

public class Interpretation implements Comparable<Interpretation> {
	long value;
	public long getValue() {
		return value;
	}

	public String getInterpretation() {
		return interpretation;
	}

	String interpretation;
	
	public Interpretation(long value, String interpretation) {
		this.interpretation=interpretation;
		this.value=value;
	}

	@Override
	public int compareTo(Interpretation interpretation) {
		if(this.value ==  interpretation.getValue())
			return 0;
		if(this.value >  interpretation.getValue())
			return -1;
		if(this.value <  interpretation.getValue())
			return 1;
		
		return 0;
	}
}
