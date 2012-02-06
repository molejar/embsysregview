package org.eclipse.cdt.embsysregview.views;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

public class HexCellEditorValidator implements ICellEditorValidator {
	
	TreeViewer viewer;
	
	public HexCellEditorValidator(TreeViewer viewer) {
		super();
		this.viewer = viewer;
	}

	@Override
	public String isValid(Object value) {
		
				if(value instanceof String && ((String)value).startsWith("0x"))
				{
					String svalue=((String)value);
					long lvalue;
					try {
						lvalue=Long.valueOf(svalue.substring(2, svalue.length()), 16);
						int bits=32;
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection)selection).getFirstElement();
						if(obj instanceof TreeField)
							bits=((TreeField)obj).getBitLength();
						long maxvalue=(1L<<bits)-1;
						if(lvalue >= 0 && lvalue <=maxvalue)
							return null;
						else
							return "out of range";
					}catch(NumberFormatException nfe){
						
					}
				}	
				return null;
	}

}
