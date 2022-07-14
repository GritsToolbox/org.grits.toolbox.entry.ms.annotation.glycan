package org.grits.toolbox.entry.ms.annotation.glycan.extquantfiles.process;

import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotation;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotationPeak;
import org.grits.toolbox.entry.ms.extquantfiles.process.CustomAnnotationDataProcessor;
import org.grits.toolbox.entry.ms.extquantfiles.process.ExternalQuantColumnInfo;
import org.grits.toolbox.ms.file.extquant.data.ExternalQuantSettings;
import org.grits.toolbox.ms.file.extquant.data.QuantPeakMatch;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.Peak;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 * Specific implementation of CustomAnnotationDataProcessor for MS Glycan Annotation data 
 */
public class MSGlycanAnnotationCustomAnnotationProcessor extends CustomAnnotationDataProcessor{
	public static CustomExtraData getMSGlycanAnnotationSequenceCED( String _sKeyPrefix, String _sLabelPrefix ) { 
		return new CustomExtraData( _sKeyPrefix + "_quant_cartoon", _sLabelPrefix + " Cartoon", 
					"Generic Method", CustomExtraData.Type.String );
	}

	public MSGlycanAnnotationCustomAnnotationProcessor(ExternalQuantSettings a_parameter) {
		super(a_parameter);
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.extquantfiles.process.CustomAnnotationDataProcessor#fillPeakData(org.grits.toolbox.ms.om.data.Peak, org.grits.toolbox.entry.ms.peaklist.extquant.data.QuantPeakMatch)
	 */
	@Override
	protected void fillPeakData( Peak a_peak, QuantPeakMatch cPeak ) {
		MSGlycanAnnotationCustomAnnotation msca = (MSGlycanAnnotationCustomAnnotation) getMassSpecCustomAnnotation();
		Double dKey = cPeak.getMzMostAbundant();
		String sLabel = dKey.toString();
		if( msca.getAnnotatedPeaks().containsKey(dKey) ) {			
			sLabel = msca.getAnnotatedPeaks().get(dKey).getPeakLabel();
		}
		String sIntKey = ExternalQuantColumnInfo.getExternalQuantIntensity(dKey.toString(), sLabel).getKey();
		String sMzKey = ExternalQuantColumnInfo.getExternalQuantIntensityMz(dKey.toString(), sLabel).getKey();
		String sSeqKey = MSGlycanAnnotationCustomAnnotationProcessor.getMSGlycanAnnotationSequenceCED(dKey.toString(), sLabel).getKey();
		MSGlycanAnnotationCustomAnnotationPeak annotPeak = (MSGlycanAnnotationCustomAnnotationPeak) msca.getAnnotatedPeaks().get(dKey);
		a_peak.addDoubleProp( sIntKey, a_peak.getIntensity());
		a_peak.addDoubleProp( sMzKey, a_peak.getMz());		
		a_peak.addStringProp( sSeqKey, annotPeak.getPeakSequence() );
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.extquantfiles.process.CustomAnnotationDataProcessor#clearPeakData(org.grits.toolbox.ms.om.data.Peak, org.grits.toolbox.entry.ms.peaklist.extquant.data.QuantPeakMatch)
	 */
	@Override
	protected void clearPeakData(Peak a_peak, QuantPeakMatch cPeak) {
		MSGlycanAnnotationCustomAnnotation msca = (MSGlycanAnnotationCustomAnnotation) getMassSpecCustomAnnotation();
		Double dKey = cPeak.getMzMostAbundant();
		String sLabel = dKey.toString();
		if( msca.getAnnotatedPeaks().containsKey(dKey) ) {			
			sLabel = msca.getAnnotatedPeaks().get(dKey).getPeakLabel();
		}
		String sIntKey = ExternalQuantColumnInfo.getExternalQuantIntensity(dKey.toString(), sLabel).getKey();
		String sMzKey = ExternalQuantColumnInfo.getExternalQuantIntensityMz(dKey.toString(), sLabel).getKey();
		String sSeqKey = MSGlycanAnnotationCustomAnnotationProcessor.getMSGlycanAnnotationSequenceCED(dKey.toString(), sLabel).getKey();
		MSGlycanAnnotationCustomAnnotationPeak annotPeak = (MSGlycanAnnotationCustomAnnotationPeak) msca.getAnnotatedPeaks().get(dKey);
		a_peak.getDoubleProp().remove( sIntKey );
		a_peak.getDoubleProp().remove( sMzKey );		
		a_peak.getDoubleProp().remove( sSeqKey );
	}
	
}
