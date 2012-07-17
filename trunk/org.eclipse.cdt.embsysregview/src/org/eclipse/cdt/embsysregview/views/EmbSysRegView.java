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



import java.net.URL;
import java.text.ParseException;
import net.miginfocom.swt.MigLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.cdt.embsysregview.Activator;
import org.eclipse.cdt.embsysregview.parser.RegisterXMLParser;
import org.eclipse.cdt.embsysregview.preferences.PreferencePageEmbSys;
import org.eclipse.cdt.embsysregview.preferences.PreferencePageEmbSysBehavior;
import org.eclipse.core.runtime.Platform;
import org.jdom.JDOMException;
import org.osgi.framework.Bundle;
import org.eclipse.cdt.embsysregview.views.Utils;

/**
 * Generates a View, displaying Registers for an Embedded Device
 */

public class EmbSysRegView extends ViewPart implements IGDBInterfaceSuspendListener,IGDBInterfaceTerminateListener {
	protected TreeViewer viewer;
	private TreeParent invisibleRoot;
	private Label infoLabel;
	private Button configButton;
	private Composite header;
	private Action doubleClickAction;
	private Image selectedImage, unselectedImage, selectedFieldImage, unselectedFieldImage, infoImage, interpretationImage, configButtonImage;
	static public GDBInterface GDBi;
	private TreeElement currentEditedElement = null;
	
	/**
	 * This is the Content Provider that present the Static Model to the
	 * TreeViewer
	 */
	class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		public ViewContentProvider() {
			initialize();
			
			Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener(){

				@Override
				public void propertyChange(PropertyChangeEvent event) {
					// only rebuild tree on chip/board change
					if(event.getProperty().equals("architecture") || event.getProperty().equals("vendor") || event.getProperty().equals("chip") || event.getProperty().equals("board"))
					{
						initialize();
						viewer.setInput(invisibleRoot);
						viewer.refresh();
						updateInfoLabel();
					}
				}});
		}
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
					initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeElement) {
				return ((TreeElement) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent) parent).hasChildren();
			return false;
		}

		private void initialize() {
			// initialize invisibleRoot with an empty TreeParent to show an empty Tree if LoadXML fails
			invisibleRoot = new TreeParent("", "");
			try {
				invisibleRoot = new RegisterXMLParser().LoadXML();
				
			} catch (JDOMException e) { 
			} catch (ParseException e) {
			}
		}
	}

	/**
	 * The constructor.
	 */
	public EmbSysRegView() {
		GDBi = new GDBEventListener();
		GDBi.addSuspendListener(this);
		GDBi.addterminateListener(this);
	}

	private void updateInfoLabel()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String store_architecture = store.getString("architecture");
		String store_vendor = store.getString("vendor");
		String store_chip = store.getString("chip");
		String store_board = store.getString("board");
		
		if(invisibleRoot == null || !invisibleRoot.hasChildren())
			infoLabel.setText("ERROR: Please select a chip using the preference page (c++/Debug/EmbSys Register View)");
		else
			if(store_board=="")
				infoLabel.setText("Arch: "+store_architecture+"  Vendor: "+store_vendor+"  Chip: "+store_chip);
			else
				infoLabel.setText("Arch: "+store_architecture+"  Vendor: "+store_vendor+"  Chip: "+store_chip+"  Board: "+store_board);
	}
	
	/**
	 * This is a callback that creates the viewer and initialize it.
	 */
	public void createPartControl(final Composite parent) {
		Bundle bundle = Platform.getBundle("org.eclipse.cdt.embsysregview");
		URL fileURL = bundle.getEntry("icons/selected_register.png");
		URL fileURL2 = bundle.getEntry("icons/unselected_register.png");
		URL fileURL3 = bundle.getEntry("icons/selected_field.png");
		URL fileURL4 = bundle.getEntry("icons/unselected_field.png");
		URL fileURL5 = bundle.getEntry("icons/info.png");
		URL fileURL6 = bundle.getEntry("icons/interpretation.png");
		URL fileURL7 = bundle.getEntry("icons/config.png");
		
		try {
			selectedImage = new Image(parent.getDisplay(),fileURL.openStream());
			unselectedImage = new Image(parent.getDisplay(),fileURL2.openStream());
			selectedFieldImage = new Image(parent.getDisplay(),fileURL3.openStream());
			unselectedFieldImage = new Image(parent.getDisplay(),fileURL4.openStream());
			infoImage = new Image(parent.getDisplay(),fileURL5.openStream());
			interpretationImage = new Image(parent.getDisplay(),fileURL6.openStream());
			configButtonImage = new Image(parent.getDisplay(),fileURL7.openStream());
		} catch (Exception e) {
			selectedImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
			unselectedImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
			selectedFieldImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
			unselectedFieldImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
			infoImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
			interpretationImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
			configButtonImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
		}

		TreeViewerColumn column;
		parent.setLayout(new MigLayout("fill","",""));
		
		header = new Composite(parent, SWT.NONE);
		/*header.setLayout(new MigLayout("fill","",""));*/
		header.setLayoutData("dock north,height 16px,width 100%,wmin 0,hmin 16,gap 0 0 -5 0");
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
	    
	    rowLayout.marginLeft=-1;
	    rowLayout.marginRight=0;
	    rowLayout.marginTop=-1;
	    rowLayout.marginBottom=0;
	    rowLayout.fill=true;
	    rowLayout.wrap=false;
	    rowLayout.spacing=5;
		header.setLayout(rowLayout);
		
		configButton = new Button(header,SWT.FLAT);
		configButton.setImage(configButtonImage);
		configButton.setSize(17, 17);
		RowData data = new RowData();
	    data.width = 17;
	    data.height = 17;
	    configButton.setLayoutData(data);
	    configButton.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {}
			
			@Override
			public void mouseDown(MouseEvent e) {
				IPreferencePage page = new PreferencePageEmbSys();
				page.setTitle("EmbSysRegView");
				IPreferencePage page2 = new PreferencePageEmbSysBehavior();
				page2.setTitle("Behavior");
				PreferenceManager mgr = new PreferenceManager();
				IPreferenceNode node = new PreferenceNode("1", page);
				node.add(new PreferenceNode("2",page2));
				mgr.addToRoot(node);
				PreferenceDialog dialog = new PreferenceDialog(PlatformUI.getWorkbench().
		                getActiveWorkbenchWindow().getShell(), mgr);
				dialog.create();
				dialog.setMessage(page.getTitle());
				dialog.open();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});
	    
	    infoLabel = new Label(header,SWT.NONE);
	    	    
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getControl().setLayoutData("height 100%,width 100%,hmin 0,wmin 0");
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		
		
		// Registername
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(250);
		column.getColumn().setMoveable(false);
		column.getColumn().setText("Register");
		column.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public Color getForeground(Object element) {
				if (element instanceof TreeRegister)
					if (((TreeRegister)element).isRetrievalActive())	
						return parent.getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
				if (element instanceof TreeField)
					if (((TreeRegister)((TreeField)element).getParent()).isRetrievalActive())	
						return parent.getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
				
				return null;
			}

			public String getText(Object element) {
				if (element instanceof TreeField)
				{
					int bitOffset = ((TreeField) element).getBitOffset();
					int bitLength = ((TreeField) element).getBitLength();
					
					if (bitLength == 1)
						return element.toString()
								+ " (bit "
								+ String.valueOf(bitOffset) + ")";
					else						
						return element.toString()
								+ " (bits "
								+ String.valueOf(bitOffset)
								+ "-"
								+ String.valueOf(bitOffset + bitLength - 1) + ")";
				}
				else
					return element.toString();
			}

			public Image getImage(Object obj) {
				if (obj instanceof TreeParent)
				{
					if (obj instanceof TreeRegister)
						if (((TreeRegister)obj).isRetrievalActive())
							return selectedImage;
						else
							return unselectedImage;					
				}
				if (obj instanceof TreeField)
				{
					if (((TreeRegister)((TreeField)obj).getParent()).isRetrievalActive())
						return selectedFieldImage;
					else
						return unselectedFieldImage;
				}
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}

		});

		// Hex Value
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(80);
		column.getColumn().setMoveable(false);
		column.getColumn().setText("Hex");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Color getForeground(Object element) {
				if (element instanceof TreeRegister)
					if (!((TreeRegister) element).isWriteOnly())
						if (((TreeRegister)element).hasValueChanged())	
							return parent.getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
				if (element instanceof TreeField)
					if (!((TreeRegister) ((TreeField)element).getParent()).isWriteOnly())
						if (((TreeField)element).hasValueChanged())	
							return parent.getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
				return null;
			}
			
			public String getText(Object element) {
				if (element instanceof TreeGroup)
					return "";
				if (element instanceof TreeRegisterGroup)
					return "";
				if (element instanceof TreeRegister)
					if (((TreeRegister) element).getValue() == -1)
						return "";
					else
						if (((TreeRegister) element).isWriteOnly())
							return "- write only -";
						else
							return Utils.longtoHexString(((TreeRegister) element)
								.getValue(), ((TreeRegister) element).getBitSize());
				if (element instanceof TreeField)
					if (((TreeRegister) ((TreeField) element).getParent())
							.getValue() == -1)
						return "";
					else
						if (((TreeRegister) ((TreeField)element).getParent()).isWriteOnly())
							return "- write only -";
						else
							return Utils.longtoHexString(((TreeField) element)
								.getValue(), ((TreeField) element)
								.getBitLength());
				else
					return element.toString();
			}

		});
		
		final TextCellEditor textCellEditor = new TextCellEditor(viewer.getTree());
		textCellEditor.setValidator(new HexCellEditorValidator(viewer));
		final ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(viewer.getTree(), new String[0], SWT.NONE);
		
		
		comboBoxCellEditor.setValidator(new HexCellEditorValidator(viewer));
		
		((CCombo)comboBoxCellEditor.getControl()).addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = ((CCombo)comboBoxCellEditor.getControl()).getSelectionIndex();
				
				TreeElement obj = currentEditedElement;
				if(obj instanceof TreeField)
				{
					long value=-1;
					
					if(selectionIndex!=-1)
					{
						value = ((TreeField)obj).getInterpretations().getValue(((CCombo)comboBoxCellEditor.getControl()).getItem(selectionIndex));
					}
					else
					{	
						String svalue= ((CCombo)comboBoxCellEditor.getControl()).getText();
						if (svalue.startsWith("0x"))
							value=Long.valueOf(svalue.substring(2, svalue.length()), 16);
						
					}
					if (value!=-1)
						((TreeField)obj).setValue(value);
					
				}
				
				if (((TreeRegister)obj.getParent()).isWriteOnly())
					updateTreeFields(invisibleRoot);
				viewer.refresh();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		comboBoxCellEditor.addListener(new ICellEditorListener(){

			@Override
			public void applyEditorValue() {
				int selectionIndex = ((CCombo)comboBoxCellEditor.getControl()).getSelectionIndex();
								
				TreeElement obj = currentEditedElement;
				if(obj instanceof TreeField)
				{
					long value=-1;
					
					if(selectionIndex!=-1)
					{
						value = ((TreeField)obj).getInterpretations().getValue(((CCombo)comboBoxCellEditor.getControl()).getItem(selectionIndex));
					}
					else
					{	
						String svalue= ((CCombo)comboBoxCellEditor.getControl()).getText();
						if (svalue.startsWith("0x"))
							value=Long.valueOf(svalue.substring(2, svalue.length()), 16);
						
					}
					if (value!=-1)
						((TreeField)obj).setValue(value);
					
				}
					
				if (((TreeRegister)obj.getParent()).isWriteOnly())
					updateTreeFields(invisibleRoot);
				viewer.refresh();
			}

			@Override
			public void cancelEditor() {}

			@Override
			public void editorValueChanged(boolean oldValidState,
					boolean newValidState) {}
		});
		
		column.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof TreeField && 
					((((TreeRegister)((TreeField)element).getParent()).isReadWrite()) || 
					 (((TreeRegister)((TreeField)element).getParent()).isWriteOnly())))
					return true;
				if (element instanceof TreeRegister && 
						(((TreeRegister)element).isReadWrite() || 
						 ((TreeRegister)element).isWriteOnly()))
					return true;
				return false;
			}

			
			@Override
			protected CellEditor getCellEditor(Object element) {
				if (element instanceof TreeField && ((TreeField)element).hasInterpretations())
				{
					comboBoxCellEditor.setItems(((TreeField)element).getInterpretations().getInterpretations());
					IPreferenceStore store = Activator.getDefault().getPreferenceStore();
					int store_combolength = store.getInt("combolength");
					if(store_combolength>0)
						((CCombo)comboBoxCellEditor.getControl()).setVisibleItemCount(store_combolength);
					currentEditedElement = (TreeElement)element;
					return comboBoxCellEditor;
				}
				else	
					return textCellEditor;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof TreeField && ((TreeField)element).hasInterpretations())
				{
					// TODO: check if the Integer is for index in the combobox
					return new Integer((int) ((TreeField)element).getValue()); // TODO: what to do on large bitfield ?
				}
				else
				{
				if (element instanceof TreeField && ((TreeField)element).getValue()!=-1)
					return Utils.longtoHexString(((TreeField) element).getValue(),((TreeField) element).getBitLength()); 
					
				if (element instanceof TreeRegister && ((TreeRegister)element).getValue()!=-1)
					return Utils.longtoHexString(((TreeRegister) element).getValue(), ((TreeRegister) element).getBitSize()); 
				}  
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (value == null)
					return;
				if (value instanceof String) {
					if (element instanceof TreeRegister && ((String)value).startsWith("0x"))
					{
						String svalue=((String)value);
						long lvalue=Long.valueOf(svalue.substring(2, svalue.length()), 16);
						
						TreeRegister treeRegister = ((TreeRegister) element);
						if(treeRegister.getValue()!=-1 && treeRegister.getValue()!=lvalue)
						{
							// Update Value on device
							treeRegister.setAndWriteValue(lvalue);
							if (((TreeRegister)element).isWriteOnly())
								updateTreeFields(invisibleRoot);
							viewer.refresh();
						}
					}
					
					if (element instanceof TreeField && ((String)value).startsWith("0x"))
					{
						String svalue=((String)value);
						long fvalue=Long.valueOf(svalue.substring(2, svalue.length()), 16);
						
						TreeField treeField = ((TreeField) element);
						if(treeField.getValue()!=-1 && treeField.getValue()!=fvalue)
						{
							TreeRegister treeRegister = ((TreeRegister)treeField.getParent());
												
							// calculate register value + modified field to write into register
							long rvalue=treeRegister.getValue();
							int bitLength = treeField.getBitLength();
							int bitOffset = treeField.getBitOffset();
							long mask;
							
							mask = (0xFFFFFFFFL >> (32 - bitLength)) << bitOffset;
							rvalue = rvalue & (~mask) ; // clear field bits in register value
							fvalue = fvalue << bitOffset; // shift field value into its position in the register
							fvalue = fvalue & mask ; // just to be sure, cut everything but the field
							rvalue = rvalue | fvalue ; // blend the field value into the register value
							
							// Update Value in Target
							treeRegister.setAndWriteValue(rvalue);
							if ( ((TreeRegister)(((TreeField) element).getParent())).isWriteOnly() )
								updateTreeFields(invisibleRoot);
							viewer.refresh(treeRegister);
						}	
					}
				}
				//TODO: why...what for...
				//viewer.refresh(element);
			}
		}); 
		
		// Binary Value
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(false);
		column.getColumn().setText("Bin");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Color getForeground(Object element) {
				if (element instanceof TreeRegister)
					if (!((TreeRegister) element).isWriteOnly())
						if (((TreeRegister)element).hasValueChanged())	
							return parent.getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
				if (element instanceof TreeField)
					if (!((TreeRegister) ((TreeField)element).getParent()).isWriteOnly())
						if (((TreeField)element).hasValueChanged())	
							return parent.getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
				
				return null;
			}
			
			public String getText(Object element) {
				if (element instanceof TreeGroup)
					return "";
				if (element instanceof TreeRegisterGroup)
					return "";
				if (element instanceof TreeRegister)
					if (((TreeRegister) element).getValue() == -1)
						return "";
					else
						if (((TreeRegister) element).isWriteOnly())
							return "------------- write only -------------";
						else
							return Utils.longtobinarystring(((TreeRegister) element)
								.getValue(), ((TreeRegister) element).getBitSize());
				if (element instanceof TreeField)
					if (((TreeRegister) ((TreeField) element).getParent())
							.getValue() == -1)
						return "";
					else
						if (((TreeRegister) ((TreeField)element).getParent()).isWriteOnly())
							return "------------- write only -------------";
						else
							return Utils.longtobinarystring(((TreeField) element)
								.getValue(), ((TreeField) element)
								.getBitLength());
				else
					return element.toString();
			}

		});
		

		column.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof TreeField && 
					((((TreeRegister)((TreeField)element).getParent()).isReadWrite()) || 
					 (((TreeRegister)((TreeField)element).getParent()).isWriteOnly())))
					return true;
				if (element instanceof TreeRegister && 
						(((TreeRegister)element).isReadWrite() || 
						 ((TreeRegister)element).isWriteOnly()))
					return true;
				return false;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new BinaryButtonsCellEditor(viewer.getTree(),viewer);
			}

			@Override
			protected Object getValue(Object element) {
				return element;
			}

			@Override
			protected void setValue(Object element, Object value) {
				viewer.refresh(element);
			}
		}); 
		
		// Register Reset Value (hex)
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(80);
		column.getColumn().setMoveable(false);
		column.getColumn().setText("Reset");
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof TreeGroup)
					return "";
				if (element instanceof TreeRegisterGroup)
					return "";
				if (element instanceof TreeRegister)
					return Utils.longtoHexString(((TreeRegister) element).getResetValue()
								, ((TreeRegister) element).getBitSize());
				if (element instanceof TreeField)
					return "";
				else
					return "";
			}

		});

		// Register Access
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setAlignment(SWT.CENTER);
		column.getColumn().setWidth(50);
		column.getColumn().setMoveable(false);
		column.getColumn().setText("Access");
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof TreeGroup)
					return "";
				if (element instanceof TreeRegisterGroup)
					return "";
				if (element instanceof TreeRegister)
					return ((TreeRegister) element).getType().toUpperCase();
				if (element instanceof TreeField)
					return "";
				else
					return "";
			}

		});

		
		// Register Address (hex)
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(80);
		column.getColumn().setMoveable(false);
		column.getColumn().setText("Address");
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof TreeGroup)
					return "";
				if (element instanceof TreeRegisterGroup)
					return "";
				if (element instanceof TreeRegister)
					return Utils.longtoHexString(((TreeRegister) element).getRegisterAddress()
								, 32); //TODO: get address width from xml ...
				if (element instanceof TreeField)
					return "";
				else
					return "";
			}

		});

		// Description
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(300);
		column.getColumn().setMoveable(false);
		column.getColumn().setText("Description");
		ColumnViewerToolTipSupport.enableFor(viewer);

		column.setLabelProvider(new CellLabelProvider() {

			public String getToolTipText(Object element) {
				if (element instanceof TreeRegister)
				{ // only display if more than one line is found
					if(org.eclipse.cdt.embsysregview.views.Utils.countTextLines(((TreeRegister) element).getDescription())>1)
						return ((TreeRegister) element).getDescription();
					else
						return null;
				}
				if (element instanceof TreeField)
				{	// display tooltip if more than one line is found, or an interpretation is shown instead of this one line
					if(org.eclipse.cdt.embsysregview.views.Utils.countTextLines(((TreeField) element).getDescription())>1 || ((TreeField) element).hasInterpretation())
						return ((TreeField) element).getDescription();
					else
						return null;
				}	
				return null;
			}

			public Point getToolTipShift(Object object) {
				return new Point(5,5);
			}

			public int getToolTipDisplayDelayTime(Object object) {
				return 200;
			}

			public int getToolTipTimeDisplayed(Object object) {
				return 0;
			}
			
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				if (element instanceof TreeGroup)
					cell.setText(((TreeGroup) element).getDescription());
				if (element instanceof TreeRegisterGroup)
					cell.setText(((TreeRegisterGroup) element).getDescription());
				if (element instanceof TreeRegister)
				{
					cell.setText(org.eclipse.cdt.embsysregview.views.Utils.getFirstNotEmptyTextLine(((TreeRegister) element).getDescription()).trim());
				}
			    
				if (element instanceof TreeField)
					if (((TreeField) element).hasInterpretation())
					{
						cell.setText(((TreeField) element).getInterpretation());
						cell.setImage(interpretationImage);
					}
					else
					{
						// Display first line
						cell.setText(org.eclipse.cdt.embsysregview.views.Utils.getFirstNotEmptyTextLine(((TreeField) element).getDescription()).trim());
						// Display Icon if there are more than one line
						if(org.eclipse.cdt.embsysregview.views.Utils.countTextLines(((TreeField) element).getDescription())>1)
							cell.setImage(infoImage);
					}
			}
		});
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				int selectedColumn = -1;

				Tree tree = viewer.getTree();
				Point pt = tree.toControl(Display.getCurrent()
						.getCursorLocation());
				viewer.getTree().getColumnCount();
				for (int i = 0; i < tree.getColumnCount(); i++) {
					TreeItem item = tree.getItem(pt);
					if (item != null) {
						if (item.getBounds(i).contains(pt)) {
							selectedColumn = i;
						}
					}
				}
				
				if (obj instanceof TreeRegisterGroup && selectedColumn == 0) {
					TreeRegisterGroup treeRegisterGroup = ((TreeRegisterGroup)obj);
					TreeElement[] treeElements = treeRegisterGroup.getChildren();
					for(TreeElement treeElement:treeElements){
						TreeRegister treeRegister = ((TreeRegister)treeElement);
						if(!treeRegister.isWriteOnly())
						{
							treeRegister.toggleRetrieval();
							treeRegister.readValue();
						}
					}
					viewer.refresh(obj);
				}

				if (obj instanceof TreeRegister && selectedColumn == 0) {
					TreeRegister treeRegister = ((TreeRegister)obj);
					if(!treeRegister.isWriteOnly())
					{
						treeRegister.toggleRetrieval();
						treeRegister.readValue();
						viewer.refresh(obj);
					}
				}
				if(obj instanceof TreeField  && selectedColumn == 0)
				{
					TreeField treeField = ((TreeField)obj);
					TreeRegister treeRegister = ((TreeRegister)treeField.getParent());
					if(!treeRegister.isWriteOnly())
					{
						treeRegister.toggleRetrieval();
						treeRegister.readValue();
						viewer.refresh(treeField.getParent());
					}
				}		
			}
		};
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});

		viewer.setContentProvider(new ViewContentProvider());
		updateInfoLabel();
		viewer.setInput(invisibleRoot);
	}

	@Override
	public void dispose() {
	   GDBi.dispose();
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Updates Data for all TreeFields
	 */
	private void updateTreeFields(TreeElement element) {
		if (element instanceof TreeRegister)
			((TreeRegister) element).readValue();
		else if (element instanceof TreeParent
				&& ((TreeParent) element).hasChildren()) {
			TreeParent pelement = (TreeParent) element;
			for (TreeElement telement : pelement.getChildren())
				updateTreeFields(telement);
		}
	}

	/**
	 * Clears Data from all TreeFields
	 */
	private void clearTreeFields(TreeElement element) {
		if (element instanceof TreeRegister)
			((TreeRegister) element).clearValue();
		else if (element instanceof TreeParent
				&& ((TreeParent) element).hasChildren()) {
			TreeParent pelement = (TreeParent) element;
			for (TreeElement telement : pelement.getChildren())
				clearTreeFields(telement);
		}
	}

	@Override
	public void gdbTerminateListener() {
		clearTreeFields(invisibleRoot);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			}
		});
	}

	@Override
	public void gdbSuspendListener() {
		updateTreeFields(invisibleRoot);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			}
		});
	}
}
