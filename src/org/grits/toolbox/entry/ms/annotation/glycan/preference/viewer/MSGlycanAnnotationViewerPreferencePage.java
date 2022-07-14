package org.grits.toolbox.entry.ms.annotation.glycan.preference.viewer;

import org.eclipse.swt.widgets.Composite;

import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.preference.viewer.MSAnnotationViewerPreferencePage;
import org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge;


public class MSGlycanAnnotationViewerPreferencePage extends MSAnnotationViewerPreferencePage {

	public MSGlycanAnnotationViewerPreferencePage() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Override
	protected MassSpecViewerPreferencePage_NatBridge getPreferenceUItoNatBridge(boolean _bDefault) {
		String sFillLabel = getTableFillLabel();
		MassSpecViewerPreferencePage_NatBridge natBridge = null;
		if( sFillLabel.contains("Summary") ) {
			natBridge = new MSGlycanAnnotationSummaryViewerPreferencePage_NatBridge( 
					new Composite(getShell(), NONE), 
					getCurMSLevel(), getTableFillType(), getHideUnannotatedPeaks() );			
		} else {
			natBridge = new MSGlycanAnnotationViewerPreferencePage_NatBridge( 
					new Composite(getShell(), NONE), 
					getCurMSLevel(), getTableFillType(), getHideUnannotatedPeaks() );
		} 
		natBridge.initializeComponents(_bDefault);
		return natBridge;
	}

	@Override
	protected void updateTableTypeCombo( int _iMSLevel ) {
		String[] tableTypes = MSGlycanAnnotationMultiPageViewer.getPreferencePageLabels(_iMSLevel);
		String defaultTable = tableTypes[0];
		comboTablelevel.setItems(tableTypes);
		comboTablelevel.setText(defaultTable);		
	}

	@Override
	protected FillTypes getTableFillType() {
		FillTypes[] fillTypes = MSGlycanAnnotationMultiPageViewer.getPreferencePageFillTypes(getCurMSLevel());
		FillTypes fillType = fillTypes[getTableNumber()];
		return fillType;
	}

	@Override
	protected String getTableFillLabel() {
		String[] labels = MSGlycanAnnotationMultiPageViewer.getPreferencePageLabels(getCurMSLevel());
		String label = labels[getTableNumber()];
		return label;
	}
	
	@Override
	protected int getMaxNumTables() {
		int iMaxTableTypes = MSGlycanAnnotationMultiPageViewer.getPreferencePageMaxNumPages();
		return iMaxTableTypes;
	}

}
