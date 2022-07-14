package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import org.apache.log4j.Logger;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewerWorker;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSGlycanAnnotationMultiPageViewerWorker extends MSAnnotationMultiPageViewerWorker {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationMultiPageViewerWorker.class);


	public MSGlycanAnnotationMultiPageViewerWorker(
			MassSpecMultiPageViewer parentEditor, MassSpecEntityProperty prop) {
		super(parentEditor, prop);
	}

	@Override
	public int doWork() {
		int iSuccess = super.doWork();
		if( getParentEditor().getStatus() != GRITSProcessStatus.OK) {
			return getParentEditor().getStatus();
		}
		MSGlycanAnnotationMultiPageViewer msParentEditor = (MSGlycanAnnotationMultiPageViewer) getParentEditor();
		if( msParentEditor.needsSummaryView((MassSpecEntityProperty) prop) ) {
			iSuccess = addSummaryPage(prop, iMajorCount);
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			} 
			iMajorCount+=2;
		}
		updateListeners("Finished MS Glycan Annotation work!", iMajorCount);
		logger.debug("Finished MS Annotation work");
		return iSuccess;
	}

	public int addSummaryPage(MassSpecEntityProperty prop, int iProcessCount) {
		try {
			updateListeners("Creating Summary tab (loading)", iProcessCount);
			int iSuccess = ((MSGlycanAnnotationMultiPageViewer) getParentEditor()).addSummaryPage_Step1(prop);
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			}
			updateListeners("Creating Summary tab (populating)", iProcessCount + 1);
			iSuccess = ((MSGlycanAnnotationMultiPageViewer) getParentEditor()).addSummaryPage_Step2();
			updateListeners("Creating Summary tab (done)", iProcessCount + 2);
			return iSuccess;				 
		} catch( Exception e ) {
			logger.error("Unable to open Summary view", e);
		}
		return GRITSProcessStatus.ERROR;
	}
}
