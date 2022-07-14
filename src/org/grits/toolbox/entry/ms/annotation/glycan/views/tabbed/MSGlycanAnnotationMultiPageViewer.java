package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationSummaryViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferencesLoader;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.ms.annotation.command.ViewRowChooserInTabCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationCustomAnnotationDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationPeakIntensityApplyDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationStandardQuantApplyDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationSummaryTable;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationDetails;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSelectionView;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSpectraView;
import org.grits.toolbox.entry.ms.dialog.MassSpecCustomAnnotationDialog;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;
import org.grits.toolbox.ms.om.data.AnnotationFilter;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.utils.data.CartoonOptions;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.progress.CancelableThread;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;

/**
 * A tabbed-editor for displaying information for MS Glycan Annotation Data.<br>
 * This editor extends MSAnnotationMultiPageViewer.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MSAnnotationMultiPageViewer
 * @see MSGlycanAnnotationSummary
 * @see AnnotationFilter
 * @see CartoonOptions
 *
 */
public class MSGlycanAnnotationMultiPageViewer extends MSAnnotationMultiPageViewer {
	public static String VIEW_ID = "plugin.ms.annotation.glycan.views.MSGlycanAnnotationMultiPageViewer";
	private static final Logger logger = Logger.getLogger(MSAnnotationMultiPageViewer.class);
	protected MSGlycanAnnotationSummary summaryView = null;
	protected int summaryViewTabIndex = -1;
	private AnnotationFilter filter;
	boolean dirtyVal = false;
	
	@Inject IEventBroker eventBroker;
	
	public static final String EVENT_TOPIC_FILTER_CHANGED="FilterChanged";
		
	public static MSGlycanAnnotationCustomAnnotationDialog msGlycanAnnotationCustomAnnotationDialog = null;
	public static MSGlycanAnnotationStandardQuantApplyDialog msGlycanAnnotationStandardQuantApplyDialog = null;
	public static MSGlycanAnnotationPeakIntensityApplyDialog msGlycanAnnotationPeakIntensityApplyDialog = null;

	@Inject
	public MSGlycanAnnotationMultiPageViewer( Entry entry ) {
		super(entry);
	}
	
	@Inject
	public MSGlycanAnnotationMultiPageViewer (MPart part) {
		super(part);
	}

	@Override
	public String toString() {
		return "MSGlycanAnnotationMultiPageViewer (" + entry + ")";
	}

	@Override
	protected Object getDesiredActivePage() {
		if( summaryView == null ) {
			return super.getDesiredActivePage();
		}
		return summaryView;
	}
	

	public int getSummaryViewTabIndex() {
		return summaryViewTabIndex;
	}

	public void setSummaryViewTabIndex(int summaryViewTabIndex) {
		this.summaryViewTabIndex = summaryViewTabIndex;
	}

	public MSGlycanAnnotationSummary getSummaryView() {
		return summaryView;
	}

	@Override
	protected MassSpecSpectraView getNewSpectraView() {
		getPart().getContext().set(Entry.class, this.entry);
		return ContextInjectionFactory.make(MSGlycanAnnotationSpectraView.class, getPart().getContext());
		//return new MSGlycanAnnotationSpectraView(this.entry);
	}
	
	

	@Override
	protected int addPages( final int _iMajorCount, final MassSpecEntityProperty prop ) {
		this.dtpdThreadedDialog = new GRITSProgressDialog(new Shell(), 1, false);
		this.dtpdThreadedDialog.open();
		this.dtpdThreadedDialog.getMajorProgressBarListener().setMaxValue(_iMajorCount);
		MSGlycanAnnotationMultiPageViewerWorker msmpvw = new MSGlycanAnnotationMultiPageViewerWorker(this, prop);
		this.dtpdThreadedDialog.setGritsWorker(msmpvw);
		int iSuccess = this.dtpdThreadedDialog.startWorker();
		
		// set the filter
		getFilter();
		sendFilterChanged();
		return iSuccess;		
	}

	@Override
	protected int getNumMajorSteps(MassSpecEntityProperty prop) {
		int iNumPages =  super.getNumMajorSteps(prop);
		if( needsSummaryView((MassSpecEntityProperty) prop) ) {
			iNumPages += 2;
			setSummaryViewTabIndex(getAnnotDetailsTabIndex()+1); // right after details?
		}
		return iNumPages;
	}

	public int addSummaryPage_Step1( MassSpecEntityProperty prop ) {
		try {
			int iSuccess = initSummaryView((MassSpecEntityProperty) prop);
			dtpdThreadedDialog.setMinorStatus(iSuccess);
			if( iSuccess == GRITSProcessStatus.CANCEL ) {
				setStatus(GRITSProcessStatus.CANCEL);
				return GRITSProcessStatus.CANCEL;
			}
		} catch( Exception ex ) {
			logger.error("Unable to open Summary view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the Summary tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();	
	}

	public int addSummaryPage_Step2() {
		try {
			boolean success = true;
			int iPageCount = getPageCount();
			try {
				int inx = getSummaryViewTabIndex();
				if( inx >= getPageCount() ) {
					inx = addPage( getSummaryView(), entry );	
					setSummaryViewTabIndex(inx);
				} else {
					addPage( inx, getSummaryView(), entry);
				}
				setPageText(inx, "Summary");
				setActivePage(inx);
				int iSuccess = summaryView.getStatus();
				setStatus(iSuccess);
				dtpdThreadedDialog.setMinorStatus(iSuccess);
			} catch( Exception ex ) {
				logger.error("Error adding Peaks List tab.", ex);
				setStatus(GRITSProcessStatus.ERROR);
			}			
			success = (getStatus() != GRITSProcessStatus.ERROR);

			if( ! success ) {
				if( getPageCount() != iPageCount ) {
					removePage(getPageCount()-1);
				}
				summaryView = null;
			} else {
				setSummaryViewTabIndex(getPageCount() - 1);
			}
		} catch( Exception ex ) {
			logger.error("Unable to open Summary view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the Summary tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();	
	}	

	@Override
	protected boolean initMSAnnotationPropertyView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotPropertyView = ContextInjectionFactory.make(MSGlycanAnnotationPropertyView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open property view", ex);
		}		
		return false;
	}
	
	@Override
	protected boolean initMsAnnotationFilterView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotFilterView = ContextInjectionFactory.make(MSGlycanAnnotationFilterView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open interactive settings view", ex);
		}		
		return false;
	}
	
	@Override
	protected boolean initOtherSettingsView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotOtherView = ContextInjectionFactory.make(MSGlycanAnnotationOtherSettingsView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open interactive settings view", ex);
		}		
		return false;
	}
	
	@Override
	protected boolean initQuantificationView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotQuantView = ContextInjectionFactory.make(MSGlycanAnnotationQuantificationView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open interactive settings view", ex);
		}		
		return false;
	}

	@Override
	public boolean needsSpectraView(MassSpecEntityProperty entityProperty) {
//		if( entityProperty.getMsLevel() > (getMinMSLevel() + 1) && 
		if( entityProperty.getMsLevel() > 2 && 
				entityProperty.getMassSpecParentProperty().getMassSpecMetaData().getMsExperimentType().equals(Method.MS_TYPE_MSPROFILE))
			return false;
		return super.needsSpectraView(entityProperty);
	}

	protected boolean needsSummaryView( MassSpecEntityProperty entityProperty ) {
		if( entityProperty.getParentScanNum() == null || entityProperty.getParentScanNum() < 0 ) 
			return false;
		if( entityProperty.getMsLevel() < 3 ) 
			return false;
		if( ((MSAnnotationEntityProperty) entityProperty).getAnnotationId() == null || 
				((MSAnnotationEntityProperty) entityProperty).getAnnotationId() < 0 ) 
			return false;
		return true; 
	}

	protected int initSummaryView( MassSpecEntityProperty entityProperty ) {
		try {
			summaryView = getNewSummaryView( this.entry, entityProperty);
			return GRITSProcessStatus.OK;
		} catch( Exception ex ) {
			logger.error("Unable to open peaks view", ex);
		}		
		return GRITSProcessStatus.ERROR;
	}

	protected MSGlycanAnnotationSummary getNewSummaryView( Entry entry, MassSpecEntityProperty entityProperty) {
		MSGlycanAnnotationMultiPageViewer parent = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(getContext(), entry.getParent());
		if ( parent != null ) {		
			getPart().getContext().set(MSAnnotationDetails.class, getDetailsView());
			MSGlycanAnnotationSummary view = ContextInjectionFactory.make(MSGlycanAnnotationSummary.class, getPart().getContext());
			view.setDtpdThreadedDialog(getThreadedDialog());
			return view;
		}
		return null;
	}

	@Override
	protected boolean initSpectraView( MassSpecEntityProperty prop ) {
		try {
			int iMSLevelForSpectrum = 1;
			if ( prop.getMsLevel() != null ) {
				if ( entry.getParent().getProperty() instanceof MSAnnotationEntityProperty ) {
					MSAnnotationEntityProperty parentProp = (MSAnnotationEntityProperty) entry.getParent().getProperty();
					iMSLevelForSpectrum = parentProp.getMsLevel();
				}
			}
			spectraView = getNewSpectraView();
			if( this.getScansView() != null ) {
				MSGlycanAnnotationTableDataProcessor scansTableProcessor = (MSGlycanAnnotationTableDataProcessor) this.getScansView().getTableDataProcessor();
				( (MSGlycanAnnotationSpectraView) spectraView).setScanListTableProcessor(scansTableProcessor);				
			}
			if( ! this.getPeaksView().isEmpty() && this.getPeaksView().get(0) != null ) {
				MSGlycanAnnotationDetails msdv = (MSGlycanAnnotationDetails) getDetailsView();
				if( msdv != null ) {
					( (MSGlycanAnnotationSpectraView) spectraView).setDetailsView(msdv);
				} else {
					MSGlycanAnnotationTableDataProcessor peaksTableProcessor = (MSGlycanAnnotationTableDataProcessor) this.getPeaksView().get(0).getTableDataProcessor();
					( (MSGlycanAnnotationSpectraView) spectraView).setPeakListTableProcessor(peaksTableProcessor);
				}
			}
			( (MSGlycanAnnotationSpectraView) spectraView).setMSLevel(iMSLevelForSpectrum);
//			( (MSGlycanAnnotationSpectraView) spectraView).setScanNum(prop.getScanNum());
			//			MassSpecProperty msProperty = getMSProperty(entry);
			updateMSView(prop, spectraView);
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open spectra view", ex);
		}		
		return false;	
	}

	@Override
	protected MSGlycanAnnotationScansView getNewScansView( Entry entry, MassSpecEntityProperty entityProperty ) {
		MSAnnotationEntityProperty msProp = (MSAnnotationEntityProperty) entityProperty.clone();
		msProp.setParentScanNum( entityProperty.getScanNum() );
		msProp.setScanNum(null);
		getPart().getContext().set(MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
		getPart().getContext().set(Property.class, msProp);
		getPart().getContext().set(Entry.class, entry);
		return ContextInjectionFactory.make(MSGlycanAnnotationScansView.class, getPart().getContext());
				//new MSGlycanAnnotationScansView(getContainer(), entry, msProp, getMinMSLevel());
	}

	@Override
	protected MSGlycanAnnotationPeaksView getNewPeaksView( Entry entry, MassSpecEntityProperty entityProperty) {
		getPart().getContext().set(MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
		getPart().getContext().set(Property.class, entityProperty);
		getPart().getContext().set(Entry.class, entry);
		return ContextInjectionFactory.make(MSGlycanAnnotationPeaksView.class, getPart().getContext());
				//new MSGlycanAnnotationPeaksView( getContainer(), entry, (MSAnnotationEntityProperty) entityProperty, getMinMSLevel());
	}

	public static MSGlycanAnnotationMultiPageViewer getActiveSummaryViewer(IEclipseContext context, Entry _entry, String _sRowId) {
		EPartService partService = context.get(EPartService.class);
		for (MPart part: partService.getParts()) {
			if (part.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
				try {				
					MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) part.getObject();
					if( viewer.getEntry().getParent().equals(_entry) &&
							viewer.getSummaryView() != null && viewer.getSummaryView().getParentViewRowId().equals(_sRowId) ) {
						return viewer;
					}
				} catch( Exception ex) {
					logger.error(ex.getMessage(),ex);
				}
			}
		}
		return null;
	}

	public static MSGlycanAnnotationMultiPageViewer getActiveViewer(IEclipseContext context) {
		MPart part = (MPart) context.get(IServiceConstants.ACTIVE_PART);
		if (part != null && part.getObject() instanceof MSGlycanAnnotationMultiPageViewer)
			return (MSGlycanAnnotationMultiPageViewer) part.getObject();
		return null;
	}

	public static MSGlycanAnnotationMultiPageViewer getActiveViewerForEntry(IEclipseContext context, Entry entry ) {
		EPartService partService = context.get(EPartService.class);
		for (MPart part: partService.getParts()) {
			if (part.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
				if (((MSGlycanAnnotationMultiPageViewer)part.getObject()).getEntry().equals(entry)) {
					return (MSGlycanAnnotationMultiPageViewer)part.getObject();
				}
			}
		}
		return null;
	}

	protected MSGlycanAnnotationDetails getNewDetailsView( Entry entry, MassSpecEntityProperty entityProperty) {
		//		return new MassSpecPeaksView(entry, entityProperty);
		MSGlycanAnnotationMultiPageViewer parent = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(getContext(), entry.getParent());
		if ( parent != null ) {
			CartoonOptions cOptions = getCartoonOptions();	
			getPart().getContext().set(MSGlycanAnnotationMultiPageViewer.class, parent);
			getPart().getContext().set(Entry.class, entry);
			getPart().getContext().set(Property.class, entityProperty);
			getPart().getContext().set(CartoonOptions.class, cOptions);
			getPart().getContext().set(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
			MSGlycanAnnotationDetails view = ContextInjectionFactory.make(MSGlycanAnnotationDetails.class, getPart().getContext());
					//new MSGlycanAnnotationDetails(parent, entry, (MSAnnotationEntityProperty) entityProperty, cOptions, getMinMSLevel());
			//		view.updateFeature(entry, (MSAnnotationEntityProperty) entityProperty, cOptions);
			return view;
		}
		return null;
	}

	protected CartoonOptions getCartoonOptions() {
		try {
			MSGlycanAnnotationCartoonPreferences prefs = MSGlycanAnnotationCartoonPreferencesLoader.getCartoonPreferences();
			//			prefs.readPreference();
			CartoonOptions cartoonOptions = new CartoonOptions(
					MSGlycanAnnotationCartoonPreferences.getGWBlayoutString(prefs.getImageLayout()), 
					MSGlycanAnnotationCartoonPreferences.getGWBStyleString(prefs.getImageStyle()),
					Double.parseDouble(prefs.getImageScaleFactor()),
					MSGlycanAnnotationCartoonPreferences.getGWBOrientationCode(prefs.getOrientation()),
					prefs.isShowInfo(),
					prefs.isShowMasses(),
					prefs.isShowRedEnd());
			return cartoonOptions;
		} catch (NumberFormatException e) {
			logger.error(e.getMessage(), e);
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

	public static void showRowSelection(IEclipseContext context, Entry entry, MSAnnotationTable parentTable, int iRowNumber, int iSourceParentScanNum, String sSourceParentRowId ) {
		try {
			MSGlycanAnnotationMultiPageViewer parent = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(context, entry);
			if ( parent != null ) {
				MSGlycanAnnotationPeaksView me = null;
				Object oActiveTab = parent.getPageItem( parent.getActivePage() );
				if( oActiveTab instanceof MSGlycanAnnotationDetails ) {
					me = (MSGlycanAnnotationPeaksView) parent.getAnnotationDetails().getCurrentPeaksView();
				} else if( oActiveTab instanceof MSGlycanAnnotationPeaksView ) {
					me = (MSGlycanAnnotationPeaksView) parent.getPeaksView().get(0);
				}
				if( me == null )
					return;
				MSAnnotationSelectionView viewer = ViewRowChooserInTabCommandExecutor.showRowChooser(me, parentTable, iRowNumber, iSourceParentScanNum, sSourceParentRowId);
				me.setSelectionView(viewer);
				me.getBottomPane().layout();
			}
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected void updateColumnVisibility( MassSpecViewerPreference updatePref ) {
		super.updateColumnVisibility(updatePref);
		if( getSummaryView() != null ) {
			try {
				MSGlycanAnnotationSummary summaryView = getSummaryView();
				MassSpecTableBase viewBase = summaryView.getViewBase();
				if( viewBase == null ||  viewBase.getNatTable() == null ) {
					return;
				}
				MSGlycanAnnotationSummaryTable table = (MSGlycanAnnotationSummaryTable) viewBase.getNatTable();
				MSGlycanAnnotationSummaryViewerPreference curPref = (MSGlycanAnnotationSummaryViewerPreference) table.getGRITSTableDataObject().getTablePreferences();
				updateColumnVisibility(table, curPref, updatePref);
			} catch( Exception ex ) {
				logger.error("Error updating Summary view from editor: " + getTitle(), ex);
			}	
		}
	}

	@Optional @Inject
	public void updatePreferences(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED)
	 					String preferenceName)
	{
		if(preferenceName != null && preferenceName.startsWith(MSGlycanAnnotationViewerPreference.class.getName())) {
	 		PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);
			
				MSGlycanAnnotationViewerPreference updatePref = (MSGlycanAnnotationViewerPreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, MSGlycanAnnotationViewerPreference.class);
				this.updateColumnVisibility(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility", e);
			}
		}
		
		if(preferenceName != null && preferenceName.startsWith(MSGlycanAnnotationSummaryViewerPreference.class.getName())) {
	 		PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);
			
				MSGlycanAnnotationSummaryViewerPreference updatePref = (MSGlycanAnnotationSummaryViewerPreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, MSGlycanAnnotationSummaryViewerPreference.class);
				this.updateColumnVisibility(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility", e);
			}
		}
	}

	public static String[] getPreferencePageLabels( int _iMSLevel ) {
		if( _iMSLevel == 1 ) {
			return new String[]{"MS Scans"};
		} else if ( _iMSLevel == 2 ) {
			return new String[]{"MS Scans", "Structure Annotation", "Structure Annotation Selection"};
		} else if ( _iMSLevel == 3 ) {
			return new String[]{"MS Scans", "Details View", "Details View Selection", "Summary" };
		} else {
			return new String[]{"MS Scans", "Details View", "Details View Selection", "Summary", "Peak List" };
		}
	}

	public static FillTypes[] getPreferencePageFillTypes( int _iMSLevel ) {
		if( _iMSLevel == 1 ) {
			return new FillTypes[]{FillTypes.Scans};
		} else if ( _iMSLevel == 2 ) {
			return new FillTypes[]{FillTypes.Scans, FillTypes.PeaksWithFeatures, FillTypes.Selection};
		} else if ( _iMSLevel == 3 ) {
			return new FillTypes[]{FillTypes.Scans, FillTypes.PeaksWithFeatures, FillTypes.Selection, FillTypes.Scans};		
		} else { // yes the Summary page is Scan fill type...not sure why I did that
			return new FillTypes[]{FillTypes.Scans, FillTypes.PeaksWithFeatures, FillTypes.Selection, FillTypes.Scans, FillTypes.PeakList};
		}
	}

	public static int getPreferencePageMaxNumPages() {
		return 4;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if( event.getProperty().equals(MassSpecCustomAnnotationDialog.PROPERTY_WIN_CLOSED ) ) {
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog = null;
		}
	}
	
	/**
	 * Apply filters on the table and update the table if necessary
	 * 
	 * @param filterSetting structure filters to be applied, can be null if only score filters are to be applied
	 * @param filterKey name of the column for the score filter (intensity score or counting score), can be null if only structure filter are being applied
	 * @param numTopHits number of top hits to be selected, used in combination with filterKey
	 * @param overrideManual if true, the filters may change manual selections as well
	 * @param keepExisting if true, keep existing selections if there is no matching structures. Used only with structure filters
	 * @param highlightOnly if true, structure filters will not change selections on the table but only highlight matching candidates
	 */
	public void applyFilter (FilterSetting filterSetting, String filterKey, int numTopHits, boolean overrideManual, boolean keepExisting, boolean highlightOnly) {
		if (getPeaksView().get(0).getViewBase() == null) { // happens if this is MS3 level
			logger.warn("You cannot apply filter at this level!");
			return;
		}
		GRITSProgressDialog progressDialog = new GRITSProgressDialog(new Shell(), 1, false);
        progressDialog.open();
        progressDialog.getMajorProgressBarListener().setMaxValue(2);
        progressDialog.setGritsWorker(new GRITSWorker() {
        	MSGlycanAnnotationTable table = (MSGlycanAnnotationTable) getPeaksView().get(0).getViewBase().getNatTable();  
            @Override
            public int doWork() {
                try {
                    updateListeners("Filtering...", 1);
                    dirtyVal = false;
                    CancelableThread t = new CancelableThread() {
                        @Override
                        public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
                            try {
                                ((MSGlycanAnnotationTableDataProcessor)table.getTableDataProcessor()).setFilter(filter);
                                dirtyVal = table.startUpdateHiddenRowsAfterEdit(
                                        filterKey, numTopHits, overrideManual, filterSetting, keepExisting, highlightOnly);
                                return true;
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                return false;
                            }
                        }
                    };
                    t.setProgressThreadHandler(progressDialog);
                    progressDialog.setThread(t);
                    table.getTableDataProcessor().setProgressBarDialog(progressDialog);
                    progressDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Determinant);
                    t.start();  
                    while ( ! t.isCanceled() && ! t.isFinished() && t.isAlive() ) 
                    {
                        if (!Display.getDefault().readAndDispatch()) 
                        {
                        //    Display.getDefault().sleep();
                        }
                    }
                    if( t.isCanceled() ) {
                        t.interrupt();
                        return GRITSProcessStatus.CANCEL;
                    } 
                } catch( Exception e ) {
                    logger.error(e.getMessage(), e);
                }
                table.finishUpdateHiddenRowsAfterEdit(dirtyVal);
                updateListeners("Done", 2);
                return GRITSProcessStatus.OK;
            }
             
        });
        progressDialog.startWorker();

		//switch to structure annotation tab
        int inx = getPeaksViewsFirstTabIndex();
        int current = getActivePage();
        
        if (inx != current)
        	setActivePage(inx);
        
        // to clear the selections in the lower table
        MSGlycanAnnotationTable table = (MSGlycanAnnotationTable) getPeaksView().get(0).getViewBase().getNatTable();
        table.performMouseDown(table.getLastMouseDownRow()+1);
	}

	@Override
	protected AnnotationFilter getFilter() {
		if (this.filter == null) {
			// get the filters from the archive
			if (!getPeaksView().isEmpty()) {
				if (getPeaksView().get(0).getViewBase() != null) {
					if (((MSGlycanAnnotationTableDataProcessor)((MSGlycanAnnotationTable) getPeaksView().get(0).getViewBase().getNatTable()).
							getTableDataProcessor()).getCurScanFeature() != null)
						setFilter(((MSGlycanAnnotationTableDataProcessor)((MSGlycanAnnotationTable) getPeaksView().get(0).getViewBase().getNatTable()).
							getTableDataProcessor()).getCurScanFeature().getFilter());
				}
			}
		}
			
		return this.filter;
	}
	
	public void setFilter(AnnotationFilter filter) {
		this.filter = filter;
	}
	
	public void sendFilterChanged() {
		eventBroker.post(EVENT_TOPIC_FILTER_CHANGED, this);
	}
	
	protected Entry getNewTableCompatibleEntry(Entry parentEntry) {
		Entry newEntry = MSGlycanAnnotationEntityProperty.getTableCompatibleEntry(parentEntry);	
		return newEntry;
	}
	
	
}
