package org.grits.toolbox.entry.ms.annotation.glycan.process.loader;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanAnnotation;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanFeature;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMFeature;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessorUtil;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.GlycanFeature;
import org.grits.toolbox.ms.om.data.Scan;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 * MSGlycanAnnotationTableDataProcessorUtil - Fills in rows in a GRITStable with fields appropriate for MS Glycan Annotation of MS data
 */
public class MSGlycanAnnotationTableDataProcessorUtil
{
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationTableDataProcessorUtil.class);

	/**
	 * Description: adds Glycan-specific cartoon columns appropriate for MS Annotation
	 * 
	 * @param _columnSettings - a TableViewerColumnSettings object that will be filled with column headers
	 * @param _bIsFragment - whether or not this is a fragment row.
	 * @return int - number of columns added.
	 */
	public static int fillMSGlycanAnnotationColumnSettingsCartoon( TableViewerColumnSettings _columnSettings, boolean _bIsFragment ) {
		if( _bIsFragment ) {
			_columnSettings.addColumn(DMGlycanAnnotation.glycan_annotation_fragmentcartoon.getLabel(), DMGlycanAnnotation.glycan_annotation_fragmentcartoon.name());			
		} else {
			_columnSettings.addColumn(DMGlycanAnnotation.glycan_annotation_glycancartoon.getLabel(), DMGlycanAnnotation.glycan_annotation_glycancartoon.name());			
		}
		return 1;
	}


	/**
	 * Description: Sets the value of the Cartoon columns
	 * 
	 * @param _oFeatureId - the feature ID (generic Object type)
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 * @param _bIsFragment - whether or not this is a fragment row.
	 */
	public static void fillMSGlycanAnnotationCartoonColumns(Object _oFeatureId, ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings, boolean _bIsFragment ) {
		if( _bIsFragment ) {
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_fragmentcartoon.name() ), _oFeatureId.toString() + ".png", _tableRow);    	
		} else {
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_glycancartoon.name() ), _oFeatureId.toString() + ".png", _tableRow);    	
		}
	}

	/**
	 * Description: allows the modification of column labels and other info AFTER reading from preferences.
	 * 
	 * Added 03/10/16 to change label of feature_charge to "Glycan Charge" when opening Glycan annotation
	 *
	 * @param preference - a TableViewerPreference object
	 */
	public static void postProcessColumnSettings(TableViewerPreference preference) {
		/*
		 	glycan_feature_charge("Glycan Charge"),
			glycan_feature_id("Glycan Id"),
			glycan_feature_type("Glycan Type"),
			glycan_feature_sequence("Glycan Sequence"),
			glycan_feature_mz("Glycan m/z"),
			glycan_feature_deviation("Glycan Mass Error");	
		 */
		TableViewerColumnSettings columnSettings = preference.getPreferenceSettings();
		GRITSColumnHeader oldHeader = columnSettings.getColumnHeader(DMFeature.feature_charge.name());
		if( oldHeader != null ) {
			if( oldHeader.getLabel().equals( DMFeature.feature_charge.getLabel() ) ) {
				int iFeatureChargeCol = columnSettings.getColumnPosition( DMFeature.feature_charge.name() );
				columnSettings.getHeaders().remove(oldHeader);
				GRITSColumnHeader header = new GRITSColumnHeader(DMGlycanFeature.glycan_feature_charge.getLabel(), 
						DMFeature.feature_charge.name());
				columnSettings.putColumn(header, iFeatureChargeCol);
			}
		}		
		oldHeader = columnSettings.getColumnHeader(DMFeature.feature_id.name());
		if( oldHeader != null ) {
			if( oldHeader.getLabel().equals( DMFeature.feature_id.getLabel() ) ) {
				int iFeatureIdCol = columnSettings.getColumnPosition( DMFeature.feature_id.name() );
				columnSettings.getHeaders().remove(oldHeader);
				GRITSColumnHeader header = new GRITSColumnHeader(DMGlycanFeature.glycan_feature_id.getLabel(), 
						DMFeature.feature_id.name());
				columnSettings.putColumn(header, iFeatureIdCol);
			}
		}		
		oldHeader = columnSettings.getColumnHeader(DMFeature.feature_type.name());
		if( oldHeader != null ) {
			if( oldHeader.getLabel().equals( DMFeature.feature_type.getLabel() ) ) {
				int iFeatureTypeCol = columnSettings.getColumnPosition( DMFeature.feature_type.name() );
				columnSettings.getHeaders().remove(oldHeader);
				GRITSColumnHeader header = new GRITSColumnHeader(DMGlycanFeature.glycan_feature_type.getLabel(), 
						DMFeature.feature_type.name());
				columnSettings.putColumn(header, iFeatureTypeCol);
			}
		}		
		oldHeader = columnSettings.getColumnHeader(DMFeature.feature_sequence.name());
		if( oldHeader != null ) {
			if( oldHeader.getLabel().equals( DMFeature.feature_sequence.getLabel() ) ) {
				int iFeatureSequenceCol = columnSettings.getColumnPosition( DMFeature.feature_sequence.name() );
				columnSettings.getHeaders().remove(oldHeader);
				GRITSColumnHeader header = new GRITSColumnHeader(DMGlycanFeature.glycan_feature_sequence.getLabel(), 
						DMFeature.feature_sequence.name());
				columnSettings.putColumn(header, iFeatureSequenceCol);
			}
		}		
		oldHeader = columnSettings.getColumnHeader(DMFeature.feature_mz.name());
		if( oldHeader != null ) {
			if( oldHeader.getLabel().equals( DMFeature.feature_mz.getLabel() ) ) {
				int iFeatureMzCol = columnSettings.getColumnPosition( DMFeature.feature_mz.name() );
				columnSettings.getHeaders().remove(oldHeader);
				GRITSColumnHeader header = new GRITSColumnHeader(DMGlycanFeature.glycan_feature_mz.getLabel(), 
						DMFeature.feature_mz.name());
				columnSettings.putColumn(header, iFeatureMzCol);
			}
		}		
		oldHeader = columnSettings.getColumnHeader(DMFeature.feature_deviation.name());
		if( oldHeader != null ) {
			if( oldHeader.getLabel().equals( DMFeature.feature_deviation.getLabel() ) ) {
				int iFeatureDeviationCol = columnSettings.getColumnPosition( DMFeature.feature_deviation.name() );
				columnSettings.getHeaders().remove(oldHeader);
				GRITSColumnHeader header = new GRITSColumnHeader(DMGlycanFeature.glycan_feature_deviation.getLabel(), 
						DMFeature.feature_deviation.name());
				columnSettings.putColumn(header, iFeatureDeviationCol);
			}
		}		
	}

	/**
	 * Description: adds the Glycan Annotation-specific columns to the TableViewerColumnSettings object
	 * @param _columnSettings - a TableViewerColumnSettings object
	 * @return int - the number of columns added
	 */
	public static int fillMSGlycanAnnotationColumnSettingsGlycanAnnotation( TableViewerColumnSettings _columnSettings ) {
		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_glycanId.getLabel(), DMGlycanAnnotation.glycan_annotation_glycanId.name() );
		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_glytoucanid.getLabel(), DMGlycanAnnotation.glycan_annotation_glytoucanid.name() );
		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_perDerivatisationType.getLabel(), DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name() );
		//		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_composition.getLabel(), DMGlycanAnnotation.glycan_annotation_composition.name() );
		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_sequenceGWB.getLabel(), DMGlycanAnnotation.glycan_annotation_sequenceGWB.name() );
		return 4;
	}

	/**
	 * Description: fills in the Glycan Annotation-specific information into the table row.
	 * 
	 * @param a_annotation - a GlycanAnnotation object
	 * @param a_feature - a GlycanFeature object
	 * @param a_scan - a Scan object
	 * @param _iNumCandidates - the number of candidate annotations for the current row
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	public static void fillMSGlycanAnnotationData(GlycanAnnotation a_annotation, GlycanFeature a_feature, 
			Scan a_scan, int _iNumCandidates, ArrayList<Object> _tableRow, 
			TableViewerColumnSettings _columnSettings)
	{
		if ( a_annotation != null )
		{
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_glycanId.name() ), 
					a_annotation.getStringId(), _tableRow);   
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_glytoucanid.name() ), 
					a_annotation.getGlytoucanId(), _tableRow);  
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name() ), 
					a_annotation.getPerDerivatisationType(), _tableRow);    	
			//			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_composition.name() ), 
			//					a_annotation.getComposition(), _tableRow);    	
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_sequenceGWB.name() ), 
					a_annotation.getSequenceGWB(), _tableRow);    	
		}
	}

	/**
	 * Description: updates the Glycan Annotation with data from the table row. Not used (yet)!
	 * @param a_annotation - an GlycanAnnotation object
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	public static void updateMSGlycanAnnotationData(GlycanAnnotation a_annotation, ArrayList _tableRow, TableViewerColumnSettings _columnSettings )
	{
		if ( a_annotation != null )
		{
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glycanId.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glycanId.name()) ).equals( a_annotation.getStringId()) ) {
				a_annotation.setGlycanId( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glycanId.name()) ));
			}
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glytoucanid.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glytoucanid.name()) ).equals( a_annotation.getGlytoucanId()) ) {
				a_annotation.setGlytoucanId( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_glytoucanid.name()) ));
			}
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name()) ).equals( a_annotation.getPerDerivatisationType()) ) {
				a_annotation.setPerDerivatisationType( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_perDerivatisationType.name()) ));
			}
			if ( _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_sequenceGWB.name()) ) != null &&
					! _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_sequenceGWB.name()) ).equals( a_annotation.getSequenceGWB()) ) {
				a_annotation.setSequenceGWB( (String) _tableRow.get( _columnSettings.getColumnPosition(DMGlycanAnnotation.glycan_annotation_sequenceGWB.name()) ));
			}
		}
	}   
	
	/* 
	 * DBW 10/03/16: This just wasn't working right yet. It would display cartoons so long as you didn't rearrange columns.
	 * Saving for later if desired
	 *
	public static int fillMSGlycanAnnotationColumnSettingsSpecialPeaks( TableViewerColumnSettings _columnSettings, 
			int _iMSLevel, 
			MSGlycanAnnotationCustomAnnotation msca ) {
		int iNumColumns = 0;
		try {
			if( msca != null && msca != null ) {
				Collection<MassSpecCustomAnnotationPeak> annotatedPeaks = msca.getAnnotatedPeaks().values();
				if( annotatedPeaks == null || annotatedPeaks.isEmpty() ) {
					return 0;
				}
				Iterator<MassSpecCustomAnnotationPeak> itr = annotatedPeaks.iterator();
				while( itr.hasNext() ) {
					MassSpecCustomAnnotationPeak mscap = itr.next();	
					Integer iMSLevel = mscap.getMSLevel();
					if( iMSLevel.intValue() != _iMSLevel ) {
						continue;
					}
					
					Double dMz = mscap.getPeakMz();
					String sKey = dMz.toString();
					String sLabel = dMz.toString();
					if( mscap.getPeakLabel() != null && ! mscap.getPeakLabel().equals("") ) {
						sLabel = sKey + " - " + mscap.getPeakLabel();
					}
					CustomExtraData cedSeq = MSGlycanAnnotationCustomAnnotationProcessor.getMSGlycanAnnotationSequenceCED(sKey, sLabel);
					_columnSettings.addColumn( cedSeq.getLabel(), cedSeq.getKey() );				
					iNumColumns += 1;
				}
			}
			return iNumColumns;
		} catch( Exception ex ) {
			logger.error("Error filling Mass Spec Column Settings for Special Peaks.", ex);
		}
		return 0;
	}	
	
	public static boolean fillMSGlycanAnnotationScanDataSpecialPeaks(Peak _precursorPeak, Peak _subscanPeak, 
			ArrayList<Object> _tableRow, 
			TableViewerColumnSettings _columnSettings, HashMap<Double, MassSpecCustomAnnotationPeak> hmAnnotatedPeaks ) {
		boolean bUpdated = false;
		try {
			if( _subscanPeak == null || _subscanPeak.getDoubleProp() == null ) {
				return false;
			}
			if( _subscanPeak.getDoubleProp().isEmpty() ) {
				return false;
			}
			if( hmAnnotatedPeaks == null ) {
				return false;
			}
			Collection<MassSpecCustomAnnotationPeak> annotatedPeaks = hmAnnotatedPeaks.values();
			if( annotatedPeaks == null || annotatedPeaks.isEmpty() ) {
				return false;
			}
			Iterator<MassSpecCustomAnnotationPeak> itr = annotatedPeaks.iterator();
			while( itr.hasNext() ) {
				MassSpecCustomAnnotationPeak mscap = itr.next();				
				Double dMz = mscap.getPeakMz();
				String sKey = dMz.toString();
				String sLabel = dMz.toString();
				if( mscap.getPeakLabel() != null && ! mscap.getPeakLabel().equals("") ) {
					sLabel = sKey + " - " + mscap.getPeakLabel();
				}
				CustomExtraData cedIntMz = ExternalQuantFileProcessor.getExternalQuantIntensityMz(sKey, sLabel);
				CustomExtraData cedSeq = MSGlycanAnnotationCustomAnnotationProcessor.getMSGlycanAnnotationSequenceCED(sKey, sLabel);
				if( _subscanPeak == null || _subscanPeak.getDoubleProp() == null || _subscanPeak.getDoubleProp().isEmpty() ) {
					continue;
				}
				String sSeq = _subscanPeak.getStringProp().get(cedSeq.getKey());
				Double dMatchedMz = _subscanPeak.getDoubleProp().get(cedIntMz.getKey());
				if( sSeq != null && dMatchedMz != null && _subscanPeak.getMz().equals(dMatchedMz) ) {
//					if( dMz == 1107.5 ) {
//						System.out.println("Why why why!");
//					}
					bUpdated = true;
					MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition(cedSeq.getKey()), 
							sSeq.trim() + ".png",
							_tableRow);
					_precursorPeak.addStringProp(cedSeq.getKey(), sSeq);
				}
			}


		} catch( Exception ex ) {
			logger.error("Error filling Mass Spec Row Data for Peak List with Features Special Peaks.", ex);
		}
		return bUpdated;
	}
	*/
}
