package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import javax.inject.Inject;

import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationExternalQuantDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationPeakIntensityApplyDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationStandardQuantApplyDialog;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationQuantificationView;
import org.grits.toolbox.entry.ms.preference.MassSpecPreference;
import org.grits.toolbox.entry.ms.preference.MassSpecStandardQuantPreferenceUI;

public class MSGlycanAnnotationQuantificationView extends MSAnnotationQuantificationView {

	@Inject
	public MSGlycanAnnotationQuantificationView(Entry entry) {
		super(entry);
	}
	
	@Override
	protected MSAnnotationMultiPageViewer getCurrentViewer () {
		return MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(MSGlycanAnnotationQuantificationView.this.getPart().getContext(), this.entry);
	}

	@Override
	protected void openExternalQuantDialog(MSAnnotationMultiPageViewer curView) {
		MSGlycanAnnotationExternalQuantDialog mseqd = new MSGlycanAnnotationExternalQuantDialog(Display.getCurrent().getActiveShell(), curView);
		mseqd.addListener(curView);
		mseqd.addListener(this);
		mseqd.open();
	}
	
	@Override
	protected void openStandardQuantDialog(MSAnnotationMultiPageViewer curView) {
		if( MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog == null || 
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.getShell() == null || 
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.getShell().isDisposed() ) {
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog  = 
					new MSGlycanAnnotationStandardQuantApplyDialog(Display.getCurrent().getActiveShell(), curView);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.addListener(curView);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.addListener(this);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.open();
		} else {
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.getShell().forceActive();
		}
	}
	
	@Override
	protected void openIntensityDialog(MSAnnotationMultiPageViewer curView) {
		if( MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog == null || 
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog.getShell() == null || 
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog .getShell().isDisposed() ) {
			MSAnnotationTableDataObject msatdo = (MSAnnotationTableDataObject)((MSAnnotationTableDataProcessor) curView.getPeaksView().get(0).getTableDataProcessor()).getSimianTableDataObject();
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog = 
					new MSGlycanAnnotationPeakIntensityApplyDialog(Display.getCurrent().getActiveShell(), curView, msatdo);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog.addListener(curView);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog.addListener(this);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog.open();
		} else {
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationPeakIntensityApplyDialog .getShell().forceActive();
		}
	}

	@Override
	protected void initializeStandardQuantifications() {
		localStandardQuant = new MassSpecPreference();
		MassSpecStandardQuantPreferenceUI.initStandardQuantFromEntry(getEntrySettings(), localStandardQuant);
		entryStandardQuant = new MassSpecPreference();
		MassSpecStandardQuantPreferenceUI.initStandardQuantFromEntry(getEntrySettings(), entryStandardQuant);
	}

}
