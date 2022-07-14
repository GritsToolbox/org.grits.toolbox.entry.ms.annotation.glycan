package org.grits.toolbox.entry.ms.annotation.glycan.process.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationSummaryViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationSummaryTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanAnnotation;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMFeature;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationDetails;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationDetails;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationEntityScroller;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.GlycanFeature;
import org.grits.toolbox.utils.image.GlycanImageProvider;

public class MSGlycanAnnotationSummaryTableDataProcessor extends MSAnnotationTableDataProcessor {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSummaryTableDataProcessor.class);
	public static final GRITSColumnHeader FIRST_GROUP = new GRITSColumnHeader("Peak Info", "peak_info");
	protected List<MSAnnotationTableDataProcessor> tableDataProcessors = null;
	protected MSGlycanAnnotationTable msParentGAT = null;
	protected List<Integer> iSummaryAnnotIds = null;
	
	/*
	 * Entry _entry => The entry from the details view in this same multipage viewer
	 * Property _sourceProperty => The property from the details view in this same multipage viewer
	 * List<MSATDP> tableDataProcessors => list of peak table data processors from the details view
	 */
	public MSGlycanAnnotationSummaryTableDataProcessor(Entry _entry, Property _sourceProperty, 
			List<MSAnnotationTableDataProcessor> tableDataProcessors) {
		super(_entry, _sourceProperty, -1);
		this.tableDataProcessors = tableDataProcessors;
		this.data = this.tableDataProcessors.get(0).getData();
	}

	public List<MSAnnotationTableDataProcessor> getTableDataProcessors() {
		return tableDataProcessors;
	}

	@Override
	protected TableViewerColumnSettings initializeColumnSettings() {
		TableViewerColumnSettings newSettings = getNewTableViewerSettings();
		int iCols = MSGlycanAnnotationSummaryTableDataProcessorUtil.fillMSGlycanAnnotationSummaryColumnSettings(newSettings);
		setLastVisibleCol(iCols);
		return newSettings;
	}

	@Override
	public TableViewerPreference initializePreferences() {
		MSAnnotationViewerPreference preferences = (MSAnnotationViewerPreference) super.initializePreferences();
		MSGlycanAnnotationSummaryTableDataProcessorUtil.postProcessColumnSettings(preferences);
		return preferences;
	}
	
	@Override
	protected TableViewerPreference getNewTableViewerPreferences() {
		return new MSGlycanAnnotationSummaryViewerPreference();
	}
		
	@Override
	public boolean createTable() throws Exception {
		// TODO Auto-generated method stub
		return super.createTable();
	}
	
	@Override
	public void initializeTableDataObject(Property _sourceProperty) {
		MSGlycanAnnotationSummaryTableDataObject mobj = new MSGlycanAnnotationSummaryTableDataObject(( (MassSpecEntityProperty) _sourceProperty).getMsLevel(), this.fillType);
		setSimianTableDataObject(mobj);
		getSimianTableDataObject().initializePreferences();
		if (getSimianTableDataObject().getTablePreferences().settingsNeedInitialization()) {
			TableViewerPreference tvp = initializePreferences();		
			MSGlycanAnnotationSummaryTableDataProcessor.setDefaultColumnViewSettings(tvp.getPreferenceSettings());
			getSimianTableDataObject().setTablePreferences(tvp);
			getSimianTableDataObject().getTablePreferences().writePreference();        	
		
		}
	}

	@Override
	public boolean saveChanges() throws Exception {
		return true; // have to implement but nothing to do
	}

	@Override
	public boolean readDataFromFile() {
		return false; // have to implement but nothing to do
	}

	@Override
	public void buildTable() throws Exception {
		processExternalQuant();
		this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Building table...");
		HashMap<PeakInfo, HashMap<String, List<FeatureInfo>>> htAllPeaks = getAnnotatedPeaks();
		int iMax = htAllPeaks.size();
		this.progressBarDialog.getMinorProgressBarListener(0).setMaxValue( iMax );
		ArrayList<ArrayList<GRITSColumnHeader>> alHeaders = getHeaderLines(getTempPreference().getPreferenceSettings());

		this.getSimianTableDataObject().getTableHeader().add(alHeaders.get(0));
		this.getSimianTableDataObject().getTableHeader().add(alHeaders.get(1));
		addTableData(htAllPeaks);
		if (getSimianTableDataObject().getTableData().isEmpty()) {
			// adding 2 blank rows to the subset table
			getSimianTableDataObject().getTableData().add(
					TableDataProcessor.getNewRow(getSimianTableDataObject()
							.getLastHeader().size(), getSimianTableDataObject()
							.getTableData().size()));
			getSimianTableDataObject().getTableData().add(
					TableDataProcessor.getNewRow(getSimianTableDataObject()
							.getLastHeader().size(), getSimianTableDataObject()
							.getTableData().size()));
		}
	}

	public static void setDefaultColumnViewSettings(TableViewerColumnSettings tvs) {
		GRITSColumnHeader header = tvs.getColumnHeader(DMPeak.peak_id.name());
		if (header != null) {
			tvs.setVisColInx(header, -1);
		}
		header = tvs.getColumnHeader(DMGlycanAnnotation.glycan_annotation_glycanId.name());
		if (header != null) {
			tvs.setVisColInx(header, -1);
		}
		header = tvs.getColumnHeader(DMFeature.feature_sequence.name());
		if (header != null) {
			tvs.setVisColInx(header, -1);
		}
		header = tvs.getColumnHeader(DMFeature.feature_id.name());
		if (header != null) {
			tvs.setVisColInx(header, -1);
		}
		header = tvs.getColumnHeader(DMFeature.feature_charge.name());
		if (header != null) {
			tvs.setVisColInx(header, -1);
		}
	}

	private MSGlycanAnnotationTableDataObject getMySimianTableDataObject() {
		return (MSGlycanAnnotationTableDataObject) getSimianTableDataObject();
	}

	public static Annotation getAnnotation(Data _data, Integer annId) {
		for (Annotation ann : _data.getAnnotation()) {
			if (ann.getId().equals(annId)) {
				return ann;
			}
		}
		return null;
	}

	public void setParentViewerSubsetTable( MSGlycanAnnotationTable msParentGAT ) {
		this.msParentGAT = msParentGAT;		
	}
	
	private ArrayList<ArrayList<GRITSColumnHeader>> getHeaderLines(
			TableViewerColumnSettings _columnSettings) throws Exception {
		ArrayList<ArrayList<GRITSColumnHeader>> alHeaders = new ArrayList<>();
		ArrayList<GRITSColumnHeader> alHeader = new ArrayList<>();
		List<String> lProcessed = new ArrayList<String>();
		GRITSColumnHeader colHeader = null;
		iSummaryAnnotIds = new ArrayList<>();
		try {
			// do prefix columns first
			for (GRITSColumnHeader header : _columnSettings.keySet()) {
				if (!header.isGrouped()) {
					// null tells system to NOT create first column header
					alHeader.add(FIRST_GROUP);
				}
			}		
			int iNumRows = msParentGAT.getBottomDataLayer().getRowCount();
			lProcessed.clear();
			for (MSAnnotationTableDataProcessor processor : this.tableDataProcessors) {
				Annotation parentAnnot = processor.getCurAnnotation();
				if ( parentAnnot == null ) {
					continue;
				}
				String sParentFeatureId = ((MSAnnotationEntityProperty) processor.getSourceProperty()).getFeatureId();
//				Feature feature2 = getFeature(processor, processor.getCurFeature().getId());
				for (int i = 0; i < iNumRows; i++) {
					if (msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getPeakIdCols().get(0), i) == null)
						continue;
					if (msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getFeatureIdCols().get(0), i) == null)
						continue;
					if (msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getAnnotationIdCols().get(0), i) == null)
						continue;
					Integer iPeakId = ((Integer) msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getPeakIdCols().get(0), i));
					String sId = msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getFeatureIdCols().get(0), i).toString();
					Integer iAnnotId = ((Integer) msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getAnnotationIdCols().get(0), i));
					Annotation annot = getAnnotation(iAnnotId);
					if( parentAnnot.getId() != annot.getId() ) 
						continue;
//					Feature feature = getFeature(processor, sId);
					if( sParentFeatureId != null && sId != null && ! sParentFeatureId.equals(sId) ) {
						continue;
					}
					if( ! iSummaryAnnotIds.contains( annot.getId() ) ) {
						iSummaryAnnotIds.add(annot.getId());
					}
					String sHeaderKey = MSAnnotationEntityScroller.getCombinedKeyForLookup(iPeakId, sId);
					if( lProcessed.contains(sId) ) {
						continue;
					}
					MSAnnotationEntityProperty prop = (MSAnnotationEntityProperty) processor.getSourceProperty();
					String sLabel = MSAnnotationDetails.getLabelForCheckbox(getAnnotationStructureId(annot), sId, prop.getMsLevel());
					colHeader = new GRITSColumnHeader(sLabel, sHeaderKey);
					for (GRITSColumnHeader header : _columnSettings.keySet()) {
						if (header.isGrouped()) {
							alHeader.add(colHeader);
						}
					}
					lProcessed.add(sId);
				}
			}
			alHeaders.add(alHeader);
			alHeader = new ArrayList<>();
			// now add second row
			for (GRITSColumnHeader header : _columnSettings.keySet()) {
				if (!header.isGrouped()) {
					// alHeader.add(header);
					addHeaderLine(alHeader.size(), header, alHeader);
				}
			}
			lProcessed.clear();
			for (MSAnnotationTableDataProcessor processor : this.tableDataProcessors) {
				Annotation parentAnnot = processor.getCurAnnotation();
				if ( parentAnnot == null ) {
					continue;
				}
				String sParentFeatureId = ((MSAnnotationEntityProperty) processor.getSourceProperty()).getFeatureId();
//				Feature feature = getFeature(processor, processor.getCurFeature().getId());
				for (int i = 0; i < iNumRows; i++) {
					if (msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getPeakIdCols().get(0), i) == null)
						continue;
					if (msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getFeatureIdCols().get(0), i) == null)
						continue;
					if (msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getAnnotationIdCols().get(0), i) == null)
						continue;
					if (msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getSequenceCols().get(0), i) == null)
						continue;
					Integer iAnnotId = ((Integer) msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getAnnotationIdCols().get(0), i));
					Annotation annot = getAnnotation(iAnnotId);
					String sSeq = (String) msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getSequenceCols().get(0), i);
					Integer iPeakId = ((Integer) msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getPeakIdCols().get(0), i));
					String sId = msParentGAT.getBottomDataLayer().getDataValueByPosition(
							((MSAnnotationTableDataObject) msParentGAT.getGRITSTableDataObject()).getFeatureIdCols().get(0), i).toString();
					
					if( parentAnnot.getId() != annot.getId() ) 
						continue;
//					Feature feature = getFeature(processor, sId);
//					if( sParentFeatureId != null && feature != null && feature.getParentId() != null && ! sParentFeatureId.equals(feature.getParentId()) ) {
						if( sParentFeatureId != null && sId != null && ! sParentFeatureId.equals(sId) ) {
						continue;
					}
					String sHeaderKey = MSAnnotationEntityScroller.getCombinedKeyForLookup(iPeakId, sId);
					if( lProcessed.contains(sId) ) {
						continue;
					}
					for (GRITSColumnHeader header : _columnSettings.keySet()) {
						if (header.isGrouped()) {
							if (header.getKeyValue().equals( DMGlycanAnnotation.glycan_annotation_glycancartoon.name() )) {
								GRITSColumnHeader cartoonHeader = new GRITSColumnHeader(
										sSeq + ".png",
										sSeq);
								addHeaderLine(alHeader.size(), cartoonHeader, alHeader);
							} else {
								addHeaderLine(alHeader.size(), header, alHeader);
							}
						}
					}
					lProcessed.add(sId);
				}
			}
			alHeaders.add(alHeader);
			return alHeaders;
		}catch( Exception ex ) {
			logger.error("Error in getHeaderLines", ex);
			throw ex;
		}
	}

	// 04/10/2018 MASAAKI
	protected String getAnnotationStructureId(Annotation annot) {
		return MSGlycanAnnotationTable.GLYCAN_ID_PREFIX + ((GlycanAnnotation)annot).getStringId();
	}

	@Override
	protected void addHeaderLine(int iPrefColNum, GRITSColumnHeader colHeader,
			ArrayList<GRITSColumnHeader> alHeader) {
		if (colHeader.getLabel().endsWith(".png")) {
			this.getMySimianTableDataObject().addCartoonCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMFeature.feature_id.name())) {
			this.getMySimianTableDataObject().addFeatureIdCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMFeature.feature_sequence.name())) {
			this.getMySimianTableDataObject().addSequenceCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMFeature.feature_charge.name())) {
			this.getMySimianTableDataObject().addFeatureChargeCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMGlycanAnnotation.glycan_annotation_glycanId.name())) {
			this.getMySimianTableDataObject().addAnnotationIdCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMPeak.peak_id.name())) {
			this.getMySimianTableDataObject().addPeakIdCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMPeak.peak_mz.name())) {
			this.getMySimianTableDataObject().addMzCol(iPrefColNum);
		}
		alHeader.add(colHeader);
	}

	public class PeakInfo {
		public Integer iPeakId = null;
		public Integer iScanNum = null;
		public Double dMz = null;
		public Double dIntensity = null;

		public PeakInfo(Integer iPeakId, Integer iScanNum, Double dMz,
				Double dIntensity) {
			this.iPeakId = iPeakId;
			this.iScanNum = iScanNum;
			this.dMz = dMz;
			this.dIntensity = dIntensity;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof PeakInfo) {
				return this.iPeakId.equals(((PeakInfo) arg0).iPeakId);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return iPeakId.hashCode();
		}
	}

	public class FeatureInfo {
		public String sFeatureSeq = null;
		public String sFeatureId = null;
		public String sFeatureCharge = null;
		
		public FeatureInfo(String sFeatureSeq, String sFeatureId, String sFeatureCharge) {
			this.sFeatureSeq = sFeatureSeq;
			this.sFeatureId = sFeatureId;
			this.sFeatureCharge = sFeatureCharge;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof FeatureInfo) {
				return this.sFeatureId == ((FeatureInfo) arg0).sFeatureId;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return sFeatureId.hashCode();
		}
	}

	private static Feature getFeature( MSAnnotationTableDataProcessor processor, String sFeatId ) {
//	private static GlycanFeature getFeature( MSAnnotationTableDataProcessor processor, String sFeatId ) {
		if( processor.getCurScanFeature() == null || processor.getCurScanFeature().getFeatures() == null ) {
			return null;
		}
		for( Feature feature : processor.getCurScanFeature().getFeatures() ) {
			if( feature.getId().equals(sFeatId) ) {
				return feature;
//				return (GlycanFeature) feature;
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private HashMap<PeakInfo, HashMap<String, List<FeatureInfo>>> getAnnotatedPeaks() {
		try {
			HashMap<PeakInfo, HashMap<String, List<FeatureInfo>>> htRetData = new HashMap();
			List<String> processedParent = new ArrayList<>();
			for (MSAnnotationTableDataProcessor processor : this.tableDataProcessors) {
				Annotation parentAnnot = processor.getCurAnnotation();
				if ( parentAnnot == null ) {
					continue;
				}
				String sParentFeatureId = ((MSAnnotationEntityProperty) processor.getSourceProperty()).getFeatureId();
				String sHashKey = parentAnnot.getId().toString();
				if( sParentFeatureId != null ) {
					sHashKey += ":" + sParentFeatureId; 
				}
				if( processedParent.contains(sHashKey) ) {
					continue;
				}
				MSAnnotationTableDataObject tdo = (MSAnnotationTableDataObject) processor.getSimianTableDataObject();
				for (int i = 0; i < tdo.getTableData().size(); i++) {
					GRITSListDataRow row = tdo.getTableData().get(i);
					Integer iPeakId = (Integer) row.getDataRow().get(tdo.getPeakIdCols().get(0));
					if( iPeakId == null ) 
						continue;
					Integer iScanNum = (Integer) row.getDataRow().get(tdo.getScanNoCols().get(0));
					Double dMz = (Double) row.getDataRow().get(tdo.getMzCols().get(0));
					if( dMz == null ) {
						continue;
					}
					String sSeq = (String) row.getDataRow().get(tdo.getSequenceCols().get(0));
//					if( sSeq == null ) {
//						continue;
//					}
					String sFeatId = (String) row.getDataRow().get(tdo.getFeatureIdCols().get(0));
					Object oCharge = row.getDataRow().get(tdo.getFeatureChargeCols().get(0));
					
					String sFeatCharge = "-1";
					if( oCharge != null ) {
						sFeatCharge = oCharge.toString();
					} 
					Integer iAnnotId = (Integer) row.getDataRow().get(tdo.getAnnotationIdCols().get(0));
					Annotation annot = null;
					if( iAnnotId != null ) {
						annot = processor.getAnnotation(iAnnotId);
					}
					Double dIntensity = (Double) row.getDataRow().get(tdo.getPeakIntensityCols().get(0));
					PeakInfo pI = new PeakInfo(iPeakId, iScanNum, dMz, dIntensity);
					HashMap<String, List<FeatureInfo>> htFeatureToListOfSO = null;
					if (htRetData.containsKey(pI)) {
						htFeatureToListOfSO = htRetData.get(pI);
					} else {
						htFeatureToListOfSO = new HashMap<>();
						htRetData.put(pI, htFeatureToListOfSO);
					}
					if (sFeatId == null) {
						continue;
					}
					
					if( parentAnnot != null ) {
						if( annot == null ){
							continue;
						}
						if( ! annot.getId().equals(parentAnnot.getId()) ) {
							continue;
						}
					}
					Feature feature = getFeature(processor, sFeatId);
//					GlycanFeature feature = getFeature(processor, sFeatId);
					if( feature.getParentId() != null && sParentFeatureId != null && ! sParentFeatureId.equals(feature.getParentId()) )  {
						continue;
					}
					
					List<FeatureInfo> lFi = null;
					if (htFeatureToListOfSO.containsKey(sHashKey)) {
						lFi = htFeatureToListOfSO.get(sHashKey);
					} else {
						lFi = new ArrayList<>();
						htFeatureToListOfSO.put(sHashKey, lFi);
					}
					FeatureInfo fi = new FeatureInfo(sSeq, sFeatId, sFeatCharge);
					lFi.add(fi);
				}
				processedParent.add(sHashKey);
			}
			return htRetData;
		} catch (Exception e) {
			logger.error("getAnnotatedPeaks: Error creating list of Peaks");
		}
		return null;
	}

	protected void addTableData(
			HashMap<PeakInfo, HashMap<String, List<FeatureInfo>>> htAllPeaks) {
		Set<PeakInfo> setPeakInfo = htAllPeaks.keySet();
		// SortedSet<Double>
		// Collections.sort(sMzs.toArray());
		// TODO: sort the set
		int iCnt = 1;
		getMySimianTableDataObject().setUnAnnotatedRows(new ArrayList<Integer>());
		for (PeakInfo pI : setPeakInfo) {
			HashMap<String, List<FeatureInfo>> htFeatToListofFI = htAllPeaks.get(pI);
			this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Building table. Peak: " + iCnt);
			this.progressBarDialog.getMinorProgressBarListener(0).setProgressValue(iCnt++);
			addTableRow(pI, htFeatToListofFI, iCnt);
			if (bCancel) {
				setSimianTableDataObject(null);
				return;
			}
		}
	}

	protected void addTableRow(PeakInfo pI,
			HashMap<String, List<FeatureInfo>> htFeatToListofFI, int iRowNum) {
		try {
			GRITSListDataRow alRow = getNewRow();
			MSGlycanAnnotationSummaryTableDataProcessorUtil.fillMSGlycanAnnotationSummryRowPrefix(pI, alRow.getDataRow(), getTempPreference().getPreferenceSettings());
			int iOffset = 0;
			boolean bMatchedOne = false;
			int iColNum = 0;
			for (MSAnnotationTableDataProcessor processor : this.tableDataProcessors) {
				Annotation parentAnnot = processor.getCurAnnotation();
				if ( parentAnnot == null || ! iSummaryAnnotIds.contains(parentAnnot.getId()) ) {
					continue;
				}
				String sParentFeatureId = ((MSAnnotationEntityProperty) processor.getSourceProperty()).getFeatureId();
				
				String sHashKey = parentAnnot.getId().toString();
				if( sParentFeatureId != null ) {
					sHashKey += ":" + sParentFeatureId; 
				}
				if (htFeatToListofFI.containsKey(sHashKey) ) {
					bMatchedOne = true;
					StringBuilder sbSequence = new StringBuilder();
					StringBuilder sbFeatureId = new StringBuilder();
					StringBuilder sbFeatureCharge = new StringBuilder();
					int iCnt2 = 0;
					boolean bIsTop = false;
					for (FeatureInfo fi : htFeatToListofFI.get(sHashKey)) {
						if (iCnt2++ > 5)
							continue;
						if (!sbSequence.toString().equals("")) {
							sbSequence.append(GlycanImageProvider.COMBO_SEQUENCE_SEPARATOR);
							sbFeatureId.append(GlycanImageProvider.COMBO_SEQUENCE_SEPARATOR);
						}
						sbSequence.append(fi.sFeatureSeq);
						sbFeatureId.append(fi.sFeatureId);
						if ( iCnt2 == 1 ) {
							sbFeatureCharge.append(fi.sFeatureCharge);
						}
					}
					fillMSAnnotationSummaryEntryData(parentAnnot,
							sbFeatureId.toString(), 
							sbSequence.toString(), sbFeatureCharge.toString(), iOffset,
							alRow.getDataRow(), getTempPreference().getPreferenceSettings());
				}
				iOffset += getLastVisibleCol();
				iColNum++;
			}
			getSimianTableDataObject().getTableData().add(alRow);
			if (!bMatchedOne) {
				getMySimianTableDataObject().getUnAnnotatedRows().add(getSimianTableDataObject().getTableData().size() - 1);
			}

		} catch (Exception e) {
			logger.error("addScanData: error adding scans data to table model.", e);
		}
	}

	protected void fillMSAnnotationSummaryEntryData(Annotation parentAnnot,
			String sFeatureId, String sSequence, String sFeatureCharge, int iOffset,
			ArrayList<Object> alDataRow, TableViewerColumnSettings preferenceSettings) {
		MSGlycanAnnotationSummaryTableDataProcessorUtil.fillMSGlycanAnnotationSummryEntryData(
				(GlycanAnnotation)parentAnnot,
				sFeatureId, sSequence, sFeatureCharge, iOffset, alDataRow, preferenceSettings);
	}
}