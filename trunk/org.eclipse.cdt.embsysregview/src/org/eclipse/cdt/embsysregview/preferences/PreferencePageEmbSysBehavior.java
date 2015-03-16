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
package org.eclipse.cdt.embsysregview.preferences;

import org.eclipse.cdt.embsysregview.Activator;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PreferencePageEmbSysBehavior extends PreferencePage implements
		IWorkbenchPreferencePage {
	Button bitbuttons;
	Combo combolength;

	public PreferencePageEmbSysBehavior() {
		super();
		this.noDefaultAndApplyButton();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("how the view should behave");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		if(bitbuttons!=null && combolength != null )
		{	
			IPreferenceStore store = getPreferenceStore();
			
			store.setValue("bitbuttons", bitbuttons.getSelection());
			if(combolength.getSelectionIndex()!=-1)
				store.setValue("combolength", Integer.valueOf(combolength.getItem(combolength.getSelectionIndex())));
		}
		//return super.performOk();
		// there is no value that can be not ok
		return true;
	}
	
	private void restoreStoredSettings()
	{
		IPreferenceStore store = getPreferenceStore();
		
		boolean store_bitbuttons = store.getBoolean("bitbuttons");
		bitbuttons.setSelection(store_bitbuttons);
		
		int store_combolength = store.getInt("combolength");
		combolength.setText(String.valueOf(store_combolength));
		
		
	}

	@Override
	protected Control createContents(Composite parent) {		
		//Composite composite = new Composite(parent, SWT.NONE);
		//composite.setLayout(new RowLayout());
		
		//parent.setLayout(new RowLayout());
		
		bitbuttons = new Button(parent,SWT.CHECK);
		bitbuttons.setText("Binary column Bit Buttons immediate effect");
		bitbuttons.setToolTipText("LONG DESCR..."); // TODO: fill out ...
		bitbuttons.setSelection(false);
		//bitbuttons.setLocation(30, 30);
		
		Label combolength_label = new Label(parent,SWT.NONE);
		combolength_label.setText("Number of elements shown in drop down List (Interpretations in Hex Column)");
		combolength = new Combo(parent, SWT.DROP_DOWN);
		combolength.setVisibleItemCount(4);
		combolength.add("5");
		combolength.add("10");
		combolength.add("15");
		combolength.add("20");
		
		restoreStoredSettings();
		parent.pack();
		//return composite;
		return parent;
	}

}
