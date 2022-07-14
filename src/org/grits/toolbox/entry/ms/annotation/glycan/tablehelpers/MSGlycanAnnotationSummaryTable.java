package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.stack.ColumnGroupBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationSummaryViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationSummaryTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanAnnotation;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.GRITSColumnHeaderDataProvider;
import org.grits.toolbox.display.control.table.tablecore.GRITSHeaderMenuConfiguration;
import org.grits.toolbox.display.control.table.tablecore.GRITSNatTableStyleConfiguration;
import org.grits.toolbox.display.control.table.tablecore.GRITSSingleClickConfiguration;
import org.grits.toolbox.entry.ms.annotation.glycan.command.MSGlycanAnnotationSummaryViewColumnChooserCommandHandler;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationImageConversion;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationSummaryImageConversion;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationSummary;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.AutoResizeImagePainter;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.ExtCheckBoxPainter;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.SharedCheckboxWidget;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationTableBase;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;
import org.grits.toolbox.widgets.processDialog.ProgressBarWithErrorListener;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

/**
 * Extends MSGlycanAnnotationTable with specific options for glycans according to "Summary" view
 * Summary view places the structures in the column headers w/ selection checkboxes. The user can click
 * the checkboxes to alter the selections in the parent "Structure Annotation" table.
 * The table on the Summary page contains all of the fragment ions for that MSn spectra w/ fragment annotations
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see GRITSColumnHeaderDataProvider
 * @see MSGlycanAnnotationSummaryTopHeaderOverrideLabelAccumulator
 */

public class MSGlycanAnnotationSummaryTable extends MSGlycanAnnotationTable {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSummaryTable.class);
	protected DataLayer topColumnHeaderDataLayer = null;
	protected GRITSColumnHeaderDataProvider topColumnHeaderDataProvider = null;
	private boolean bHideCommon = false;

	@SuppressWarnings("rawtypes")
	protected MSGlycanAnnotationSummaryTopHeaderOverrideLabelAccumulator groupedAccumulator = null;
	private GRITSSingleClickConfiguration sortConfiguration;

	public MSGlycanAnnotationSummaryTable(MSAnnotationTableBase parent, TableDataProcessor xmlExtractor) throws Exception {
		super(parent, xmlExtractor);
	}

	public MSGlycanAnnotationSummaryTable(Composite parent, TableDataProcessor tableDataExtractor) {
		super(parent, tableDataExtractor);		
	}

	private MSGlycanAnnotationSummaryTableDataObject getMyTableDataObject() {
		return (MSGlycanAnnotationSummaryTableDataObject) getGRITSTableDataObject();
	}

	@Override
	public void initCellAccumulator() {
		super.initCellAccumulator();
		setSummaryAccumulator();
	}

	@Override
	protected void finishNatTable() {		
		super.finishNatTable();
	}

	@Override
	protected MSGlycanAnnotationImageConversion getNewImageConverter() {
		return new MSGlycanAnnotationSummaryImageConversion();
	}

	@Override
	public MassSpecViewerPreference getPreference() {
		return super.getPreference();
	}

	@Override
	public void setPreference(MassSpecViewerPreference preference) {
		super.setPreference(preference);
	}
	
	@Override
	protected GRITSHeaderMenuConfiguration getNewHeaderMenuConfiguration() {
		return new MSGlycanAnnotationSummaryHeaderMenuConfiguration(this);
	}
	
	@Override
	protected IConfiguration getSingleClickConfiguration() {
		sortConfiguration = new GRITSSingleClickConfiguration(getGRITSTableDataObject().getTableHeader().size() > 1) {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				super.configureRegistry(configRegistry);
				for(int i = 0; i < getGRITSTableDataObject().getLastHeader().size(); i++ ) {
					GRITSColumnHeader header = getGRITSTableDataObject().getLastHeader().get(i);
					if( header.getLabel().endsWith(".png") ) {
						GlycanImageObject image = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(header.getKeyValue());
						Object ob = configRegistry.getSpecificConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, SORT_DOWN_CONFIG_TYPE);
						if ( ob != null ) {
							configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, SORT_DOWN_CONFIG_TYPE);
							configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, SORT_UP_CONFIG_TYPE);
						}
						AutoResizeImagePainter imgPainter = new AutoResizeImagePainter(image, true, 5, true);
						configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, imgPainter, DisplayMode.NORMAL, header.getLabel());
					} 
				}
			}
		};
		return sortConfiguration;
	}

	@Override
	public TableViewerColumnSettings getPreferenceSettingsFromCurrentView() {
		if ( this.columnGroupModel == null || this.columnGroupModel .isEmpty() ) 
			return super.getPreferenceSettingsFromCurrentView();

		if ( getMyTableDataObject().getFirstGroupIndices() == null )
			getMyTableDataObject().discoverGroups(this.columnGroupModel);

		TableViewerColumnSettings newEntity = new TableViewerColumnSettings();
		if (getMyTableDataObject().getFirstGroupIndices().size() < 2) {
			logger.log(Level.WARN, "Not enough column groups to update visibility");
			return newEntity;
		}

		int iPos = 0;
		for (int i = 0; i < 2; i++) {
			ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(i));
			List<Integer> members = group.getMembers();
			for (int j = 0; j < members.size(); j++) {
				int iColLayerInx = members.get(j);
				int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);

				GRITSColumnHeader header = (GRITSColumnHeader) this.columnHeaderDataLayer.getDataValueByPosition(iColLayerPos, 0);
				newEntity.setVisColInx(header, iPos++);
			}
		}
		return newEntity;
	}

	@Override
	// updates the positions in the nattable based on current preferences
	public boolean updateViewFromPreferenceSettings() {
		try {
			if ( this.columnGroupModel == null || this.columnGroupModel.isEmpty() ) {
				return super.updateViewFromPreferenceSettings();
			}
			if (this.columnHeaderDataLayer == null || this.columnHeaderDataLayer.getColumnCount() == 0)
				return false;
			int iNumCols = this.columnHeaderDataLayer.getColumnCount();
			if (iNumCols == 0)
				return false;

			this.columnHideShowLayer.showAllColumns(); // first show all columns
			ArrayList<Integer> alHiddenCols = new ArrayList<Integer>();
			if ( getMyTableDataObject().getFirstGroupIndices() == null )
				getMyTableDataObject().discoverGroups(this.columnGroupModel);

			for( int i = 0; i < getMyTableDataObject().getFirstGroupIndices().size(); i++ ) {
				ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(i));  // change 07/31/2013. No longer using "Exp" text so relying on order
				List<Integer> members = group.getMembers();
				for( int j = 0; j < members.size(); j++ ) {  // I believe members are index based
					int iColLayerInx = members.get(j);
					int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
					String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColLayerPos, 0);
					String sHeaderLable = this.columnHeaderDataLayer.getDataValueByPosition(iColLayerPos, 0).toString();
					if( sHeaderLable.endsWith(".png" ) ) {
						sHeaderKey = DMGlycanAnnotation.glycan_annotation_glycancartoon.name();
					}
					int iColShowLayerPos = LayerUtil.convertColumnPosition(this.columnHeaderDataLayer, iColLayerPos, this.columnHideShowLayer);
					if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings()
							.hasColumn(sHeaderKey)) {
						int iPrefColPos = getGRITSTableDataObject().getTablePreferences()
								.getPreferenceSettings()
								.getVisColInx(sHeaderKey);
						if ( iPrefColPos == -1 ) {
							alHiddenCols.add(iColShowLayerPos);
						} 
					} else { // not there????
						throw new Exception("Header: " + sHeaderKey + " not found in preferences!");
					}
				}
			}
			this.columnHideShowLayer.hideColumnPositions(alHiddenCols);
			Object selCell = getGRITSTableDataObject().getLastHeader().get(0);
			boolean bAddSelect = selCell.equals(TableDataProcessor.selColHeader);

			int iNumVisFirstGroup = 0;
			for( int i = 0; i < getMyTableDataObject().getFirstGroupIndices().size(); i++ ) {
				ColumnGroup group = this.columnGroupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(i));  // change 07/31/2013. No longer using "Exp" text so relying on order
				int iNumNonHidden = 0;
				List<Integer> members = group.getMembers();
				for( int j = 0; j < members.size(); j++ ) {  // I believe members are index based
					int iColLayerInx = members.get(j);
					int iColLayerPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
					String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColLayerPos, 0);
					String sHeaderLable = this.columnHeaderDataLayer.getDataValueByPosition(iColLayerPos, 0).toString();
					if( sHeaderLable.endsWith(".png" ) ) {
						sHeaderKey = DMGlycanAnnotation.glycan_annotation_glycancartoon.name();
					}
					if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings()
							.hasColumn(sHeaderKey)) {
						int iPrefColPos = getGRITSTableDataObject().getTablePreferences()
								.getPreferenceSettings()
								.getVisColInx(sHeaderKey);
						if ( iPrefColPos != -1 ) {
							iNumNonHidden++;
						} 
						if ( i == 0 ) {
							iNumVisFirstGroup++;
						}
					}			
				}
				doReorderForExpGroup(this.columnHeaderDataLayer, this.columnHideShowLayer, group, i == 0 ? 0 : iNumVisFirstGroup, iNumNonHidden, i, bAddSelect);
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		setHideUnannotated( ((MSGlycanAnnotationSummaryViewerPreference) getPreference()).isHideUnannotatedPeaks() );
		setHideCommonFragments( ((MSGlycanAnnotationSummaryViewerPreference) getPreference()).isHideCommonFragments() );
		return true;
	}

	private void doReorderForExpGroup( DataLayer columnLayer, ColumnHideShowLayer columnShowLayer, ColumnGroup group, 
			int iNumFirstGroup, int iNumNonHidden,
			int iGroupNum, boolean bAddSelect  ) {
		List<Integer> members = group.getMembers();
		int iAdder = iGroupNum > 0 ? (iGroupNum - 1) * iNumNonHidden : 0;
		iAdder += iNumFirstGroup;
		for (int iPrefColPos = 0; iPrefColPos < iNumNonHidden; iPrefColPos++) { // going in position order of the new PREFERENCES
			GRITSColumnHeader prefHeader = getGRITSTableDataObject()
					.getTablePreferences().getPreferenceSettings()
					.getColumnAtVisColInx(iPrefColPos + iNumFirstGroup);
			if ( prefHeader == null )
				continue;
			for (int iMemInx = 0; iMemInx < members.size(); iMemInx++) { // column index based
				int iColLayerInx = members.get(iMemInx);
				int iColPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColLayerInx);
				int iFromPos = LayerUtil.convertColumnPosition(
						this.columnHeaderDataLayer, iColPos,
						this.columnHideShowLayer);
				String sThisHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
				if (prefHeader.getKeyValue().equals(sThisHeaderKey)) {
					int iToPos = iPrefColPos + iAdder;
					if (bAddSelect)
						iToPos++;
					if (iFromPos + iAdder != iToPos) {
						ColumnReorderCommand command = new ColumnReorderCommand(
								this.columnHideShowLayer, iFromPos, iToPos);
						//						 System.out.println("Moving " + sThisHeaderKey + " from "
						//						 + (iFromPos + iAdder) + " to " + iToPos );
						this.columnHideShowLayer.doCommand(command);
					} else {
						//						 System.out.println("Staying put: " + sThisHeaderKey +
						//						 " from " + (iFromPos + iAdder) + " to " + iToPos );
					}
					break;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable#isHiddenTableRow(int)
	 */
	@Override
	protected boolean isHiddenTableRow(int iRowNum) {
		boolean bHidden = super.isHiddenTableRow(iRowNum);
		if( bHidden ) {
			return true;
		}
		if( ! getHideCommonFragments() ) {
			return false;
		}
		MSGlycanAnnotationSummaryTableDataObject tdo =  (MSGlycanAnnotationSummaryTableDataObject) getMyTableDataObject();
		GRITSListDataRow row = tdo.getTableData().get(iRowNum);
		
		if ( tdo.getFeatureIdCols().size() <= 1  )
			return false;
		for( int iFeatColInx = 0; iFeatColInx < tdo.getFeatureIdCols().size(); iFeatColInx++ ) {
			int iFeatCol = tdo.getFeatureIdCols().get(iFeatColInx);
			Object oVal = row.getDataRow().get(iFeatCol);
			if( oVal == null || oVal.toString().trim().equals("") ) {
				return false;
			}
		}
		return true;
	}	

	@Override
	// updates preference object based on current column settings in nattable
	public void updatePreferenceSettingsFromCurrentView() {
		if ( this.columnGroupModel == null || this.columnGroupModel.isEmpty() ) {
			super.updatePreferenceSettingsFromCurrentView();
			return;
		}
		if (this.columnHeaderDataLayer == null || this.columnHeaderDataLayer.getColumnCount() == 0)
			return;

		if ( getMyTableDataObject().getFirstGroupIndices() == null )
			getMyTableDataObject().discoverGroups(this.columnGroupModel);

		setVisibilityOfGroups(this.columnHeaderDataLayer, this.columnHideShowLayer, this.columnGroupModel, 0);
		setVisibilityOfGroups(this.columnHeaderDataLayer, this.columnHideShowLayer, this.columnGroupModel, 1);
		((MSGlycanAnnotationSummaryViewerPreference) getPreference()).setHideUnannotatedPeaks(hideUnannotated());
		((MSGlycanAnnotationSummaryViewerPreference) getPreference()).setHideCommonFragments(getHideCommonFragments());

	}	

	private void setVisibilityOfGroups(DataLayer columnLayer, 
			ColumnHideShowLayer columnShowLayer, ColumnGroupModel groupModel, int iGroupNum ) {
		// i suppose it is possible for users to rearrange the groups and put the "first" group somewhere else. Well, we 
		// aren't supporting that. the first groups will always be first, thus I will order them separately
		//		int iAdder = iGroupNum > 0 ? groupModel.getColumnGroupByIndex(0).getSize() : 0;
		int iAdder = 0;
		if( iGroupNum > 0 ) {
			ColumnGroup group = groupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(0));	
			List<Integer> members = group.getMembers();
			//			int iNewNumCols = 0;
			for( int iMemInx = 0; iMemInx < members.size(); iMemInx++ ) {
				int iColInx = members.get(iMemInx);
				boolean bHidden = columnShowLayer.isColumnIndexHidden(iColInx);	
				if( ! bHidden ) {
					iAdder++;
				}
			}

		}

		ColumnGroup group = groupModel.getColumnGroupByIndex(getMyTableDataObject().getFirstGroupIndices().get(iGroupNum));	
		List<Integer> members = group.getMembers();
		int iNewNumCols = 0;
		for( int iMemInx = 0; iMemInx < members.size(); iMemInx++ ) {
			int iColInx = members.get(iMemInx);
			boolean bHidden = columnShowLayer.isColumnIndexHidden(iColInx);	
			if( ! bHidden ) {
				iNewNumCols++;
			}
		}

		// now iterate over the visible columns using the columnshowlayer and
		// set the preference value
		int iToPos = iAdder;
		for (int iVisPos = 0; iVisPos < iNewNumCols; iVisPos++) { // position
			// based on the column show  header layer
			int iColPos = LayerUtil.convertColumnPosition(
					this.columnHideShowLayer, iVisPos + iAdder,
					this.columnHeaderDataLayer);
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
			String sHeaderLable = this.columnHeaderDataLayer.getDataValueByPosition(iColPos, 0).toString();
			if( sHeaderLable.endsWith(".png" ) ) {
				sHeaderKey = DMGlycanAnnotation.glycan_annotation_glycancartoon.name();
			}
			if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().hasColumn(sHeaderKey)) {
				GRITSColumnHeader header = getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().getColumnHeader(sHeaderKey);
				getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().setVisColInx(header, iToPos++);
			}
		}

		for (int iMemInx = 0; iMemInx < members.size(); iMemInx++) { // index based
			// off of column layer (all data)
			int iColInx = members.get(iMemInx);
			boolean bHidden = columnShowLayer.isColumnIndexHidden(iColInx);	
			if (!bHidden)
				continue;
			int iColPos = this.columnHeaderDataLayer.getColumnPositionByIndex(iColInx);
			String sHeaderKey = this.columnHeaderDataProvider.getDataKey(iColPos, 0);
			String sHeaderLable = this.columnHeaderDataLayer.getDataValueByPosition(iColPos, 0).toString();
			if( sHeaderLable.endsWith(".png" ) ) {
				sHeaderKey = DMGlycanAnnotation.glycan_annotation_glycancartoon.name();
			}
			if (getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().hasColumn(sHeaderKey)) {
				GRITSColumnHeader header = getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().getColumnHeader(sHeaderKey);
				getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().setVisColInx(header, -1);
			}
		}
	}

	public void setSummaryAccumulator() {
		if( parentTable == null && getColumnGroupHeaderLayer() != null ) {
			MSGlycanAnnotationSummaryLastHeaderOverrideLabelAccumulator ola = new MSGlycanAnnotationSummaryLastHeaderOverrideLabelAccumulator(
					columnHeaderDataLayer, 
					getMyTableDataObject().getCartoonCols());
			this.columnHeaderDataLayer.setConfigLabelAccumulator(ola);
			groupedAccumulator = new MSGlycanAnnotationSummaryTopHeaderOverrideLabelAccumulator(
					topColumnHeaderDataLayer, columnGroupHeaderLayer);
			this.topColumnHeaderDataLayer.setConfigLabelAccumulator(groupedAccumulator);
		}		
	}

	@Override
	public void initColumnHeaderLayer() {
		// columnHeaderLayer
		columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer,
				viewportLayer, selectionLayer);
	}
	
	@Override
	public void initColumnHeaderDataLayer() {
		columnHeaderDataProvider = new GRITSColumnHeaderDataProvider(getGRITSTableDataObject().getLastHeader());
		columnHeaderDataLayer = new SummaryColumnHeaderDataLayer(columnHeaderDataProvider);
		topColumnHeaderDataProvider = new GRITSColumnHeaderDataProvider(getGRITSTableDataObject().getTableHeader().get(0));
		topColumnHeaderDataLayer = new SummaryColumnHeaderDataLayer(topColumnHeaderDataProvider);
	}

	@Override
	public void createMainTable() throws Exception  {
		try {
			initCommonTableComponents();
			initColumnChooserLayer();

			registerDoubleStyles(configRegistry);
			updateRowVisibilityAfterRead();
			updateEventListForVisibility();
			updateImageRegistry( false );

			//		registerEditableCells(configRegistry);
			finishNatTable();
			performAutoResizeAfterPaint();
		} catch( Exception e ) {
			logger.error("Error initializing table.", e);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	protected void initColumnGroupHeaderLayer() {		
		columnGroupModel = new ColumnGroupModel();
		ColumnGroupBodyLayerStack bodyLayer = new ColumnGroupBodyLayerStack(
				this.dataLayer, columnGroupModel);

		columnGroupHeaderLayer = new MSGlycanAnnotationExtColumnGroupHeaderLayer(columnHeaderLayer,
				bodyLayer.getSelectionLayer(), topColumnHeaderDataLayer, columnGroupModel);
	}

	@Override
	protected void initColumnChooserLayer() {
		MSGlycanAnnotationSummaryViewColumnChooserCommandHandler columnChooserCommandHandler = new MSGlycanAnnotationSummaryViewColumnChooserCommandHandler(this );		
		columnGroupHeaderLayer.registerCommandHandler(columnChooserCommandHandler);		
	}

	@Override
	protected void initGridLayer() {
		super.initGridLayer();
		updateHeaderImageRegistry();
		updateHeaderCheckboxRegistry();		
	}


	@Override
	protected void initConfigRegistry() {
		super.initConfigRegistry();
	}

	@Override
	public void refreshTableImages() {
		updateHeaderImageRegistry();
		((SummaryColumnHeaderDataLayer) columnHeaderDataLayer).calculateRowHeight();
		super.refreshTableImages();
	}

	public int performImageConversion(Shell shell) {
		try {
			ProgressBarWithErrorListener pblMajor = getTableDataProcessor().getProgressBarDialog().getMajorProgressBarListener();
			String sDialogText = pblMajor.getCurText();
			int iDialogValue = pblMajor.getCurValue();

			pblMajor.setProgressMessage(sDialogText + ", step 1 of 2");
			super.performImageConversion(shell);
			pblMajor.setProgressValue(iDialogValue + 1);

			pblMajor.setProgressMessage(sDialogText + ", step 2 of 2");
			setImageProviderCartoonOptions();
			imageLoader = new MSGlycanAnnotationSummaryImageConversion();
			imageLoader.setSimianTableData(getMyTableDataObject());
			imageLoader.setReportName(parentView.getTitle());
			ProgressBarWithErrorListener pbl = getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0);
			pbl.setMinValue(0);
			pbl.setMaxValue(getMyTableDataObject().getLastHeader().size());
			imageLoader.addProgressListeners(getTableDataProcessor().getProgressBarDialog().getMinorProgressBarListener(0));
			imageLoader.convertImages();
			pblMajor.setProgressValue(iDialogValue + 2);
			return GRITSProcessStatus.OK;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return GRITSProcessStatus.ERROR;
	}

	@Override
	public void performAutoResize() {
		super.performAutoResize();
		for( int i = 0; i < columnGroupHeaderLayer.getRowCount(); i++ ) {
			InitializeAutoResizeRowsCommand rowCommand = new InitializeAutoResizeRowsCommand(
					columnGroupHeaderLayer, i, this.getConfigRegistry(), new GCFactory(this));
			boolean bRes = this.doCommand(rowCommand);			
		}
	}

	protected void updateHeaderImageRegistry() {
		for(int i = 0; i < getGRITSTableDataObject().getLastHeader().size(); i++ ) {
			GRITSColumnHeader header = getGRITSTableDataObject().getLastHeader().get(i);
			if( header.getLabel().endsWith(".png") ) {
				registerHeaderImage(configRegistry, header.getKeyValue(), header.getLabel());
			}
		}
	}

	protected void updateHeaderCheckboxRegistry() {
		HashMap<String, Boolean> htProcessed = new HashMap<>();
		//		SharedCheckboxWidget scw = getParentViewerSharedCheckboxWidget();
		for(int i = 0; i < getGRITSTableDataObject().getTableHeader().get(0).size(); i++ ) {
			GRITSColumnHeader header = getGRITSTableDataObject().getTableHeader().get(0).get(i);

//			if( header.getLabel().startsWith(MSGlycanAnnotationTable.GLYCAN_ID_PREFIX) && ! htProcessed.containsKey(header.getKeyValue())) { // 04/10/2018 MASAAKI
			if( isAnnotationStructureId(header.getLabel()) && ! htProcessed.containsKey(header.getKeyValue())) {
				String sPeakAndFeature = header.getKeyValue();
				registerHeaderCheckbox(configRegistry, header);
				htProcessed.put(header.getKeyValue(), Boolean.TRUE);
			}
		}
	}

	protected void registerHeaderImage(ConfigRegistry configRegistry, String sequence, String imgName) {
		try {
			GlycanImageObject image = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(sequence);
			if( image == null ) 
				throw new Exception("Image not found!");
			Object ob = configRegistry.getSpecificConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, imgName);
			if ( ob != null ) {
				configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, imgName);
				configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL, imgName);
			}
			ILayer layer = gridLayer;
			boolean bRes = layer.doCommand(
					new RowResizeCommand(
							layer, 
							1, 
							image.getSwtImage().getBounds().height));
			AutoResizeImagePainter imgPainter = new AutoResizeImagePainter(image, true, 5, true);
			Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, imgName);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, imgPainter, DisplayMode.NORMAL, imgName);
		} catch (Exception e) {
			logger.error("Unable to process image: " + imgName, e);
		}
	}

	protected void registerHeaderCheckbox(ConfigRegistry configRegistry, GRITSColumnHeader header) {
		SharedCheckboxWidget scw = getParentViewerSharedCheckboxWidget();
		ExtCheckBoxPainter ecbp = scw.getHtGlycanToCheckBox().get(header.getKeyValue());
		Object ob = configRegistry.getSpecificConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, header.getLabel());
		if ( ob != null ) {
			configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, header.getLabel());
			configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL, header.getLabel());
		}
		Style cellStyle = GRITSNatTableStyleConfiguration.getNewStyle();

		configRegistry.registerConfigAttribute(	CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, header.getLabel());
		configRegistry.registerConfigAttribute(	CellConfigAttributes.CELL_PAINTER, ecbp, DisplayMode.NORMAL, header.getLabel());
	}

	@Override
	public void mouseUp(MouseEvent e) {
		//		super.mouseUp(e);
	}

	public void setCheckBoxStatus( String _sKey, boolean _bStatus ) {
		SharedCheckboxWidget scw = getParentViewerSharedCheckboxWidget();
		ExtCheckBoxPainter ecbp = scw.getHtGlycanToCheckBox().get(_sKey);
		ecbp.setCurStatus(_bStatus);
	}

	@Override
	public void mouseDown(MouseEvent e) {
		GridLayer gl = (GridLayer) getLayer();
		int origRow = gl.getRowPositionByY(e.y); 
		int origCol = gl.getColumnPositionByX(e.x); // must subtract the corner column
		int iNewCol = groupedAccumulator.getGroupedColPosition(origCol - 1, origRow);
		if( iNewCol < 0 ) {
			return;
		}
		String sKey = (String) topColumnHeaderDataProvider.getDataKey(iNewCol, origRow);
		String sLabel = (String) topColumnHeaderDataProvider.getDataValue(iNewCol, origRow);
//		if( sLabel != null && sLabel.startsWith(MSGlycanAnnotationTable.GLYCAN_ID_PREFIX) ) { // 04/10/2018 MASAAKI
		if( sLabel != null && isAnnotationStructureId(sLabel) ) {
			//check if the selections are locked or not to decide whether to allow changes
			MSGlycanAnnotationSummary summaryViewer = getSummaryViewer();
			Integer iParentScanNum = summaryViewer.getParentViewScanNum();
			MSAnnotationEntityProperty prop = (MSAnnotationEntityProperty) summaryViewer.getEntry().getProperty();
			if (prop.getMsLevel() > 3)
				iParentScanNum = prop.getParentScanNum();    // actual scan number for MSn (for MS1, direct infusion, the parent scan number is always the first one)
			String iPeakId = summaryViewer.getParentViewRowId();
			if (iPeakId != null && iPeakId.indexOf(":") > 0) {
				iPeakId = iPeakId.substring(0, iPeakId.indexOf(":"));
			}
			if (iParentScanNum != null && iPeakId != null) {
				if (summaryViewer.getParentSubsetTable() != null &&
						summaryViewer.getParentSubsetTable().getParentTable() != null &&
						summaryViewer.getParentSubsetTable().getParentTable().getGRITSTableDataObject().isLockedPeak(iParentScanNum, iPeakId)) {
					MessageDialog.openInformation(getShell(), "Disabled", "Selections are locked, cannot make changes. Please unlock if you still wish to change candidate selections!");
					return;
				}
			}
			toggleParentSelectedRow(sKey);
			if( getGRITSTableDataObject().getTableData().size() == 2 ) {
				// hacky solution to ticket # 247. If there are data rows, refresh happens
				// automatically
				updateHeaderImageRegistry();
			}
		}
	}

	protected MSGlycanAnnotationSummary getSummaryViewer() {

		//	MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(parentView.getParentEditor().getContext(), parentEntry);
		//	return viewer.getSummaryView();
		return (MSGlycanAnnotationSummary) parentView.getParentEditor();
	}	

	private GRITSColumnHeader getColumnHeaderFromKey( String _sKey ) {
		for(int i = 0; i < getGRITSTableDataObject().getTableHeader().get(0).size(); i++ ) {
			GRITSColumnHeader header = getGRITSTableDataObject().getTableHeader().get(0).get(i);
			if( header.getKeyValue().equals(_sKey) )
				return header;
		}
		return null;
	}

	protected SharedCheckboxWidget getParentViewerSharedCheckboxWidget() {
		//		MSGlycanAnnotationSummaryTableDataProcessor proc = (MSGlycanAnnotationSummaryTableDataProcessor) getTableDataProcessor();
		//		SharedCheckboxWidget scw = proc.getParentViewerSubsetTable().getSharedCheckboxWidget();
		//		return scw;	
		MSGlycanAnnotationSummary summaryViewer = getSummaryViewer();
		return summaryViewer.getParentSubsetTable().getSharedCheckboxWidget();
	}

	public void updateCheckbox( String sKey ) {
		GRITSColumnHeader header = getColumnHeaderFromKey(sKey);
		registerHeaderCheckbox(configRegistry, header);		
	}

	public void toggleParentSelectedRow(String sKey) {
		MSGlycanAnnotationSummary summaryViewer = getSummaryViewer();
		MSAnnotationMultiPageViewer parentViewer = getParentMultiPageViewer();
		if( parentViewer != null ) {
			MSAnnotationTable parentTable = null;
			if (parentViewer.getPeaksView() != null && !parentViewer.getPeaksView().isEmpty() && parentViewer.getPeaksView().get(0).getViewBase() != null)
				parentTable = (MSAnnotationTable) parentViewer.getPeaksView().get(0).getViewBase().getNatTable();
			else if (parentViewer.getDetailsView() != null)
				parentTable = (MSAnnotationTable) parentViewer.getDetailsView().getViewBase().getNatTable();

			if( ! parentTable.getCurrentRowId().equals(summaryViewer.getParentViewRowId()) ) { // refresh the subset table in the parent first
				MSGlycanAnnotationMultiPageViewer.showRowSelection(parentViewer.getContext(), parentViewer.getEntry(), parentTable, 
						summaryViewer.getParentViewRowIndex(), summaryViewer.getParentViewScanNum(), summaryViewer.getParentViewRowId());
				parentTable.setCurrentRowId(summaryViewer.getParentViewRowId());
				parentTable.setCurrentRowIndex(summaryViewer.getParentViewRowIndex());
			}
			parentTable.getCurrentSubsetTable().toggleSubsetTableRowsForClickedItem(sKey);
			boolean bDirty = parentTable.startUpdateHiddenRowsAfterEdit(parentTable.getCurrentSubsetTable());
			parentTable.finishUpdateHiddenRowsAfterEdit(bDirty);	
		}
	}		

	@Override
	public boolean startUpdateHiddenRowsAfterEdit(String _sCustomExtraDataKey, int _iNumTopHits,
			boolean _bOverrideManual, Object filter, boolean keepExisting, boolean highlightOnly) {
		return super.startUpdateHiddenRowsAfterEdit(_sCustomExtraDataKey, _iNumTopHits, _bOverrideManual, filter, keepExisting, highlightOnly);
	}

	@Override
	protected void initImageConfigRegistry() {
		super.initImageConfigRegistry();
	}

	private class SummaryColumnHeaderDataLayer extends DefaultColumnHeaderDataLayer {

		public SummaryColumnHeaderDataLayer(
				IDataProvider columnHeaderDataProvider) {
			super(columnHeaderDataProvider);
			calculateRowHeight();
		}

		public void calculateRowHeight() {
			int iMaxHeight = getHeight();
			int iCol = getDataProvider().getColumnCount();
			for( int i = 0; i < iCol; i++ ) {
				String label = ((GRITSColumnHeaderDataProvider) getDataProvider()).getDataValue(i, 0);
				String key = ((GRITSColumnHeaderDataProvider) getDataProvider()).getDataKey(i, 0);
				if( label != null && label.endsWith(".png") ) {					
					GlycanImageObject image = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(key);
					if( image != null && image.getAwtBufferedImage().getHeight() > iMaxHeight ) {
						iMaxHeight = image.getAwtBufferedImage().getHeight() + 10; // some buffer
					}
				}
			}
			setRowHeightByPosition(0, iMaxHeight);			
		}
	}

	@Override
	protected void hackResizeLastRowsAndCols() {
		int iColCnt = selectionLayer.getColumnCount();
		int iWidth = viewportLayer.getWidth();
		int iMaxWidth = viewportLayer.getClientAreaWidth();
		int iColWidthAdd = 0;
		iColWidthAdd = (int) ((double) (iMaxWidth - iWidth) / (double) (iColCnt));
		if (iMaxWidth > iWidth ) { // have some leftover and not already expanded
			for (int i = 1; i < iColCnt; i++ ) {
				viewportLayer.moveColumnPositionIntoViewport(i);
				int colPosition = LayerUtil.convertColumnPosition(
						selectionLayer, i, viewportLayer);
				if (colPosition < 0)
					continue;
				int iNewWidth = viewportLayer.getColumnWidthByPosition(i);
				iNewWidth += iColWidthAdd;
				ColumnResizeCommand colResizeCommand = new ColumnResizeCommand(
						selectionLayer, colPosition,
						iNewWidth);
				this.doCommand(colResizeCommand);
			}
		}
	}

	// Overrides InitializeAutoResizeColumnsCommand for columns w/ check box labels to ensure the entire header text fits
	@Override	
	public boolean doCommand(ILayerCommand command) {
		if( command instanceof InitializeAutoResizeColumnsCommand ) {
			int colPosition = LayerUtil.convertColumnPosition(
					((InitializeAutoResizeColumnsCommand) command).getLayer(), ((InitializeAutoResizeColumnsCommand) command).getColumnPosition(), selectionLayer);
			String checkLabel = topColumnHeaderDataProvider.getDataValue(colPosition, 0);
//			if(checkLabel != null && checkLabel.startsWith(MSGlycanAnnotationTable.GLYCAN_ID_PREFIX) ) { // 04/10/2018 MASAAKI
			if(checkLabel != null && isAnnotationStructureId(checkLabel) ) {
				FontMetrics fm = ((InitializeAutoResizeColumnsCommand) command).getGCFactory().createGC().getFontMetrics();
				int iWidth = fm.getAverageCharWidth() * (checkLabel.toCharArray().length + 5 );

				ColumnResizeCommand colResizeCommand = new ColumnResizeCommand(
						selectionLayer, colPosition,
						iWidth);
				return super.doCommand(colResizeCommand);
			}

		}
		return super.doCommand(command);
	}

	// 04/10/2018 MASAAKI
	protected boolean isAnnotationStructureId(String sLabel) {
		return sLabel.startsWith(MSGlycanAnnotationTable.GLYCAN_ID_PREFIX);
	}

	public boolean getHideCommonFragments() {
		return bHideCommon;
	}
	
	public void setHideCommonFragments(boolean bHideCommon) {
		this.bHideCommon = bHideCommon;
	}
	
	public void hideCommonFragments() {
		setHideCommonFragments(true);
		finishUpdateHiddenRowsAfterEdit(false);
	}

	public void showCommonFragments() {
		setHideCommonFragments(false);
		finishUpdateHiddenRowsAfterEdit(false);
	}

}
