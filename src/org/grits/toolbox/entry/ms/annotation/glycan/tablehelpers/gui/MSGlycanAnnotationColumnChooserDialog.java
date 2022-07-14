 package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.display.control.table.dialog.GRITSColumnEntry;
import org.grits.toolbox.display.control.table.dialog.GRITSTableColumnChooser;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.gui.MSAnnotationColumnChooserDialog;

public class MSGlycanAnnotationColumnChooserDialog extends MSAnnotationColumnChooserDialog {
	
	public MSGlycanAnnotationColumnChooserDialog(Shell parentShell,
			String availableLabel, String selectedLabel, GRITSTableColumnChooser colChooser) {
		super(parentShell, availableLabel, selectedLabel, colChooser);
	}
	
	@Override
	public void populateAvailableTree(List<ColumnEntry> columnEntries,
			ColumnGroupModel columnGroupModel) {
		
		List<ColumnEntry> newColumnEntries = getFilteredColumnEntries(columnEntries);
		super.populateAvailableTree(newColumnEntries, columnGroupModel);
	}
	
	@Override
	public void populateSelectedTree(List<ColumnEntry> columnEntries,
			ColumnGroupModel columnGroupModel) {
		List<ColumnEntry> newColumnEntries = getFilteredColumnEntries(columnEntries);
		super.populateSelectedTree(newColumnEntries, columnGroupModel);
	}
	
	protected List<ColumnEntry> getFilteredColumnEntries( List<ColumnEntry> columnEntries ) {
		List<ColumnEntry> newColumnEntries = new ArrayList<>();
		for( int i = 0; i < columnEntries.size(); i++ ) {
			GRITSColumnEntry entry = (GRITSColumnEntry) columnEntries.get(i);
			if( entry.getLabel().endsWith(".png") ) {
				GRITSColumnEntry newEntry = new GRITSColumnEntry(entry.getKey(), "Cartoon", entry.getIndex(), entry.getPosition());
				newColumnEntries.add(newEntry);
			} else {
				newColumnEntries.add(entry);
			}
		}
		return newColumnEntries;
	}
	
	@Override
	protected void cancelPressed() {
		// TODO Auto-generated method stub
		super.cancelPressed();
	}
}
	