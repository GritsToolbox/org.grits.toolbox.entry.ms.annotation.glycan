package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.command.ViewRowChooserInTabCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.IMSAnnotationPeaksViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationPeaksView;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSelectionView;
import org.grits.toolbox.entry.ms.views.tabbed.IMSPeaksViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;

public class MSGlycanAnnotationPeaksView extends MSAnnotationPeaksView implements IMSAnnotationPeaksViewer {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationPeaksView.class);
	public static final String VIEW_ID = "plugin.ms.annotation.glycan.views.MSGlycanAnnotationPeaksView"; //$NON-NLS-1$
	private MPart part;
	
	@Inject
	public MSGlycanAnnotationPeaksView (@Optional Entry entry, @Optional Property msEntityProperty,
			@Named(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT) int iMinMSLevel) {
		super (entry, msEntityProperty, iMinMSLevel);
	}
	
	@PostConstruct
	public void postConstruct (MPart part) {
		this.part = part;
	}

	public MPart getPart() {
		return part;
	}

	protected MSGlycanAnnotationResultsComposite getNewResultsComposite( Composite composite, int style ) {
		return new MSGlycanAnnotationResultsComposite(composite, style);
	}
	
	@Override
	public String toString() {
		return "MSGlycanAnnotationPeaksView (" + entry + ")";
	}	
	
	public static void showRowSelection(IEclipseContext context, Entry entry, MSAnnotationTable parentTable, int iRowNumber, int iScanNum, String sRowId ) {
		MSGlycanAnnotationMultiPageViewer parent = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(context, entry);
		if ( parent != null ) {
			MSGlycanAnnotationPeaksView me = (MSGlycanAnnotationPeaksView) parent.getPeaksView();
			if( me == null ) {
				me = (MSGlycanAnnotationPeaksView) parent.getAnnotationDetails().getCurrentPeaksView();
			}
			MSAnnotationSelectionView viewer = ViewRowChooserInTabCommandExecutor.showRowChooser(me, parentTable, iRowNumber, iScanNum, sRowId);
			me.setSelectionView(viewer);
			me.getBottomPane().layout();
		}
	}
	
	@Override
	public void initNewSelectionView() {
		setSelectionView(new MSGlycanAnnotationSelectionView(getBottomPane()));
	}
	
	@Override
	protected TableDataProcessor getNewTableDataProcessor( Entry entry, Property entityProperty  ) {
		MSGlycanAnnotationTableDataProcessor proc = new MSGlycanAnnotationTableDataProcessor(
				entry, entityProperty, 
				FillTypes.PeaksWithFeatures, getMinMSLevel());
		proc.initializeTableDataObject(entityProperty);
		return proc;
	}
	
	@Override
	protected TableDataProcessor getNewTableDataProcessor(Property entityProperty) {
		Entry parentEntry = getEntry().getParent();
		MSGlycanAnnotationMultiPageViewer parentViewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(part.getContext(), parentEntry);
		if( parentViewer == null || parentViewer.getScansView() == null ) {
			return null;
		}
		MSGlycanAnnotationTableDataProcessor parentProc = (MSGlycanAnnotationTableDataProcessor) parentViewer.getScansView().getTableDataProcessor();
		if( parentProc.getGRITSdata() == null ) // DBW 04-30-15, addressing Ticket # 320
			return null;
		FillTypes fillType = FillTypes.PeaksWithFeatures;
		if( entityProperty instanceof MSAnnotationEntityProperty ) {
			MSAnnotationEntityProperty ep = ((MSAnnotationEntityProperty) entityProperty);
			if( ep.getMsLevel() > 3 && ep.getAnnotationId() == -1 ) {// if not annotated, show peak list only
				fillType = FillTypes.PeakList;
			}
		}
		MSGlycanAnnotationTableDataProcessor proc = new MSGlycanAnnotationTableDataProcessor(parentProc, entityProperty, 
				fillType, getMinMSLevel());
		proc.initializeTableDataObject(entityProperty);
		proc.setEntry(getEntry());
		return proc;
	}
	
	/**
	 * This method is called whenever a preference change occurs
	 * We need to act upon cartoon preference changes for this view 
	 * 
	 * @param preferenceName
	 */
	@Optional @Inject
	public void updatePreferences(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED)
	 					String preferenceName)
	{
	 	if (MSGlycanAnnotationCartoonPreferences.getPreferenceID().equals(preferenceName)) {
	 		try {
				MSGlycanAnnotationSelectionView sv = (MSGlycanAnnotationSelectionView) this.getCurrentSelectionView();
				if( sv != null && sv.getSubTable() != null ) {
					((MSGlycanAnnotationTable) sv.getSubTable()).refreshTableImages();
				}
			} catch( Exception e ) {
				logger.error("Could not refresh subtable images", e);
			}
	 		
	 		try {
				MassSpecTableBase viewBase = (MassSpecTableBase) ( (IMSPeaksViewer) this ).getViewBase();
				if (viewBase != null && viewBase.getNatTable() != null)
					((MSGlycanAnnotationTable) viewBase.getNatTable()).refreshTableImages();
			} catch( Exception e ) {
				logger.error("Could not refresh table images", e);
			}
	 	}
	}
}
