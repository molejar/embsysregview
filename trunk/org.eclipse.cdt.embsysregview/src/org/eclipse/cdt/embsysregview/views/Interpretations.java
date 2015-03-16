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
import java.util.Collections;
import java.util.Iterator;

public class Interpretations {
	private ArrayList<Interpretation> interpretations;
	private TreeField treeField;
	
	public Interpretations() {
		interpretations = new ArrayList<Interpretation>();
	}

	public void setTreeField(TreeField treeField) {
		this.treeField = treeField;
	}


	public void addInterpretation(long value, String interpretation) {
		interpretations.add(new Interpretation(value,interpretation));
	}
	
	public String getInterpretation(long value){
		Iterator<Interpretation> it = interpretations.iterator();
		while(it.hasNext()){
			Interpretation i = it.next();
			if(i.getValue()==value)
				return i.getInterpretation();
		}
		return "";
	}
	
	public long getValue(String interpretation){
		Iterator<Interpretation> it = interpretations.iterator();
		while(it.hasNext()){
			Interpretation i = it.next();
			if(i.getInterpretation().equals(interpretation))
				return i.getValue();
		}
		return -1;
	}
	
	public String[] getInterpretations(){
		Iterator<Interpretation> it = interpretations.iterator();
		int j=1;
		String[] ret = new String[interpretations.size()+1];
		ret[0] = Utils.longtoHexString(treeField.getValue(),treeField.getBitLength());
		while(it.hasNext()){
			Interpretation i = it.next();
			ret[j]=i.getInterpretation();
			j++;
		}
		return ret;
	}
	
	public void sort() {
		Collections.sort(interpretations);
	}

	public boolean containsKey(long value) {
		Iterator<Interpretation> it = interpretations.iterator();
		
		while(it.hasNext()){
			Interpretation i = it.next();
				if(i.getValue()==value)
					return true;
		}
		return false;
	}
	
	public boolean hasInterpretations() {
		return !interpretations.isEmpty();
	}
}
