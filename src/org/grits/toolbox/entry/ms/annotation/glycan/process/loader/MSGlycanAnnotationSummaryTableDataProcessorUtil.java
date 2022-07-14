package org.grits.toolbox.entry.ms.annotation.glycan.process.loader;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanAnnotation;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.dmtranslate.DMGlycanFeature;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMFeature;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationSummaryTableDataProcessor.PeakInfo;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessorUtil;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;

public class MSGlycanAnnotationSummaryTableDataProcessorUtil
{
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSummaryTableDataProcessorUtil.class);

	public static int fillMSGlycanAnnotationSummaryColumnSettings( TableViewerColumnSettings _columnSettings ) {		
		GRITSColumnHeader header = new GRITSColumnHeader(DMPeak.peak_id.getLabel(), DMPeak.peak_id.name());
		_columnSettings.addColumn( header );
		header.setIsGrouped(false);
		header = new GRITSColumnHeader(DMPeak.peak_mz.getLabel(), DMPeak.peak_mz.name());
		header.setIsGrouped(false);
		_columnSettings.addColumn( header );
		header = new GRITSColumnHeader(DMPeak.peak_intensity.getLabel(), DMPeak.peak_intensity.name());
		_columnSettings.addColumn( header );
		header.setIsGrouped(false);

		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_glycanId.getLabel(), DMGlycanAnnotation.glycan_annotation_glycanId.name() );
		_columnSettings.addColumn( DMGlycanFeature.glycan_feature_sequence.getLabel(), DMFeature.feature_sequence.name() );
		_columnSettings.addColumn( DMGlycanAnnotation.glycan_annotation_glycancartoon.getLabel(), DMGlycanAnnotation.glycan_annotation_glycancartoon.name());
		_columnSettings.addColumn( DMGlycanFeature.glycan_feature_id.getLabel(), DMFeature.feature_id.name());

		return 4; // in this case, sending number of grouped columns
	}

	public static void fillMSGlycanAnnotationSummryRowPrefix(PeakInfo _peak, 
			ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings )
	{
		MassSpecTableDataProcessorUtil.setRowValue( _columnSettings.getColumnPosition( DMPeak.peak_id.name() ), 
				_peak.iPeakId, _tableRow);    		
		MassSpecTableDataProcessorUtil.setRowValue( _columnSettings.getColumnPosition( DMPeak.peak_mz.name() ), 
				new Double(MassSpecTableDataProcessorUtil.formatDec4.format(_peak.dMz)), _tableRow);    	
		MassSpecTableDataProcessorUtil.setRowValue( _columnSettings.getColumnPosition( DMPeak.peak_intensity.name() ), 
				new Double(MassSpecTableDataProcessorUtil.formatDec1.format(_peak.dIntensity)), _tableRow);    	
	}

	public static void fillMSGlycanAnnotationSummryEntryData(GlycanAnnotation _annotation, String _sFeatureId, 
			String _sSequence, String _sFeatureCharge, int _iOffset, ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings )
	{
		if( _annotation == null ) 
			return;
		MassSpecTableDataProcessorUtil.setRowValue( _iOffset +
				_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_glycanId.name() ), 
				_annotation.getStringId(), _tableRow);    	
		MassSpecTableDataProcessorUtil.setRowValue( _iOffset +
				_columnSettings.getColumnPosition( DMFeature.feature_sequence.name() ), 
				_sSequence, _tableRow);    	
		MassSpecTableDataProcessorUtil.setRowValue( _iOffset +
				_columnSettings.getColumnPosition( DMGlycanAnnotation.glycan_annotation_glycancartoon.name() ), 
				_sSequence + ".png", _tableRow);    	
		MassSpecTableDataProcessorUtil.setRowValue( _iOffset +
				_columnSettings.getColumnPosition( DMFeature.feature_id.name() ), 
				_sFeatureId, _tableRow);    	

	}
	
	// Added 03/10/16 to change label of feature_charge to "Glycan Charge" when opening Glycan annotation
	public static void postProcessColumnSettings(TableViewerPreference preference) {
		TableViewerColumnSettings columnSettings = preference.getPreferenceSettings();
		GRITSColumnHeader oldHeader = columnSettings.getColumnHeader(DMFeature.feature_id.name());
		if( oldHeader != null ) {
			if( oldHeader.getLabel().equals( DMFeature.feature_id.getLabel() ) ) {
				int iFeatureIdCol = columnSettings.getColumnPosition( DMFeature.feature_id.name() );
				columnSettings.getHeaders().remove(oldHeader);
				GRITSColumnHeader header = new GRITSColumnHeader(DMGlycanFeature.glycan_feature_id.getLabel(), 
						DMFeature.feature_id.name());
				columnSettings.putColumn(header, iFeatureIdCol);
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
	}

}
