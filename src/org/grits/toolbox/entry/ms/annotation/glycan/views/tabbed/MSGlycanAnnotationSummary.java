package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.tablecore.DelayedResizeListener;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationSummaryTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationSummaryTable;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationDetails;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationPeaksView;
import org.grits.toolbox.entry.ms.tablehelpers.MassSpecTable;
import org.grits.toolbox.entry.ms.views.tabbed.IMSPeaksViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecPeaksView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecResultsComposite;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

public class MSGlycanAnnotationSummary extends EntryEditorPart implements IMSPeaksViewer {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSummary.class);
	public static final String VIEW_ID = "ms.annotation.glycan.views.MSAnnotationSummary";
	protected MSAnnotationDetails detailsView = null;
	protected MSGlycanAnnotationSummaryTableDataProcessor processor = null;
	protected Composite parent = null;
	protected Composite compositeTop = null;
	protected MassSpecTableBase viewBase = null;
	protected MassSpecResultsComposite resultsView = null;
	protected TableViewerColumnSettings tempSettings = null;
	protected MSGlycanAnnotationSummaryTable natTable = null;
	// hold copies of a parent subset table data. To be used for interaction between editors
	protected MSAnnotationTable parentSubsetTableData = null;
	protected int parentViewRowIndex = -1;
	protected String parentViewRowId = null;
	protected int parentViewScanNum = -1;
	
	protected GRITSProgressDialog dtpdThreadedDialog = null;
	protected int iStatus = -1;
	private MPart part;
	
	@Inject
	public MSGlycanAnnotationSummary( MSAnnotationDetails detailsView) {
		super();
		this.detailsView = detailsView;
//		setParentSubsetTableData();
	}
	
	@PostConstruct
	public void postConstruct (MPart part) {
		this.part = part;
	}

	public MPart getPart() {
		return part;
	}

	
	public void setDtpdThreadedDialog(GRITSProgressDialog dtpdThreadedDialog) {
		this.dtpdThreadedDialog = dtpdThreadedDialog;
	}
	

	public String getParentViewRowId() {
		return parentViewRowId;
	}
	
	public int getParentViewScanNum() {
		return parentViewScanNum;
	}

	public int getParentViewRowIndex() {
		return parentViewRowIndex;
	}

	public MSAnnotationTable getParentSubsetTable() {
		return parentSubsetTableData;
	}

	protected MSAnnotationMultiPageViewer getParentMultiPageViewer() {
		Entry parentEntry = this.detailsView.getEntry().getParent();
		MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(part.getContext(), parentEntry);		
		return viewer;
	}	

	public void setParentSubsetTableData() {
		MSAnnotationMultiPageViewer viewer = getParentMultiPageViewer();
		if (viewer != null) {
			Object activePage = viewer.getPageItem(viewer.getActivePage());
			MassSpecPeaksView peaksView = null;
			if( activePage instanceof MSAnnotationPeaksView ) {
				peaksView = viewer.getPeaksView().get(0);
			} else if ( activePage instanceof MSAnnotationDetails ) {
				MSAnnotationDetails detailsView = viewer.getDetailsView();
				peaksView = detailsView.getCurrentPeaksView();
			}
			MassSpecTableBase tableBase = peaksView.getViewBase();
			MassSpecTable msTable = tableBase.getNatTable();
			parentSubsetTableData = ((MSAnnotationTable) msTable).getCurrentSubsetTable();
			parentViewRowId = parentSubsetTableData.getParentTableRowId();
			parentViewRowIndex = parentSubsetTableData.getParentTableRowIndex();
			parentViewScanNum = parentSubsetTableData.getParentTableParentScanNum();
			processor.setParentViewerSubsetTable( (MSGlycanAnnotationTable) parentSubsetTableData );
			
//			processor.setParentViewerSubsetTable( (MSGlycanAnnotationTable) ((MSAnnotationTable) msTable).getCurrentSubsetTable() );
		}
	}

	public void setParentSubsetTableData(MSAnnotationTable parentSubsetTableData, int parentViewRowIndex, int iParentScanNum, String sParentRowId) {
		this.parentSubsetTableData = parentSubsetTableData;
		this.parentViewRowIndex = parentViewRowIndex;
		this.parentViewRowId = sParentRowId;
		this.parentViewScanNum = iParentScanNum;
	}

	@Override
	public String toString() {
		return "MSGlycanAnnotationSummary (" + entry + ")";
	}	

	public MSGlycanAnnotationSummaryTable getNatTable() {
		return natTable;
	}

	public MassSpecTableBase getViewBase() {
		return viewBase;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		try {
			this.parent = parent;
			final Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(new FillLayout());
			createView(container);
			addListeners(container);
		} catch( Exception e ) {
			viewBase = null;
			resultsView = null;
		}

	}

	protected void addListeners(Composite container) {
		DelayedResizeListener l = new DelayedResizeListener();
		if (natTable != null) {
			l.addTable(natTable);
		}
		container.addControlListener(l);
	}

	protected void createView(Composite container) throws Exception {
		initResultsView(container);
	}

	public MSGlycanAnnotationSummaryTableDataProcessor getTableDataProcessor() {
		return processor;
	}

	// 04/24/2018 MASAAKI
	protected MSGlycanAnnotationSummaryTableDataProcessor getNewTableDataProcessor(List<MSAnnotationTableDataProcessor> alList) {
		return new MSGlycanAnnotationSummaryTableDataProcessor(
				this.detailsView.getEntry(), this.detailsView.getMsEntityProperty(), alList
			);
	}

	protected MSGlycanAnnotationSummaryResultsComposite getNewResultsComposite( Composite composite, int style ) {
		return new MSGlycanAnnotationSummaryResultsComposite(composite, style);
	}

	@Override
	public int getStatus() {
		if( resultsView == null || viewBase == null ) {
			return GRITSProcessStatus.ERROR;
		} else if ( processor.isCanceled() ) {
			return GRITSProcessStatus.CANCEL;
		}
		return GRITSProcessStatus.OK;
//		return resultsView != null && viewBase != null;
	}


	protected void initResultsView(Composite parent) throws Exception {	

		compositeTop = new Composite(parent, SWT.BORDER);
		compositeTop.setLayout(new GridLayout(1, false));

		try {
			List<MSAnnotationTableDataProcessor> alList = new ArrayList<>();
			for( MSAnnotationPeaksView pv : this.detailsView.getPeaksViews() ) {
				alList.add( (MSAnnotationTableDataProcessor) pv.getTableDataProcessor());
			}
			// 04/24/2018 MASAAKI
//			processor = new MSGlycanAnnotationSummaryTableDataProcessor(this.detailsView.getEntry(),
//					this.detailsView.getMsEntityProperty(), alList);
			processor = getNewTableDataProcessor(alList);
			processor.setMethod(( (MSAnnotationTableDataProcessor) this.detailsView.getPeaksViews().get(0).getTableDataProcessor()).getMethod() ); 
			processor.initializeTableDataObject(this.detailsView.getMsEntityProperty());
			processor.setProgressBarDialog(dtpdThreadedDialog);
			setParentSubsetTableData();
			resultsView = getNewResultsComposite(compositeTop, SWT.NONE);
			resultsView.createPartControl(this.compositeTop, this, this.detailsView.getMsEntityProperty(), this.processor, FillTypes.Scans);
			resultsView.setLayout(new FillLayout());
			this.viewBase = resultsView.getBaseView();
			this.viewBase.layout();
		} catch (Exception e) {
			viewBase = null;
			resultsView = null;
			logger.error("Error in MSGlycanAnnotationSummary: initResultsView");
			throw new Exception(e.getMessage());
		}
	}


	public void layout() {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		natTable.setLayoutData(gridData);
		natTable.redraw();
	}

	@Override
	protected void updateProjectProperty() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void savePreference() {
		// TODO Auto-generated method stub

	}

	@Override
	protected Composite getParent() {
		// TODO Auto-generated method stub
		return this.parent;
	}

	@Focus
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reInitializeView() throws Exception {
		throw new Exception("Not implemented yet for MSGlycanAnnotationSummary.");

	}


	@Override
	public void reLoadView() {
		// TODO Auto-generated method stub
		
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
			if (this.viewBase != null && this.viewBase.getNatTable() != null) {
				((MSGlycanAnnotationTable)this.viewBase.getNatTable()).refreshTableImages();
			}
	 	}
	}

	public MSAnnotationDetails getDetailsView() {
		return detailsView;
	}
}
