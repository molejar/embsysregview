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

package org.eclipse.cdt.embsysregview.preferences;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import net.miginfocom.swt.MigLayout;
import org.eclipse.cdt.embsysregview.Activator;
import java.io.File;
import java.io.IOException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.osgi.framework.Bundle;

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

public class PreferencePageEmbSys extends PreferencePage implements
		IWorkbenchPreferencePage {
	Combo architecture;
	Combo vendor;
	Combo chip;
	Combo board;
	Text descriptionText;

	public PreferencePageEmbSys() {
		super();
		this.noDefaultAndApplyButton();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A Periperal Register View for embedded system");
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
		if(chip!=null && architecture!=null && vendor!=null && chip!=null && board!=null)
		{
			if(chip.getSelectionIndex()==-1)
				return false;  
			IPreferenceStore store = getPreferenceStore();
			
			if(architecture.getSelectionIndex()!=-1)
				store.setValue("architecture", architecture.getItem(architecture.getSelectionIndex()));
			else
				store.setValue("architecture","");
			
			if(vendor.getSelectionIndex()!=-1)
				store.setValue("vendor", vendor.getItem(vendor.getSelectionIndex()));
			else
				store.setValue("vendor","");
			
			if(chip.getSelectionIndex()!=-1)
				store.setValue("chip", chip.getItem(chip.getSelectionIndex()));
			else
				store.setValue("chip","");
			
			if(board.getSelectionIndex()!=-1)
				store.setValue("board", board.getItem(board.getSelectionIndex()));
			else
				store.setValue("board","");
			
			// if arch+vendor+chip is selected then return ok ...
			return architecture.getSelectionIndex()!=-1 && vendor.getSelectionIndex()!=-1 && chip.getSelectionIndex()!=-1;
		}
		//return super.performOk();
		return true ;
	}
	
	private List<String> getDirList(String path, String pattern)
	{
		List<String> dirList = new ArrayList<String>();
		
		Enumeration<URL> entries = Platform.getBundle("org.eclipse.cdt.embsysregview").findEntries(path, pattern, false);

		if (entries != null) {
			while (entries.hasMoreElements()) {
				URL entry = entries.nextElement();
				
				File x = new File(entry.getFile());
				try {
					String filename = x.getCanonicalFile().getName();
					if (!filename.startsWith(".")) {
						dirList.add(filename);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return dirList;
	}
	
	private void restoreStoredSettings()
	{
		// Try to fill out Combos with values from the Store	
		IPreferenceStore store = getPreferenceStore();
		String store_architecture = store.getString("architecture");
		String store_vendor = store.getString("vendor");
		String store_chip = store.getString("chip");
		String store_board = store.getString("board");
		
		int index = architecture.indexOf(store_architecture);
		if(index!=-1)
		{
			architecture.select(index);
			fillVendor(store_architecture);
			vendor.setEnabled(true);
			index = vendor.indexOf(store_vendor);
			if(index!=-1)
			{
				vendor.select(index);
				fillChip(store_architecture, store_vendor);
				chip.setEnabled(true);
				index = chip.indexOf(store_chip);
				if(index!=-1)
				{
					chip.select(index);
					fillBoard(store_architecture, store_vendor, store_chip);					
					index = board.indexOf(store_board);
					if(index!=-1)
						board.select(index);
				}				
			}
			else
				vendor.setText("");
		}
		else
			architecture.setText("");
	}
	
	private void fillArchitecture ()
	{
		for(String entry:getDirList("data", "*"))
			architecture.add(entry);

		restoreStoredSettings();
	}
	
	private void fillVendor(String selectedArchitecture)
	{
		chip.setEnabled(false);
		board.setEnabled(false);
		vendor.setEnabled(true);
		chip.setText("");
		board.setText("");
		vendor.removeAll();
		chip.removeAll();
		board.removeAll();
		
		for(String entry:getDirList("data/" + selectedArchitecture, "*"))
			vendor.add(entry);

		vendor.setText("");
		descriptionText.setText("");
	}
	
	private void fillChip(String selectedArchitecture, String selectedVendor)
	{
		board.setEnabled(false);
		board.setText("");
		chip.setEnabled(true);
		chip.removeAll();
		board.removeAll();

		for(String entry:getDirList("data/"	+ selectedArchitecture +"/"+ selectedVendor, "*.xml"))
			chip.add(entry.substring(0, entry.length()-4));
		
		chip.setText("");
		descriptionText.setText("");
	}
	
	@SuppressWarnings("unchecked")
	private void fillBoard(String selectedArchitecture, String selectedVendor, String selectedChip)
	{
		board.removeAll();
		board.add("---  none ---");
		// Check if boards are listed ...
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		Bundle bundle = Platform.getBundle("org.eclipse.cdt.embsysregview");
		URL fileURL = bundle.getEntry("data/"	+ selectedArchitecture +"/"+ selectedVendor+"/"+selectedChip+".xml");
		Document doc;
		boolean containsBoards=false;
		try {
			doc = builder.build(fileURL);
			Element root = doc.getRootElement();
			if(root.getName() != "device")
			{
				List<Element> grouplist = root.getChildren("boards");
				for(Element group:grouplist)
				{
					List<Element> boardlist = group.getChildren();
					for(Element boardElement:boardlist)
					{
						containsBoards=true;
						Attribute attr_bname = boardElement.getAttribute("id");
						String bname;
						if( attr_bname != null )
							bname = attr_bname.getValue();
						else
							bname = "-1";
						board.add(bname);	
						
					}
				}
				
				Element chip_description = root.getChild("chip_description");
				if (chip_description!=null)
					descriptionText.setText(chip_description.getText());
			} else {
				Element chip_description = root.getChild("description");
				if (chip_description!=null)
					descriptionText.setText(chip_description.getText());
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// enable only if there are boards to show
		if (containsBoards)
			board.setEnabled(true);
		else
			board.setEnabled(false);
		
		if(board.getItemCount()>0)
			board.setText(board.getItem(0));
		else
			board.setText("");
	}

	@Override
	protected Control createContents(Composite parent) {		
		Composite composite = new Composite(parent, SWT.NONE);
		Composite left = new Composite(composite, SWT.NONE);
		Composite right = new Composite(composite, SWT.NONE);
		
		MigLayout migLayout = new MigLayout("fill","[180,grow 0][fill,grow]","top");
		composite.setLayout(migLayout);
		
		left.setLayoutData("width 100:180:180");
		left.setLayout(new GridLayout(1, false));
		
		right.setLayoutData("grow,hmin 0,wmin 0");
		right.setLayout(new FillLayout());

		Label architectureLabel = new Label(left, SWT.LEFT);
		architectureLabel.setText("Architecture:");
		architecture = new Combo(left, SWT.DROP_DOWN|SWT.READ_ONLY);		
		architecture.setVisibleItemCount(10);
		architecture.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true,false));
		
		Label vendorLabel = new Label(left, SWT.LEFT);
		vendorLabel.setText("Vendor:");
		vendor = new Combo(left, SWT.DROP_DOWN|SWT.READ_ONLY);
		vendor.setVisibleItemCount(10);
		vendor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true,false));
		
		Label chipLabel = new Label(left, SWT.LEFT);
		chipLabel.setText("Chip:");
		chip = new Combo(left, SWT.DROP_DOWN|SWT.READ_ONLY);
		chip.setVisibleItemCount(20);
		chip.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true,false));
		
		Label boardLabel = new Label(left, SWT.LEFT);
		boardLabel.setText("Board:");
		board = new Combo(left, SWT.DROP_DOWN|SWT.READ_ONLY);
		board.setVisibleItemCount(10);
		board.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true,false));

		Group descriptionGroup = new Group(right, SWT.NONE);
		descriptionGroup.setText("Chip description");
		descriptionGroup.setLayout(new MigLayout("fill"));
		
		descriptionText = new Text(descriptionGroup,SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);		
		descriptionText.setLayoutData("height 100%,width 100%,hmin 0,wmin 400");
		descriptionText.setText("");
		FontData[] fD = descriptionText.getFont().getFontData();
		fD[0].setName("Lucida Console");
		Font f = new Font(Display.getCurrent(), fD[0]);
		descriptionText.setFont(f);
		

		architecture.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				fillVendor(architecture.getItem(architecture.getSelectionIndex()));
			}
		});

		vendor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				fillChip(architecture.getItem(architecture.getSelectionIndex()), vendor.getItem(vendor.getSelectionIndex()));
			}
		});

		chip.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				fillBoard(architecture.getItem(architecture.getSelectionIndex()), vendor.getItem(vendor.getSelectionIndex()), chip.getItem(chip.getSelectionIndex()));
			}
		});
		
		architecture.setSize(500, 30);	

		vendor.setSize(500, 30);
		vendor.setEnabled(false);

		chip.setSize(500, 30);
		chip.setEnabled(false);

		board.setSize(500, 30);
		board.setEnabled(false);

		fillArchitecture();		
		parent.pack();
		return composite;
	}

}
