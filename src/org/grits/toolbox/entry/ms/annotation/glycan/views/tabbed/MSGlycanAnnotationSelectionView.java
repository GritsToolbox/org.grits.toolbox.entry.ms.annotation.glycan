package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSelectionView;

public class MSGlycanAnnotationSelectionView extends MSAnnotationSelectionView {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSelectionView.class);
	
	public MSGlycanAnnotationSelectionView( Composite parent ) {
		super(parent);
	}
	
	@Override
	public String toString() {
		return "MSGlycanAnnotationSelectionView (" + parentTable + ")";
	}	
	
	@Override
	protected void initTable() {
		subTable = new MSGlycanAnnotationTable(parent, (MSGlycanAnnotationTable) parentTable, iParentRowIndex, iParentScanNum, sParentRowId);
		subTable.createSubsetTable();
		addListeners();
	}
}
