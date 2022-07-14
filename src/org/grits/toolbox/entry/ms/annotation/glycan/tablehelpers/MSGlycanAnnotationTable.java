package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConversion;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glycomedb.residuetranslator.ResidueTranslator;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationViewerPreferenceLoader;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMScan;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.command.MSGlycanAnnotationViewColumnChooserCommandHandler;
import org.grits.toolbox.entry.ms.annotation.glycan.command.ViewMSOverviewCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanAnnotationFilterVisitor;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationImageConversion;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationSummary;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.AutoResizeImagePainter;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.ExtCheckBoxPainter;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationCellOverrideLabelAccumulatorForRowHeader;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.SharedCheckboxWidget;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationDetails;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationEntityScroller;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationTableBase;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.utils.data.CartoonOptions;
import org.grits.toolbox.utils.image.GlycanImageProvider;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;
import org.grits.toolbox.utils.process.GlycoWorkbenchUtil;
import org.grits.toolbox.widgets.processDialog.ProgressBarListener;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

/**
 * Extends MSAnnotationTable with specific options for displaying glycans.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MSGlycanAnnotationImageConversion
 * @see GlycoWorkbenchUtil
 */

public class MSGlycanAnnotationTable extends MSAnnotationTable {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationTable.class);
	public static final String GLYCAN_ID_PREFIX = "GlycanID: ";

	protected MSGlycanAnnotationImageConversion imageLoader;
	private GlycoWorkbenchUtil m_gwbUtil;

	public MSGlycanAnnotationTable(MSAnnotationTableBase parent, TableDataProcessor xmlExtractor) throws Exception {
		super(parent, xmlExtractor);
		initializeGlycoWorkbench();
	}

	public MSGlycanAnnotationTable(Composite parent, MSGlycanAnnotationTable parentTable, int iParentRowIndex, int iParentScanNum, String sParentRowId ) {
		super(parent, parentTable, iParentRowIndex, iParentScanNum, sParentRowId);
		initializeGlycoWorkbench();
	}	

	public MSGlycanAnnotationTable(Composite parent, TableDataProcessor tableDataExtractor) {
		super(parent, tableDataExtractor);		
		initializeGlycoWorkbench();
	}
	
	private void initializeGlycoWorkbench() {
		Config t_objConf = new Config();
		MonosaccharideConversion t_msdb = new MonosaccharideConverter(t_objConf);
		try {
			m_gwbUtil = new GlycoWorkbenchUtil(new ResidueTranslator(), t_msdb);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void loadData() throws Exception {
		super.loadData();
		int iSuccess = performImageConversion(getShell());
		if( iSuccess == GRITSProcessStatus.ERROR ) {
			throw new Exception("Failed to convert images.");
		} else if ( iSuccess == GRITSProcessStatus.CANCEL ) {
			getTableDataProcessor().cancelWork();
			return;
		}
	}
	
	@Override
	public void createMainTable() throws Exception  {
		try {
			initCommonTableComponents();
			initColumnChooserLayer();
			registerDataUpdateHandler();
			registerEditableColumns(this.configRegistry);
			registerLockedIcons(configRegistry);
			registerFilterStyle(configRegistry, true);
			registerDoubleStyles(configRegistry);
			registerPolarityStyle(configRegistry);
			updateRowVisibilityAfterRead();
			updateEventListForVisibility();
			updateImageRegistry( false );
			bLoadedSubset = false;
			//		registerEditableCells(configRegistry);
			finishNatTable();
			performAutoResizeAfterPaint();
			initialSort();
		} catch( Exception e ) {
			logger.error("Error initializing table.", e);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	public void reInit(Composite parent, MSAnnotationTable parentTable,
			int iParentRowIndex, int iParentScanNum, String sParentRowId ) {
		super.reInit(parent, parentTable, iParentRowIndex, iParentScanNum, sParentRowId);
	}

	@Override
	public void createSubsetTable()  {
		try {
			loadDataFromParent();
			MSAnnotationViewerPreference parentPref = (MSAnnotationViewerPreference) parentTable.getPreference();
//			MSGlycanAnnotationViewerPreference pref = MSGlycanAnnotationViewerPreferenceLoader.getTableViewerPreference(parentPref.getMSLevel(), FillTypes.Selection);
			MSGlycanAnnotationViewerPreference pref = loadPreference(parentPref);
			if( pref.getPreferenceSettings() == null ) { // not initialized
				pref.setPreferenceSettings(parentPref.getPreferenceSettings());
				pref.writePreference();
			} else {
				boolean bUpdate = addUnrecognizedHeadersToSubsetTable(parentPref, pref);
				if( bUpdate ) {
					pref.writePreference();
				}
			}
			setPreference(pref);
			initCommonTableComponents();
			initColumnChooserLayer();
			//		updateImageRegistry( false );
			registerSelectedCheckbox(this.configRegistry, getCheckboxEditableRule());
			registerFilterStyle(configRegistry, false);
			registerDoubleStyles(configRegistry);
			bLoadedSubset = true;
			finishNatTable();
			performAutoResizeAfterPaint();
			parentTable.setCurrentSubsetTable(this);
			createCheckBoxPainters();
		} catch (Exception e) {
			logger.error("Failed to create subset table.", e);
		}

	}

	protected MSGlycanAnnotationViewerPreference loadPreference(MSAnnotationViewerPreference parentPref) {
		return MSGlycanAnnotationViewerPreferenceLoader.getTableViewerPreference(parentPref.getMSLevel(), FillTypes.Selection);
	}

	@Override
	protected void initColumnChooserLayer() {
		MSGlycanAnnotationViewColumnChooserCommandHandler columnChooserCommandHandler = new MSGlycanAnnotationViewColumnChooserCommandHandler( this );		
		columnGroupHeaderLayer.registerCommandHandler(columnChooserCommandHandler);		
	}
	
	@Override
	protected AbstractUiBindingConfiguration getUIBindingConfiguration() {
		return new GlycanStructureExportConfiguration(getMyTableDataObject().getCartoonCols(), this);
	}

	private MSGlycanAnnotationTableDataObject getMyTableDataObject() {
		return (MSGlycanAnnotationTableDataObject) getGRITSTableDataObject();
	}

	@Override
	protected void loadDataFromParent() {
		super.loadDataFromParent();
		setImageLoader(( (MSGlycanAnnotationTable) parentTable).getImageLoader());
	}

	public MSGlycanAnnotationImageConversion getImageLoader() {
		return imageLoader;
	}

	protected void setImageLoader(MSGlycanAnnotationImageConversion imageLoader) {
		this.imageLoader = imageLoader;
	}

	@Override
	protected MSAnnotationMultiPageViewer getParentMultiPageViewer() {
		Entry parentEntry = parentView.getEntry().getParent();
		MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(parentView.getParentEditor().getContext(), parentEntry);
		return viewer;
	}	

	@Override
	public void performAutoResizeAfterPaint() {
		this.addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(Event event) {
				performAutoResize();
				removeListener(SWT.Paint, this);
			}
		});
	}

	@Override
	protected boolean rowNeedsResize(int iRowNum) {
		if( true ) 
			return super.rowNeedsResize(iRowNum);
		// actually only resize rows that DON'T have images. Those rows have an autoimageresize config associated
		int rowPosition = LayerUtil.convertRowPosition(viewportLayer, iRowNum, dataLayer );
		boolean bHasImage = false;
		for( int j = 0; j < getMyTableDataObject().getCartoonCols().size(); j++ ) {
			int iCartoonCol = getMyTableDataObject().getCartoonCols().get(j);
			String sCartoonID = (String) getGRITSTableDataObject().getTableData().get(rowPosition).getDataRow().get(iCartoonCol);
			if ( sCartoonID == null || sCartoonID.equals("") ) 
				continue;
			bHasImage = true;
		}
		return ! bHasImage;
	}

	protected MSGlycanAnnotationImageConversion getNewImageConverter() {
		return new MSGlycanAnnotationImageConversion();
	}

	public int performImageConversion(Shell shell) {
		try {
			setImageProviderCartoonOptions();
			imageLoader = new MSGlycanAnnotationImageConversion();
			imageLoader.setSimianTableData(getMyTableDataObject());
			imageLoader.setReportName(parentView.getTitle());
			ProgressBarListener pbl = getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0);
			pbl.setMinValue(0);
			pbl.setMaxValue(getMyTableDataObject().getTableData().size());
			imageLoader.addProgressListeners(getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0));
			imageLoader.convertImages();
			return GRITSProcessStatus.OK;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return GRITSProcessStatus.ERROR;
	}

	public void setImageProviderCartoonOptions() {
		String sLayout = getMyTableDataObject().getCartoonPrefs().getImageLayout();
		String sStyle = getMyTableDataObject().getCartoonPrefs().getImageStyle();
		String sScaleFactor = getMyTableDataObject().getCartoonPrefs().getImageScaleFactor();
		String sOrientation = getMyTableDataObject().getCartoonPrefs().getOrientation();
		boolean bShowInfo = getMyTableDataObject().getCartoonPrefs().isShowInfo();
		boolean bShowMasses = getMyTableDataObject().getCartoonPrefs().isShowMasses();
		boolean bShowRedEnd = getMyTableDataObject().getCartoonPrefs().isShowRedEnd();
		CartoonOptions cartoonOptions = null;
		try {
			cartoonOptions = new CartoonOptions(MSGlycanAnnotationCartoonPreferences.getGWBlayoutString(sLayout), 
					MSGlycanAnnotationCartoonPreferences.getGWBStyleString(sStyle),
					Double.parseDouble(sScaleFactor),
					MSGlycanAnnotationCartoonPreferences.getGWBOrientationCode(sOrientation),
					bShowInfo,
					bShowMasses,
					bShowRedEnd);

		} catch (NumberFormatException e) {
			logger.error(e.getMessage(), e);
			return;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
		MSGlycanAnnotationTableDataObject.glycanImageProvider.setCartoonOptions(cartoonOptions);		
	}

	public void refreshTableImages() {
		updateImageRegistry(true);
		//		performAutoResizeAfterPaint(this);		
		finishUpdateHiddenRowsAfterEdit(false);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initCellAccumulator() {
		MSGlycanAnnotationCellOverrideLabelAccumulator cellLabelAccumulator = null;
		MSAnnotationCellOverrideLabelAccumulatorForRowHeader rowHeaderCellAccumulator = null;
		List<Integer> filterCols = getMyTableDataObject().getFilterCols();
		if ( parentTable == null ) {
			List<Integer> intensityCols = getAllIntensityColumns(null);
			Integer commentCol = null;
			if (getMyTableDataObject().getCommentCols() != null && !getMyTableDataObject().getCommentCols().isEmpty())
				commentCol = getMyTableDataObject().getCommentCols().get(0);
			Integer ratioCol = null;
			if (getMyTableDataObject().getRatioCols() != null && !getMyTableDataObject().getRatioCols().isEmpty())
				ratioCol = getMyTableDataObject().getRatioCols().get(0);
			if (filterCols != null && !filterCols.isEmpty())
				cellLabelAccumulator = new MSGlycanAnnotationCellOverrideLabelAccumulator(this.bodyDataProvider, 
					getMyTableDataObject().getCartoonCols(), filterCols.get(0), commentCol, ratioCol, intensityCols);
			else 
				cellLabelAccumulator = new MSGlycanAnnotationCellOverrideLabelAccumulator(this.bodyDataProvider, 
						getMyTableDataObject().getCartoonCols(), null, commentCol, ratioCol, intensityCols);
			rowHeaderCellAccumulator = new MSAnnotationCellOverrideLabelAccumulatorForRowHeader<>(bodyDataProvider, this, 0);
		} else {		
			List<Integer> intensityCols = getAllIntensityColumns(1);  //shift each index by 1 because of the selection column
			if (filterCols != null && !filterCols.isEmpty())
				cellLabelAccumulator = new MSGlycanAnnotationCellOverrideLabelAccumulator(this.bodyDataProvider, 
					getMyTableDataObject().getCartoonCols(), 0, filterCols.get(0), null, null, intensityCols);
			else
				cellLabelAccumulator = new MSGlycanAnnotationCellOverrideLabelAccumulator(this.bodyDataProvider, 
						getMyTableDataObject().getCartoonCols(), 0, null, null, null, intensityCols);
		}
		if (getTableDataProcessor() != null && getTableDataProcessor().getTempPreference() != null) {
			int polarityColumn = getTableDataProcessor().getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_polarity.name());
			if (polarityColumn != -1)
				cellLabelAccumulator.setPolarityColumn (polarityColumn);
		}

		dataLayer.setConfigLabelAccumulator(cellLabelAccumulator);	
		if (rowHeaderCellAccumulator != null)
			rowHeaderDataLayer.setConfigLabelAccumulator(rowHeaderCellAccumulator);
	}

	protected void initImageConfigRegistry() {
		try {
			if ( getMyTableDataObject().getCartoonCols() != null ) {
				if ( getGRITSTableDataObject().getTableData() != null && ! getGRITSTableDataObject().getTableData().isEmpty() ) {
					for(int i = 0; i < getGRITSTableDataObject().getTableData().size(); i++ ) {
						for( int j = 0; j < getMyTableDataObject().getCartoonCols().size(); j++ ) {
							Integer iCartoonCol = getMyTableDataObject().getCartoonCols().get(j);
							String sCartoonFile = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(iCartoonCol);
							if ( sCartoonFile == null || sCartoonFile.equals("") ) 
								continue;
							Integer iSequenceCol = getMyTableDataObject().getSequenceCols().get(j);
							String sSequence = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(iSequenceCol);
							if ( sSequence == null || sSequence.equals("") ) 
								continue;

							registerImage(configRegistry, sSequence, sCartoonFile);
						}
					}
				}
			}		
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	protected void initConfigRegistry() {
		super.initConfigRegistry();
		initImageConfigRegistry();
	}

	protected void updateImageRegistry( boolean bForceImageRedraw ) {	
		if ( getGRITSTableDataObject().getTableData() == null || 
				getGRITSTableDataObject().getTableData().isEmpty() || 
				getMyTableDataObject().getPeakIdCols().isEmpty() ||
				getMyTableDataObject().getCartoonCols().isEmpty() ) 
			return; // can't support multiple images in default glycan annotation viewer!
		// note: only applicable for non-merge results

		// If a merge, should I even get here?
		//		if( getMyTableDataObject().getCartoonCols().size() > 1 ) {
		//			System.out.println("Watch me");
		//		}
		for(int i = 0; i < getGRITSTableDataObject().getTableData().size(); i++ ) {
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( getMyTableDataObject().getCartoonCols().get(0) ) == null )
				continue;
			// must now do the same for the images
			for( int j = 0; j < getMyTableDataObject().getCartoonCols().size(); j++ ) {
				int iScanNum = -1;
				if( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols() != null && ! ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().isEmpty() ) {
					if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( 
							((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0)) != null )
						iScanNum = ( (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0)) ).intValue();					
				}
				int iCartoonCol = getMyTableDataObject().getCartoonCols().get(j);
				int iPeakId = -1;
				String sCartoonID = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(iCartoonCol);
				if ( sCartoonID == null || sCartoonID.equals("") ) 
					continue;
				Integer iSequenceCol = getMyTableDataObject().getSequenceCols().get(j);
				String sSequence = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(iSequenceCol);
				if ( getMyTableDataObject().getCartoonCols().size() == getMyTableDataObject().getPeakIdCols().size() ) { // probably size=1 for single SimGlgetMyTableDataObject()taObject) getSimDataObject()).getPeakIdCols().get(j);
					iPeakId = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow().get( getMyTableDataObject().getPeakIdCols().get(j) );
				} 
				// iterating over the table data object, so get scan number passing the table data object				
				Integer iParentScanNum = getScanNumberForVisibility(getMyTableDataObject(), i);
				boolean bInvisible = getGRITSTableDataObject().isInvisibleRow(iParentScanNum, Integer.toString(iPeakId));
				Object ob = configRegistry.getSpecificConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
				if ( bForceImageRedraw ) {
					configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
					registerImage(configRegistry, sSequence, sCartoonID);							
				} else {
					if ( ob == null && ! bInvisible )
						registerImage(configRegistry, sSequence, sCartoonID);
					else if ( ob != null && bInvisible )
						configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
				}
			}
			/* 
			 * DBW 10/03/16: This just wasn't working right yet. It would display cartoons so long as you didn't rearrange columns.
			 * Saving for later if desired
			 *

			for( int j = 0; j < getMyTableDataObject().getExtraCartoonCols().size(); j++ ) {
				int iSequenceCol = getMyTableDataObject().getExtraCartoonCols().get(j);
				int iPeakId = -1;
				String sCartoonID = (String) getSimDataObject().getTableData().get(i).getDataRow().get(iSequenceCol);
				if ( sCartoonID == null || sCartoonID.equals("") ) 
					continue;
				if ( getMyTableDataObject().getCartoonCols().size() == getMyTableDataObject().getPeakIdCols().size() ) { // probably size=1 for single SimGlgetMyTableDataObject()taObject) getSimDataObject()).getPeakIdCols().get(j);
					iPeakId = (Integer) getSimDataObject().getTableData().get(i).getDataRow().get( getMyTableDataObject().getPeakIdCols().get(0) );
				} 

				String sSequence = sCartoonID;
				int iInx = ((String) sSequence).indexOf(".png");
				if( iInx > 0 ) {
					sSequence = ((String) sSequence).substring(0, iInx);
				}
				boolean bInvisible = getSimDataObject().isInvisibleRow(iPeakId);
				Object ob = configRegistry.getSpecificConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
				if ( bForceImageRedraw ) {
					configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
					registerImage(configRegistry, sSequence, sCartoonID);							
				} else {
					if ( ob == null && ! bInvisible )
						registerImage(configRegistry, sSequence, sCartoonID);
					else if ( ob != null && bInvisible )
						configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, sCartoonID);
				}
			}
			*/
		}
	}


	protected void registerImage(ConfigRegistry configRegistry, String sequence, String imgName) {
		try {
			GlycanImageObject image = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(sequence);

			AutoResizeImagePainter imgPainter = new AutoResizeImagePainter(image, true, 5, true);
			Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, imgName);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, imgPainter, DisplayMode.NORMAL, imgName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected void createCheckBoxPainters() {
		MSGlycanAnnotationTableDataObject tdo = (MSGlycanAnnotationTableDataObject) getGRITSTableDataObject();
		SharedCheckboxWidget scw = getSharedCheckboxWidget();
		scw.getHtGlycanToCheckBox().clear();
		int iNumRows = tdo.getTableData().size();
		for( int i = 0; i < iNumRows; i++ ) {
			for( int j = 0; j < tdo.getCartoonCols().size(); j++ ) {
				Integer iFeatureCol = tdo.getFeatureIdCols().get(j);
				Integer iPeakCol = tdo.getPeakIdCols().get(j);
				Integer iAnnotCol = tdo.getAnnotationIdCols().get(j);
				if ( iFeatureCol == null || iPeakCol == null || iAnnotCol == null ) 
					continue;
				Integer iPeakId = (Integer) tdo.getTableData().get(i).getDataRow().get(iPeakCol);
				String sFeature = (String) tdo.getTableData().get(i).getDataRow().get(iFeatureCol);
				Integer iAnnotId = (Integer) tdo.getTableData().get(i).getDataRow().get(iAnnotCol);
				if ( iPeakId == null || sFeature == null || iAnnotId == null ) 
					continue;
				Annotation annot = ( (MSAnnotationTableDataProcessor) getTableDataProcessor()).getAnnotation(iAnnotId);
				String sHeaderKey = MSAnnotationEntityScroller.getCombinedKeyForLookup( iPeakId, sFeature );	
				// we know this is a subset table, so first column is the selected value
				//				Boolean bCurStatus = tdo.isHiddenRow(iPeakId, sFeature) || tdo.isInvisibleRow(iPeakId);
				Boolean bCurStatus = (Boolean) tdo.getTableData().get(i).getDataRow().get(0); // subset table, so we know that this is boolean
				MSAnnotationEntityProperty prop = (MSAnnotationEntityProperty) ( (MSAnnotationTableDataProcessor) getTableDataProcessor()).getSourceProperty();
				String sLabel = MSAnnotationDetails.getLabelForCheckbox(getAnnotationStructureId(annot), sFeature, (prop.getMsLevel()+1));
				ExtCheckBoxPainter ecbp = getSharedCheckboxWidget().createCheckBoxPainter(sHeaderKey, sLabel, bCurStatus);
			}
		}
	}

	// 04/10/2018 MASAAKI
	protected String getAnnotationStructureId(Annotation annot) {
		return GLYCAN_ID_PREFIX + ((GlycanAnnotation)annot).getStringId();
	}

	@Override
	public void mouseDown(MouseEvent e) {
		super.mouseDown(e);
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		super.mouseDoubleClick(e);
	}

	@Override
	public void finishUpdateHiddenRowsAfterEdit( boolean isDirty ) {
		highlightRows();
		updateImageRegistry(false);
		super.finishUpdateHiddenRowsAfterEdit(isDirty);
		if( ! isDirty ) 
			return;
		MSAnnotationTable subsetTable = getCurrentSubsetTable();
		if( subsetTable == null ) 
			return;
		MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveSummaryViewer(parentView.getParentEditor().getContext(), 
				parentView.getEntry(), subsetTable.getParentTableRowId());
		if( viewer != null ) {
			MSGlycanAnnotationSummary summaryViewer = (MSGlycanAnnotationSummary) viewer.getSummaryView();			
			summaryViewer.setParentSubsetTableData(getCurrentSubsetTable(), getCurrentRowIndex(), getCurrentParentScanNum(), getCurrentRowId());
			MSGlycanAnnotationSummaryTable summaryTable = (MSGlycanAnnotationSummaryTable) viewer.getSummaryView().getViewBase().getNatTable();
			summaryTable.updateHeaderCheckboxRegistry();
		}
	}


	@Override
	public boolean startUpdateHiddenRowsAfterEdit( MSAnnotationTable subsetTable ) {
		boolean bIsDirty = super.startUpdateHiddenRowsAfterEdit(subsetTable);
		MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveSummaryViewer(parentView.getParentEditor().getContext(),
				parentView.getEntry(), 
				subsetTable.getParentTableRowId());
		if( bIsDirty && viewer != null ) {
			viewer.getSummaryView().setParentSubsetTableData(subsetTable, getCurrentRowIndex(), getCurrentParentScanNum(), getCurrentRowId());			
		}
		return bIsDirty;
	}

	@Override
	protected void openRowSelection(int iSourceRowIndex, int iSourceParentScanNum, String sSourceParentRowId ) {
		MSGlycanAnnotationMultiPageViewer.showRowSelection(parentView.getParentEditor().getContext(), parentView.getEntry(), 
				this, iSourceRowIndex, iSourceParentScanNum, sSourceParentRowId);		
	}

	@Override
	protected void showMSOverview(Entry newEntry) {
		ViewMSOverviewCommandExecutor.showMSOverview(parentView.getParentEditor().getContext(), newEntry);		
	}

	
	
	/**
	 * apply the filter on the table and make necessary changes to the selection status for the rows
	 * according to filter match results. if overrideManual is false, it will not change the manual selections even 
	 * if the row fails to match the filter
	 *
	 * returns whether the table is modified or not
	 * 
	 * @param filter
	 * @param overrideManual
	 * @param highlightOnly if true do not change selections, only mark the row to be highlighted
	 * @return true if filter caused selection changes on the table, false if nothing has changed
	 * @throws Exception
	 */
	@Override
	protected boolean applyFilter(Object filter, boolean _bOverrideManual, boolean highlightOnly) throws Exception {
		if (!(filter instanceof FilterSetting))
			return false;
		
		int iNumRows =  getBottomDataLayer().getRowCount();
		if ( iNumRows == 0 )
			return false;
		
		MSAnnotationTableDataProcessor tdp = (MSAnnotationTableDataProcessor) getTableDataProcessor();
		boolean isDirty = false;
		getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0).setMaxValue(iNumRows);
	    getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0).setProgressValue(0);
		for( int i = 0; i < iNumRows && !getTableDataProcessor().getProgressBarDialog().isCanceled(); i++ ) {
			getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0).setProgressValue(i+1);
	        getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0).setProgressMessage("Filtering row: " + (i+1) + " of " + iNumRows);
			if ( getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getParentNoCol().get(0) , i) == null )
				continue;
			if ( getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0) , i) == null )
				continue;
			if ( getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0) , i) == null )
				continue;
			
			// iterating over the nattable bottom layer, so get scan number passing self
			Integer iParentScanNo = getScanNumberForVisibility(this, i);
			//Integer scanNo = (Integer) getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0), i);
			Integer iPeakId = ( (Integer) getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0) , i) );
			Integer iScan = null;
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow()
					.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0) ) != null ) {
				iScan = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
						.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0));
			}
			String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
			boolean bLocked = getGRITSTableDataObject().isLockedPeak(iParentScanNo, sRowId);
			if (bLocked)  // do not change selections if it is locked
				if (!highlightOnly) // highlightOnly does not change selections and should not care if it is locked
					continue;
			boolean bManuallyChanged = getGRITSTableDataObject().isManuallyChangedPeak(iParentScanNo, sRowId);
			if( bManuallyChanged && ! _bOverrideManual )
				if (!highlightOnly) // highlightOnly does not change selections and should not care if it is manually changed
					continue;
			
			String sId = getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0), i).toString();			
			boolean bPrevInvisible = getGRITSTableDataObject().isInvisibleRow(iParentScanNo, sRowId);
			boolean bPrevHidden = getGRITSTableDataObject().isHiddenRow(iParentScanNo, sRowId, sId);
			int sequenceCol = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getSequenceCols().get(0);
			Integer filterCol = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().get(0);
			String sequence = (String) getBottomDataLayer().getDataValueByPosition(sequenceCol, i);
			boolean bCurHidden = !passesFilters(sequence, (FilterSetting)filter, i);
			if ( ! bCurHidden ) {
				if (highlightOnly) {
					// passed the filters, mark the row as such
					if (filterCol != -1) {
						GRITSListDataRow backendRowData = this.bodyDataProvider.getGRITSListDataRow(i);
						int j = this.getSourceIndexFromRowId(backendRowData.getId());
						GRITSListDataRow rowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(j);
						rowData.getDataRow().set(filterCol, 10);
					}
				} else {
					getGRITSTableDataObject().removeHiddenRow(iParentScanNo, sRowId, sId);
					if ( bPrevInvisible || bPrevHidden ) {
						isDirty = true;
						tdp.addDirtyParentScan(iParentScanNo);
					}
				}
			} else if ( bCurHidden ) {
				if (highlightOnly) {
					// // failed  the filters, mark the row as such
					if (filterCol != -1) {
						GRITSListDataRow backendRowData = this.bodyDataProvider.getGRITSListDataRow(i);
						int j = this.getSourceIndexFromRowId(backendRowData.getId());
						GRITSListDataRow rowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(j);
						rowData.getDataRow().set(filterCol, 0);
					}
				} else {
					getGRITSTableDataObject().setHiddenRow(iParentScanNo, sRowId, sId);
					if ( ! bPrevHidden || ! bPrevInvisible ) {
						isDirty = true;
						tdp.addDirtyParentScan(iParentScanNo);
					}
				}
			}
		}
		getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0).setProgressValue(iNumRows);
        getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0).setProgressMessage("Done!");
		if (getTableDataProcessor().getProgressBarDialog().isCanceled())
			return false; // no updates should be done to the table if the filter has been canceled
		return isDirty;
	}

	/**
	 * apply the given filter to the provided sequence (may also apply custom filters to filter by other column values in the table) 
	 * 
	 * @param sequence GWB sequence
	 * @param filterSetting filters to apply
	 * @param rowId the row index of the table to apply the filter to (required for custom filters)
	 * @return true if the given sequence (and/or row) matches the filter
	 * @throws Exception
	 */
	private boolean passesFilters(String sequence, FilterSetting filterSetting, int rowId) throws Exception {
		if (sequence != null && !sequence.isEmpty() && filterSetting != null) {
			// use own filter visitor to be able to apply custom filters
			MSGlycanAnnotationFilterVisitor filterVisitor = new MSGlycanAnnotationFilterVisitor();
			filterVisitor.setFilter(filterSetting.getFilter());
			
			int iInx1 = sequence.indexOf(GlycanImageProvider.COMBO_SEQUENCE_SEPARATOR);
			if (iInx1 < 0) {  // single sequence
				m_gwbUtil.parseGWSSequence(sequence);
				Glycan glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
				return filterVisitor.evaluate(this, rowId, glycan.toSugar());
			//	return GlycanFilterOperator.evaluate(glycan.toSugar(), filterSetting.getFilter());
			} else { // if any of the sequences passes the filter, return true
				String sRemaining = sequence;
				do {
					String sSeq = iInx1 > 0 ? sRemaining.substring(0, iInx1) : sRemaining;
					m_gwbUtil.parseGWSSequence(sSeq);
					Glycan glycan = m_gwbUtil.getGlycoWorkbenchGlycan();
					if (filterVisitor.evaluate(this, rowId, glycan.toSugar()))
					//if (GlycanFilterOperator.evaluate(glycan.toSugar(), filterSetting.getFilter()))  // even if one satisfies the filter, the row will be included
						return true;
					sRemaining = iInx1 > 0 ? sRemaining.substring(iInx1	+ GlycanImageProvider.COMBO_SEQUENCE_SEPARATOR.length()) : null;
					iInx1 = sRemaining != null ? sRemaining.indexOf(GlycanImageProvider.COMBO_SEQUENCE_SEPARATOR) : -1;
				} while (sRemaining != null);
			}
		} 
		// for now allow empty sequences (unannotated rows) to pass the filter
		return true;
	}
	
	@Override
	protected Entry getNewTableCompatibleEntry(Entry parentEntry) {
		Entry newEntry = MSGlycanAnnotationEntityProperty.getTableCompatibleEntry(parentEntry);	
		return newEntry;
	}


}
