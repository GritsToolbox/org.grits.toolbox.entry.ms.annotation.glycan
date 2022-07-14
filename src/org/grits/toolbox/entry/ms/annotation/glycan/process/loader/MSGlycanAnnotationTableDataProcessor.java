package org.grits.toolbox.entry.ms.annotation.glycan.process.loader;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.MSGlycanAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanAnnotation;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMAnnotation;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMFeature;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPrecursorPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMScan;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.extquantfiles.process.MSGlycanAnnotationCustomAnnotationProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.extquantfiles.process.CustomAnnotationDataProcessor;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessorUtil;
import org.grits.toolbox.ms.file.extquant.data.ExternalQuantSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.AnnotationFilter;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.GlycanFeature;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 * Extends MSAnnotationTableDataProcessor with specific options for displaying glycan annotated mass spec data
 */
public class MSGlycanAnnotationTableDataProcessor extends MSAnnotationTableDataProcessor
{
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationTableDataProcessor.class);
	private AnnotationFilter filter = null;

	/**
	 * @param _entry - current MS Entry
	 * @param _sourceProperty - current MS property
	 * @param iMinMSLevel - min MS level of this MS run
	 */
	public MSGlycanAnnotationTableDataProcessor(Entry _entry, Property _sourceProperty, int iMinMSLevel) {	
		super(_entry, _sourceProperty, iMinMSLevel);
	}

	/**
	 * @param _entry - current MS Entry
	 * @param _sourceProperty - current MS property
	 * @param fillType - FillType, options are "Scans" and "PeakList" and determine how to fill the GRITSTable 
	 * @param iMinMSLevel - min MS level of this MS run
	 */
	public MSGlycanAnnotationTableDataProcessor(Entry _entry, Property _sourceProperty, FillTypes _fillType, int iMinMSLevel)
	{
		super(_entry, _sourceProperty, _fillType, iMinMSLevel);
	}

	/**
	 * @param _parent - if table created by a parent, the parent's TableDataProcessor 
	 * @param _entry - current MS Entry
	 * @param _sourceProperty - current MS property
	 * @param fillType - FillType, options are "Scans" and "PeakList" and determine how to fill the GRITSTable 
	 * @param iMinMSLevel - min MS level of this MS run
	 */
	public MSGlycanAnnotationTableDataProcessor( TableDataProcessor _parent, Property _sourceProperty, FillTypes _fillType, int iMinMSLevel )
	{
		super(_parent, _sourceProperty, _fillType, iMinMSLevel);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#addCustomAnnotationPeaks(org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference, org.grits.toolbox.entry.ms.extquantfiles.process.CustomAnnotationDataProcessor)
	 */
	@Override
	protected int addCustomAnnotationPeaks( MassSpecViewerPreference _preferences, CustomAnnotationDataProcessor _processor ) {
		int iColCnt = super.addCustomAnnotationPeaks(_preferences, _processor);		
		return iColCnt;
	}


	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#createTable()
	 */
	@Override
	public boolean createTable() throws Exception {
		return super.createTable();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#readDataFromFile()
	 */
	@Override
	public boolean readDataFromFile() {
		boolean bSuccess = super.readDataFromFile();
		return bSuccess;
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#initializeTableDataObject(org.grits.toolbox.core.datamodel.property.Property)
	 */
	@Override
	public void initializeTableDataObject(Property _sourceProperty) {
		setSimianTableDataObject(new MSGlycanAnnotationTableDataObject(getMassSpecEntityProperty().getMsLevel(), this.fillType));
		getSimianTableDataObject().initializePreferences();
		if( getSimianTableDataObject().getTablePreferences().settingsNeedInitialization() ) {
			TableViewerPreference tvp = initializePreferences();		
			MSGlycanAnnotationTableDataProcessor.setDefaultColumnViewSettings(this.fillType, tvp.getPreferenceSettings());
			getSimianTableDataObject().setTablePreferences(tvp);
			getSimianTableDataObject().getTablePreferences().writePreference();        	
		}  
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#getNewTableViewerPreferences()
	 */
	@Override
	protected TableViewerPreference getNewTableViewerPreferences() {
		return new MSGlycanAnnotationViewerPreference();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#initializePreferences()
	 */
	@Override
	public TableViewerPreference initializePreferences() {
		MSGlycanAnnotationViewerPreference newPreferences = (MSGlycanAnnotationViewerPreference) super.initializePreferences();

		MSGlycanAnnotationViewerPreference oldPreferences = (MSGlycanAnnotationViewerPreference) getSimianTableDataObject().getTablePreferences();
		if( oldPreferences != null ) { // preserve previous setting if present
			newPreferences.setShowExtraInfo(oldPreferences.getShowExtraInfo());
		}
		MSGlycanAnnotationTableDataProcessorUtil.postProcessColumnSettings(newPreferences);
		return newPreferences;
	}

	/**
	 * Description: sets the default order of columns for the MS Glycan Annotation table.
	 * 
	 * @param fillType - the FillType of the current page
	 * @param tvs - a TableViewerColumnSettings object
	 */
	public static void setDefaultColumnViewSettings(FillTypes fillType, TableViewerColumnSettings tvs) {
		MSAnnotationTableDataProcessor.setDefaultColumnViewSettings(fillType, tvs);
		if ( fillType == FillTypes.PeaksWithFeatures ) {
			GRITSColumnHeader header = tvs.getColumnHeader( DMGlycanAnnotation.glycan_annotation_sequence.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}			
			header = tvs.getColumnHeader( DMGlycanAnnotation.glycan_annotation_sequenceGWB.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}
			header = tvs.getColumnHeader( DMGlycanAnnotation.glycan_annotation_glycancartoon.name());
			if( header != null ) {
				setDefaultColumnPosition(tvs, header, 0);
			}
		}
	}

	/**
	 * @return MSGlycanAnnotationTableDataObject - casts the TableDataObject to MSGlycanAnnotationTableDataObject
	 */
	private MSGlycanAnnotationTableDataObject getMySimianTableDataObject() {
		return (MSGlycanAnnotationTableDataObject) getSimianTableDataObject();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#addHeaderLine(int, org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader, java.util.ArrayList)
	 */
	@Override
	protected void addHeaderLine( int iPrefColNum, GRITSColumnHeader colHeader, ArrayList<GRITSColumnHeader> alHeader ) {  
		if ( colHeader.getKeyValue().equals( DMGlycanAnnotation.glycan_annotation_glycancartoon.name() ) ) {
			this.getMySimianTableDataObject().addCartoonCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMGlycanAnnotation.glycan_annotation_fragmentcartoon.name() ) ) {
			this.getMySimianTableDataObject().addCartoonCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMScan.scan_scanNo.name()) ||  colHeader.getKeyValue().equals( DMScan.scan_pseudoScanNo.name())) {
			this.getMySimianTableDataObject().addScanNoCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMScan.scan_parentScan.name() ) ) {
			this.getMySimianTableDataObject().addParentNoCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMFeature.feature_id.name() ) ) {
			this.getMySimianTableDataObject().addFeatureIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMFeature.feature_charge.name() ) ) {
			this.getMySimianTableDataObject().addFeatureChargeCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMFeature.feature_sequence.name() ) ) {
			this.getMySimianTableDataObject().addSequenceCol(iPrefColNum);		
		} else if ( colHeader.getKeyValue().equals( DMAnnotation.annotation_id.name() ) ) {
			this.getMySimianTableDataObject().addAnnotationIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMGlycanAnnotation.glycan_annotation_glycanId.name() ) ) {
			this.getMySimianTableDataObject().addAnnotationStringIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_id.name() ) ) {
			this.getMySimianTableDataObject().addPeakIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_mz.name() ) ) {
			this.getMySimianTableDataObject().addMzCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_intensity.name() ) ) {
			this.getSimianTableDataObject().addPeakIntensityCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_is_precursor.name() ) ) {
			this.getSimianTableDataObject().addPeakIsPrecursorCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPrecursorPeak.precursor_peak_intensity.name()) ) {
			this.getSimianTableDataObject().addPrecursorIntensityCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(TableDataProcessor.filterColHeader.getKeyValue())) {
			this.getMySimianTableDataObject().addFilterCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(TableDataProcessor.commentColHeader.getKeyValue())) {
			this.getMySimianTableDataObject().addCommentCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMAnnotation.annotation_ratio.name())) {
			this.getMySimianTableDataObject().addRatioCol(iPrefColNum);
		}
		MassSpecTableDataProcessorUtil.setHeaderValue(iPrefColNum, colHeader, alHeader);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#buildTable()
	 */
	@Override
	public void buildTable() throws Exception {
		super.buildTable();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#processExternalQuant()
	 */
	@Override
	protected boolean processExternalQuant() {
		// TODO Auto-generated method stub
		return super.processExternalQuant();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#addSubScanPeaksData(org.grits.toolbox.ms.om.data.Scan, org.grits.toolbox.ms.om.data.Peak, org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings, org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow)
	 */
	@Override
	protected boolean addSubScanPeaksData( Scan _scan, Peak _peak, TableViewerColumnSettings _settings, GRITSListDataRow alRow ) {
		boolean bAddQuant = super.addSubScanPeaksData(_scan, _peak, _settings, alRow);
		try {
			MSAnnotationEntityProperty eProp = getMSAnnotationEntityProperty();
			MSGlycanAnnotationProperty prop = (MSGlycanAnnotationProperty) eProp.getMSAnnotationParentProperty();
			MSGlycanAnnotationMetaData mData = (MSGlycanAnnotationMetaData) prop.getMSAnnotationMetaData();
						
			if( mData != null && mData.getCustomAnnotations() != null && _scan != null ) { // && _scan.getPrecursor() != null) {

				for( MassSpecCustomAnnotation annotation : mData.getCustomAnnotations() ) {
					for( int i = 0; i < _scan.getPeaklist().size(); i++ ) {
						Peak subpeak = _scan.getPeaklist().get(i);
						bAddQuant |= MassSpecTableDataProcessorUtil.fillMassSpecScanDataCustomAnnotation(_peak, subpeak, 
								alRow.getDataRow(), _settings, annotation.getAnnotatedPeaks() );	
						/* 
						 * DBW 10/03/16: This just wasn't working right yet. It would display cartoons so long as you didn't rearrange columns.
						 * Saving for later if desired
						bAddQuant |= MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationScanDataSpecialPeaks(
								_peak, subpeak, 
								alRow.getDataRow(), _settings, annotation.getAnnotatedPeaks() );
						*/	
					}
				}
			}

		} catch( Exception e ) {
			logger.error("addSubScanPeaksData: error adding peaks data to table model.", e);
		}
		return bAddQuant;
	}


	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#addAnnotationColumns(org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings)
	 */
	@Override
	protected void addAnnotationColumns(TableViewerColumnSettings _settings) {
		super.addAnnotationColumns(_settings);
		MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationColumnSettingsCartoon(_settings, (getMassSpecEntityProperty().getMsLevel() > 2));
		MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationColumnSettingsGlycanAnnotation(_settings);
	}	

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#fillAnnotationData(org.grits.toolbox.ms.om.data.Annotation, org.grits.toolbox.ms.om.data.Feature, org.grits.toolbox.ms.om.data.Scan, int, org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings, org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow)
	 */
	@Override
	protected void fillAnnotationData(Annotation a_annotation, Feature feature, Scan a_scan, int _iNumCandidates, 
			TableViewerColumnSettings _settings, GRITSListDataRow alRow) {
		super.fillAnnotationData(a_annotation, feature, a_scan, _iNumCandidates, _settings, alRow);
		MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationData( 
				(GlycanAnnotation) a_annotation, 
				(GlycanFeature) feature, a_scan, 
				_iNumCandidates, alRow.getDataRow(), _settings);
		if( feature != null ) {
			MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationCartoonColumns(feature.getId(), alRow.getDataRow(),
					_settings, (getMassSpecEntityProperty().getMsLevel() > 2));
		}
	}

	protected CustomAnnotationDataProcessor getNewCustomAnnotationProcessor(ExternalQuantSettings quantSettings) {		
		CustomAnnotationDataProcessor spdf = new MSGlycanAnnotationCustomAnnotationProcessor(quantSettings);	
		return spdf;
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#initializeExternalQuantProcessors()
	 */
	@Override
	protected void initializeQuantFiles() {
		super.initializeQuantFiles();
		/*
		MSAnnotationEntityProperty msaep = (MSAnnotationEntityProperty) getMassSpecEntityProperty();		
		MSGlycanAnnotationMetaData metaData= (MSGlycanAnnotationMetaData) msaep.getMSAnnotationParentProperty().getMSAnnotationMetaData();

		if( metaData != null && metaData.getCustomAnnotations() != null && ! metaData.getCustomAnnotations().isEmpty() ) {
			try {
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Reading special peak file...");
				for( MassSpecCustomAnnotation annotation : metaData.getCustomAnnotations() ) {
					ExternalQuantSettings quantSettings = new ExternalQuantSettings(null, 
							annotation.getIsPPM(), annotation.getMassTolerance());
					CustomAnnotationDataProcessor spdf = new MSGlycanAnnotationCustomAnnotationProcessor(quantSettings);	
					spdf.setMassSpecCustomAnnotation(annotation);
					spdf.setCustomAnnotationPeakList();
					this.quantFileProcessors.add(spdf);
				}

			} catch( Exception e ) {
				logger.error("initializeExternalQuantProcessors: unable to add special peaks files.", e);
			}
		}
		*/
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#initializeColumnSettings()
	 */
	@Override
	protected TableViewerColumnSettings initializeColumnSettings() {
		try {
			TableViewerColumnSettings newSettings = super.initializeColumnSettings();
			/* 
			 * DBW 10/03/16: This just wasn't working right yet. It would display cartoons so long as you didn't rearrange columns.
			 * Saving for later if desired
			int iNumCols = 0;

			if( this.fillType == FillTypes.Scans ) {
				if( this.quantFileProcessors != null ) {
					for( ExternalQuantFileProcessor processor : this.quantFileProcessors ) {
						if( processor instanceof SpecialPeaksDataProcessor ) {
							MSGlycanAnnotationCustomAnnotation msca = (MSGlycanAnnotationCustomAnnotation) ((SpecialPeaksDataProcessor) processor).getMassSpecCustomAnnotation();
							int iAdded = MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationColumnSettingsSpecialPeaks(newSettings, 
									getMassSpecEntityProperty().getMsLevel(), msca);
							if( iAdded > 0) {
//								getMySimianTableDataObject().addCartoonCol(newSettings.getNumColumns() - 1);
								iNumCols += iAdded; 
							}
						} 
					}
				}
			}
			else if ( this.fillType == FillTypes.PeaksWithFeatures ) {
				if( this.quantFileProcessors != null ) {
					for( ExternalQuantFileProcessor processor : this.quantFileProcessors ) {
						if( processor instanceof SpecialPeaksDataProcessor ) {
							MSGlycanAnnotationCustomAnnotation msca = (MSGlycanAnnotationCustomAnnotation) ((SpecialPeaksDataProcessor) processor).getMassSpecCustomAnnotation();
							int iAdded = MSGlycanAnnotationTableDataProcessorUtil.fillMSGlycanAnnotationColumnSettingsSpecialPeaks(newSettings, 
									getMassSpecEntityProperty().getMsLevel(), msca);
							if( iAdded > 0) {
//								getMySimianTableDataObject().addSequenceCol(newSettings.getNumColumns() - 1);
								for( int i = 1; i <= iAdded; i++ ) {
									getMySimianTableDataObject().addExtraCartoonCol(newSettings.getNumColumns() - i);
								}
//								getMySimianTableDataObject().addFeatureIdCol(getMySimianTableDataObject().getFeatureIdCols().get(0));
								iNumCols += iAdded; 
							}
						} 
					}
				}
			}
			setLastVisibleCol(iNumCols);
			*/
			return newSettings;
		} catch( Exception e ) {
			logger.error("initializeColumnSettings: unable to initialize all columns.", e);
		}
		return null;
	}

	/**
	 * @return AnnotationFilter - the "filter" member variable
	 */
	public AnnotationFilter getFilter() {
		return filter;
	}

	/**
	 * @param filter - an AnnotationFilter object
	 */
	public void setFilter(AnnotationFilter filter) {
		this.filter = filter;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor#updateScanFeaturesWithFilter(org.grits.toolbox.ms.om.data.ScanFeatures)
	 */
	@Override
	protected void updateScanFeaturesWithFilter(ScanFeatures features) {
		if (features != null && filter != null)
			features.setFilter(filter);
	}
}