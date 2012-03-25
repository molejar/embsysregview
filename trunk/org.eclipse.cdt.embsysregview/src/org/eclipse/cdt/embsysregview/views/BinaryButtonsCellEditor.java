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

import org.eclipse.cdt.embsysregview.Activator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class BinaryButtonsCellEditor extends CellEditor {

	Composite composite;
	TreeElement element;
	TreeViewer viewer = null;
	boolean store_bitbuttons;
	private Button[] b;

	public BinaryButtonsCellEditor(Composite parent) {
		super(parent);
	}
	
	public BinaryButtonsCellEditor(Composite parent, TreeViewer viewer) {
		super(parent);
		this.viewer=viewer;
	}
	
	private void updateViewer(final Object Element) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(Element==null)
					viewer.refresh();
				else
					viewer.refresh(Element);
			}
		});
	}

	@Override
	protected Control createControl(Composite parent) {

		RowLayout layout = new RowLayout();
		layout.wrap = false;
		layout.pack = true;
		layout.justify = false;
		layout.type = SWT.HORIZONTAL;
		layout.marginLeft = -1;
		layout.marginTop = -1;
		layout.marginRight = -1;
		layout.marginBottom = -1;
		layout.spacing = -1;

		// Initialize the components in a Composite container.
		composite = new Composite(parent, SWT.NONE);
		composite.setBounds(0, 0, parent.getSize().x, parent.getSize().y);
		// Set the layout for the editor.
		composite.setLayout(layout);
		return composite;
	}

	@Override
	protected Object doGetValue() {
		// TODO: ???
		return 0xAFFE;
	}

	@Override
	protected void doSetFocus() {
		if (composite != null) {
			composite.setFocus();
		}
	}
	
	protected void updateBinaryValue(Object Element, boolean updateView)
	{
		long value = 0;
		
		for (int i = b.length-1; i >= 0; i--)
			value = (value << 1) + Integer.valueOf(b[i].getText());

		if (Element instanceof TreeRegister)
			((TreeRegister) Element).setAndWriteValue(value);
		if (Element instanceof TreeField)
		{
			((TreeField) Element).setValue(value);
		}
		
		if(updateView==true)
			updateViewer(Element);
	}

	@Override
	protected void doSetValue(Object value) {
		int bitsize = 0;
		long regvalue = -1;
		element = (TreeElement)value;
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store_bitbuttons = store.getBoolean("bitbuttons");

		if (element instanceof TreeRegister) {
			bitsize = ((TreeRegister) element).getBitSize();
			regvalue = ((TreeRegister) element).getValue();
		}

		if (element instanceof TreeField) {
			bitsize = ((TreeField) element).getBitLength();
			regvalue = ((TreeField) element).getValue();
		}

		Font f;
		if (regvalue != -1)
		{
			b = new Button[bitsize];

			for (int i = bitsize-1; i >=0 ; i--) {
				b[i] = new Button(composite, SWT.FLAT);
				RowData data = new RowData();
			    data.width = 15;
			    data.height = 19;
			    b[i].setLayoutData(data);
				FontData[] fD = b[i].getFont().getFontData();
				fD[0].setHeight(7);
				f = new Font(Display.getCurrent(), fD[0]);
				b[i].setFont(f);

				b[i].addMouseListener(new MouseListener() {

					@Override
					public void mouseUp(MouseEvent e) {}

					@Override
					public void mouseDown(MouseEvent e) {
						Button but = ((Button) e.getSource());
						if (but.getText().equals("1"))
							but.setText("0");
						else
							but.setText("1");
						if (store_bitbuttons==true)
							updateBinaryValue(element,false);
					}

					@Override
					public void mouseDoubleClick(MouseEvent e) {}
				});

				b[i].setText("-");
				if (element instanceof TreeRegister)
					b[i].setText(String.valueOf(org.eclipse.cdt.embsysregview.views.Utils
							.getBitFromValue(i,
									((TreeRegister) element).getValue())));
				if (element instanceof TreeField)
					b[i].setText(String
							.valueOf(org.eclipse.cdt.embsysregview.views.Utils
									.getBitFromValue(i,
											((TreeField) element).getValue())));
			}

			
			
			// show set button only if immediate effect is disabled
			if(store_bitbuttons==false)
			{
				Button setButton = new Button(composite, SWT.NONE);
				setButton.setText("Set");
				RowData data = new RowData();
			    data.width = 26;
			    data.height = 19;
			    setButton.setLayoutData(data);
				setButton.addMouseListener(new MouseListener() {
	
					@Override
					public void mouseUp(MouseEvent e) {}
	
					@Override
					public void mouseDown(MouseEvent e) {
						updateBinaryValue(element, true);
					}
	
					@Override
					public void mouseDoubleClick(MouseEvent e) {}
				});
			}
		}
	}
}
