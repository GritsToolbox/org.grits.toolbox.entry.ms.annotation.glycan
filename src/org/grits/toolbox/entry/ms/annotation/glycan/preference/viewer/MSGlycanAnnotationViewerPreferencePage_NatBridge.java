package org.grits.toolbox.entry.ms.annotation.glycan.preference.viewer;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationViewerPreferenceLoader;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.preference.viewer.MSAnnotationViewerPreferencePage_NatBridge;

public class MSGlycanAnnotationViewerPreferencePage_NatBridge extends
		MSAnnotationViewerPreferencePage_NatBridge {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationViewerPreferencePage_NatBridge.class);	

	public MSGlycanAnnotationViewerPreferencePage_NatBridge(Composite parent,
			int iMSLevel, FillTypes fillTypes, boolean bHideUnannotated) {
		super(parent, iMSLevel, fillTypes, bHideUnannotated);
	}

	@Override
	protected MSAnnotationTableDataObject getNewTableDataObject() {
		return new MSGlycanAnnotationTableDataObject(this.iMSLevel, this.fillType);
	}
		
	@Override
	protected GRITSTable getNewSimianTable(Composite parent) {
		return new MSGlycanAnnotationTable( parent, null ); 		
	}
	
	@Override
	protected TableViewerPreference getNewTableViewerPreference() {
		return new MSGlycanAnnotationViewerPreference();
	}
	
	
	@Override
	protected void initializePreferences() throws Exception {
		super.initializePreferences();
	}
	
	@Override
	protected void postProcessPreferences() {
		MSAnnotationViewerPreference preferences = (MSAnnotationViewerPreference ) getNatTable().getGRITSTableDataObject().getTablePreferences();
		MSGlycanAnnotationTableDataProcessorUtil.postProcessColumnSettings(preferences);
	}
	
	@Override
	protected void setDefaultPreferences() {
		super.setDefaultPreferences();
		MSAnnotationViewerPreference preferences = (MSAnnotationViewerPreference ) getNatTable().getGRITSTableDataObject().getTablePreferences();
		MSGlycanAnnotationTableDataProcessor.setDefaultColumnViewSettings(fillType, preferences.getPreferenceSettings());
	}
	
	
	@Override
	protected void initializeTableData(boolean _bDefault) throws Exception {
		super.initializeTableData(_bDefault);
	}
	
	@Override	
	protected TableViewerColumnSettings getDefaultSettings() {
		TableViewerColumnSettings newSettings = super.getDefaultSettings();
		if ( this.fillType == FillTypes.PeaksWithFeatures ) {
			MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationColumnSettingsCartoon(newSettings, (this.iMSLevel > 2));
			MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationColumnSettingsGlycanAnnotation(newSettings);
		}
		return newSettings;
	}
	
	@Override
	protected MassSpecViewerPreference getCurrentTableViewerPreference( int _iMSLevel, FillTypes _fillType ) {
		return MSGlycanAnnotationViewerPreferenceLoader.getTableViewerPreference(_iMSLevel, _fillType);
	}
	

	
}
