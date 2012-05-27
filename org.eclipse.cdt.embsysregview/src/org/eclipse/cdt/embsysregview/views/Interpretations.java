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
