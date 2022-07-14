package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.gui;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.display.control.table.dialog.ColumnChooserDialog;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.tablecore.IGritsTable;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.gui.MSAnnotationTableColumnChooser;

public class MSGlycanAnnotationTableColumnChooser extends MSAnnotationTableColumnChooser {

	public MSGlycanAnnotationTableColumnChooser(Shell shell,
			boolean sortAvailableColumns,
			boolean asGlobalPreference, 
			IGritsTable gritsTable ) {
		super(shell, sortAvailableColumns, asGlobalPreference, gritsTable );
	}

	@Override
	protected ColumnChooserDialog getNewColumnChooserDialog(Shell shell) {
		if ( asGlobalPreference ) 		
			columnChooserDialog = new ColumnChooserDialog(shell, Messages.getString("ColumnChooser.availableColumns"), Messages.getString("ColumnChooser.selectedColumns")); //$NON-NLS-1$ //$NON-NLS-2$
		else
			columnChooserDialog = new MSGlycanAnnotationColumnChooserDialog(shell, 
					Messages.getString("ColumnChooser.availableColumns"), 
					Messages.getString("ColumnChooser.selectedColumns"), this); //$NON-NLS-1$ //$NON-NLS-2$	
		
		return columnChooserDialog;
	}

	@Override
	protected TableViewerColumnSettings getDefaultSettings() {
		MSGlycanAnnotationTableDataProcessor proc = (MSGlycanAnnotationTableDataProcessor) getGRITSTable().getTableDataProcessor();
		TableViewerPreference newPref = proc.initializePreferences();
		TableViewerColumnSettings newSettings = newPref.getPreferenceSettings();
		MSGlycanAnnotationTableDataProcessor.setDefaultColumnViewSettings(proc.getSimianTableDataObject().getFillType(), newSettings);
		return newSettings;
	}
	
}
