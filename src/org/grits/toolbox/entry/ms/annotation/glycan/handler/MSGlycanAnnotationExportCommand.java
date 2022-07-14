package org.grits.toolbox.entry.ms.annotation.glycan.handler;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationSummaryTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.adaptor.MSAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExportDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.adaptor.MSGlycanAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.glycan.adaptor.MSGlycanAnnotationSummartExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationExportDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationSummaryExportDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.handler.MSAnnotationExportCommand;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;

/**
 * Export command. call SimianExportDialog.
 * 
 * @author kitaemyoung
 * 
 */
public class MSGlycanAnnotationExportCommand extends MSAnnotationExportCommand {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationExportCommand.class);

	@Override
	protected MSAnnotationExportFileAdapter getNewExportAdapter() {
		if (this.getTableDataObject() != null && this.getTableDataObject() instanceof MSGlycanAnnotationSummaryTableDataObject) {
			return new MSGlycanAnnotationSummartExportFileAdapter();
		} else {
			MSGlycanAnnotationExportFileAdapter adapter = new MSGlycanAnnotationExportFileAdapter();
			return adapter;
		}
	}
	
	@Override
	protected MSAnnotationExportDialog getNewExportDialog(Shell activeShell, MSAnnotationExportFileAdapter adapter) {
		if (this.getTableDataObject() != null && this.getTableDataObject() instanceof MSGlycanAnnotationSummaryTableDataObject) {
			return new MSGlycanAnnotationSummaryExportDialog(PropertyHandler.getModalDialog(activeShell), adapter);
		} else {
			return new MSGlycanAnnotationExportDialog(PropertyHandler.getModalDialog(activeShell), adapter);
		}
	}
	
	@Override
	protected void createSimianExportDialog(Shell activeShell) {
		MSAnnotationExportFileAdapter adapter = getNewExportAdapter();
		MSAnnotationExportDialog dialog = getNewExportDialog (activeShell, adapter);
		// set parent entry
		dialog.setMSAnnotationEntry(getEntry());
		dialog.setTableDataObject(getTableDataObject());
		dialog.setMasterParentScan(getMasterParentScan());
		dialog.setLastVisibleColInx(getLastVisibleColInx());
		if (dialog.open() == Window.OK) {
			// to do something..
		}
	}
	
	@Override
	protected boolean initialize(MPart part, EPartService partService) {
		try {
			MSAnnotationMultiPageViewer viewer = null;
			if (part != null && part.getObject() instanceof MSAnnotationMultiPageViewer) {
				viewer = (MSAnnotationMultiPageViewer) part.getObject();
			} else { // try to find an open part of the required type
				for (MPart mPart: partService.getParts()) {
					if (mPart.getObject() instanceof MSAnnotationMultiPageViewer) {
						if (mPart.equals(mPart.getParent().getSelectedElement())) {
							viewer = (MSAnnotationMultiPageViewer) mPart.getObject();
							if (!viewer.getPeaksView().isEmpty())
								break;
						}
					}
				}
			}
			if (viewer != null) {
				if( viewer.getPeaksView().isEmpty() ) {
					return false;
				}
				setEntry(viewer.getEntry());
				
				MSAnnotationTableDataObject tdo = null;
				// for ms2/ms3 tables, peaksview is null, we have to export them differently
				if (viewer.getPeaksView().get(0) == null || viewer.getPeaksView().get(0).getViewBase() == null) {
					if (viewer instanceof MSGlycanAnnotationMultiPageViewer) {   // export summary view
						if (((MSGlycanAnnotationMultiPageViewer) viewer).getSummaryView() != null &&
								((MSGlycanAnnotationMultiPageViewer) viewer).getSummaryView().getViewBase() != null &&
								((MSGlycanAnnotationMultiPageViewer) viewer).getSummaryView().getViewBase().getNatTable() != null) {
							tdo = (MSAnnotationTableDataObject) ((MSGlycanAnnotationMultiPageViewer) viewer)
								.getSummaryView().getViewBase().getNatTable().getGRITSTableDataObject();
							if (tdo != null) {
								setTableDataObject(tdo);
								TableDataProcessor processor = ((MSGlycanAnnotationMultiPageViewer) viewer)
									.getSummaryView().getTableDataProcessor();
								setLastVisibleColInx( processor.getLastVisibleCol() );
								return true;
							}
						}
					}
					return false;
				} else {
					tdo = (MSAnnotationTableDataObject) viewer.getPeaksView().get(0).getViewBase().getNatTable().getGRITSTableDataObject();
					setTableDataObject(tdo);
					
					int iMasterParentScan = ((MSAnnotationTable) viewer.getPeaksView().get(0).getViewBase().getNatTable()).getScanNumberForVisibility(tdo, -1);
					setMasterParentScan(iMasterParentScan);
					TableDataProcessor processor = viewer.getPeaksView().get(0).getViewBase().getNatTable().getTableDataProcessor();
					setLastVisibleColInx( processor.getLastVisibleCol() );
					return true;
				}
			} else {
				return false;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
			return false;
		}		
	}	
}
