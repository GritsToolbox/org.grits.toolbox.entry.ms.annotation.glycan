package org.grits.toolbox.entry.ms.annotation.glycan.preference.viewer;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationSummaryViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationSummaryViewerPreferenceLoader;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationSummaryTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationSummaryTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationSummaryTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationSummaryTable;
import org.grits.toolbox.entry.ms.annotation.preference.viewer.MSAnnotationViewerPreferencePage_NatBridge;

public class MSGlycanAnnotationSummaryViewerPreferencePage_NatBridge extends
		MSAnnotationViewerPreferencePage_NatBridge {

	public MSGlycanAnnotationSummaryViewerPreferencePage_NatBridge(Composite parent,
			int iMSLevel, FillTypes fillTypes, boolean bHideUnannotated) {
		super(parent, iMSLevel, fillTypes, bHideUnannotated);
	}

	@Override
	protected MSAnnotationTableDataObject getNewTableDataObject() {
		return new MSGlycanAnnotationSummaryTableDataObject(this.iMSLevel, this.fillType);
	}
		
	@Override
	protected GRITSTable getNewSimianTable(Composite parent) {
		return new MSGlycanAnnotationSummaryTable( parent, null ); 		
	}
	
	@Override
	protected TableViewerPreference getNewTableViewerPreference() {
		return new MSGlycanAnnotationSummaryViewerPreference();
	}
	
	@Override	
	protected TableViewerColumnSettings getDefaultSettings() {
		TableViewerColumnSettings newSettings = getNewTableViewerSettings();
		MSGlycanAnnotationSummaryTableDataProcessorUtil.fillMSGlycanAnnotationSummaryColumnSettings(newSettings);
		return newSettings;
	}
	
	@Override
	protected MassSpecViewerPreference getCurrentTableViewerPreference( int _iMSLevel, FillTypes _fillType ) {
		return MSGlycanAnnotationSummaryViewerPreferenceLoader.getTableViewerPreference(_iMSLevel, _fillType);
	}
	
	@Override
	protected void initializePreferences() throws Exception {
		super.initializePreferences();
	}
	
	@Override
	protected void postProcessPreferences() {
		MSGlycanAnnotationSummaryViewerPreference preferences = (MSGlycanAnnotationSummaryViewerPreference ) getNatTable().getGRITSTableDataObject().getTablePreferences();
		MSGlycanAnnotationSummaryTableDataProcessorUtil.postProcessColumnSettings(preferences);
	}
	
	@Override
	protected void setDefaultPreferences() {
		super.setDefaultPreferences();
		MSGlycanAnnotationSummaryViewerPreference preferences = (MSGlycanAnnotationSummaryViewerPreference ) getNatTable().getGRITSTableDataObject().getTablePreferences();
		MSGlycanAnnotationSummaryTableDataProcessor.setDefaultColumnViewSettings(preferences.getPreferenceSettings());
	}
	
}
