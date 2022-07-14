package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotation;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotationPeak;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.dialog.MassSpecCustomAnnotationDialog;
import org.grits.toolbox.entry.ms.preference.IMSPreferenceWithCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.MassSpecCustomAnnotationsPreferenceUI;
import org.grits.toolbox.entry.ms.preference.MassSpecPreference;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationPeak;

public class MSGlycanAnnotationCustomAnnotationsPreferenceUI extends MassSpecCustomAnnotationsPreferenceUI {

	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationCustomAnnotationsPreferenceUI.class);

	public MSGlycanAnnotationCustomAnnotationsPreferenceUI(Composite parent, int style,
			IPropertyChangeListener listener, boolean bAddSaveAsDefault) {
		super(parent, style, listener, bAddSaveAsDefault);
	}

	/**
	 * Utility method that initializes the localAnnotations and entryAnnotations member variables based on the
	 * custom annotations currently applied to the current open Mass Spec entry.
	 * @see MassSpecPreference
	 */
	public void initLocalAnnotations() {
		localAnnotations = new MSGlycanCustomAnnotationPreference();
		MSGlycanAnnotationCustomAnnotationsPreferenceUI.initAnnotationFromEntry(massSpecEntry, localAnnotations);
		entryAnnotations = new MSGlycanCustomAnnotationPreference();
		MSGlycanAnnotationCustomAnnotationsPreferenceUI.initAnnotationFromEntry(massSpecEntry, entryAnnotations);
	}	

	/**
	 * Populates the passed annotation object with the custom annotation data applied to the passed entry.
	 * 
	 * @param entry
	 *    a MassSpeEntry
	 * @param annotations
	 *    the custom annotation object to be populated
	 * @see IMSPreferenceWithCustomAnnotation
	 * @see MassSpecPreference
	 * @see MassSpecCustomAnnotation
	 */
	public static void initAnnotationFromEntry( Entry entry, IMSPreferenceWithCustomAnnotation annotations ) {
		((MSGlycanCustomAnnotationPreference)annotations).setCustomAnnotations( new ArrayList<MassSpecCustomAnnotation>() );
		List<String> localAnnotsAdded = new ArrayList<String>();
		if( entry != null ) {
			Property p = entry.getProperty();
			if( p != null ) {				
				MSAnnotationProperty ep = ((MSAnnotationEntityProperty) p).getMSAnnotationParentProperty();				
				MSGlycanAnnotationMetaData metaData = (MSGlycanAnnotationMetaData) ep.getMSAnnotationMetaData();
				List<MassSpecCustomAnnotation> l = metaData.getCustomAnnotations();
				if( l != null && ! l.isEmpty() ) {
					for( MassSpecCustomAnnotation curAnnot : l ) {
						MSGlycanAnnotationCustomAnnotation glycanAnnot = (MSGlycanAnnotationCustomAnnotation) curAnnot;
						annotations.getCustomAnnotations().add( (MSGlycanAnnotationCustomAnnotation) glycanAnnot.clone() );
						localAnnotsAdded.add(curAnnot.getAnnotationName());
					}
				}
				//((MSGlycanAnnotationPreference)annotations).setCustomAnnotationText( MSGlycanAnnotationPreference.createCustomAnnotationsText(annotations.getCustomAnnotations()) );
			}		
		}
	}

	/**
	 * Loads the Mass Spec preferences from the workspace XML.
	 * 
	 * @return
	 */
	public static IMSPreferenceWithCustomAnnotation loadWorkspacePreferences() {
		try {
			return MSGlycanAnnotationPreferenceLoader.getMSGlycanCustomAnnotationPreference();
		} catch (Exception ex) {
			logger.error("Error getting the mass spec preferences", ex);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.MassSpecCustomAnnotationsPreferenceUI#initStoredAnnotationsList()
	 */
	@Override
	public void initStoredAnnotationsList() {
		String sToSelect = "";
		if( cmbSelectAnnotation.getItems().length != 0 ) {
			cmbSelectAnnotation.removeAll();
		}
		cmbSelectAnnotation.add("");
		// first add the stored preferences
		if( getStoredAnnotations() != null && getStoredAnnotations().getCustomAnnotations() != null ) {
			for( int i = 0; i < getStoredAnnotations().getCustomAnnotations().size(); i++ ) {
				MSGlycanAnnotationCustomAnnotation annot = (MSGlycanAnnotationCustomAnnotation) getStoredAnnotations().getCustomAnnotations().get(i);
				cmbSelectAnnotation.add(annot.getAnnotationName());
				if( currentCustomAnnotation != null && annot.getAnnotationName().equals(currentCustomAnnotation.getAnnotationName()) ) {
					int iItemCount = cmbSelectAnnotation.getItemCount();
					sToSelect = cmbSelectAnnotation.getItem(iItemCount-1);
				}
			}
		}
		if( getContextViewer() != null && getLocalAnnotations() != null && getPreferenceCustomAnnotations(getLocalAnnotations()) != null ) {
			for( int i = 0; i < getPreferenceCustomAnnotations(getLocalAnnotations()).size(); i++ ) {
				MSGlycanAnnotationCustomAnnotation annot = (MSGlycanAnnotationCustomAnnotation) getPreferenceCustomAnnotations(getLocalAnnotations()).get(i);
				MassSpecCustomAnnotationDialog.setComboEntryForTempAnnotation(annot, cmbSelectAnnotation, 
						entryAnnotations.getCustomAnnotations(),
						storedAnnotations.getCustomAnnotations());
				if( currentCustomAnnotation != null && annot.getAnnotationName().equals(currentCustomAnnotation.getAnnotationName()) ) {
					int iItemCount = cmbSelectAnnotation.getItemCount();
					sToSelect = cmbSelectAnnotation.getItem(iItemCount-1);
				}
			}
		}
		int iPrevSelInx = cmbSelectAnnotation.indexOf(sToSelect);
		cmbSelectAnnotation.select(iPrevSelInx);		
	}

	/**
	 * Initializes the storedAnnotations member variable by loading the custom annotations from preferences in the workspace XML
	 */
	public void initStoredAnnotations() {
		storedAnnotations = MSGlycanAnnotationCustomAnnotationsPreferenceUI.loadWorkspacePreferences();		
	}

	@Override
	protected List<MassSpecCustomAnnotation> getPreferenceCustomAnnotations(Object preferences) {
		return ((MSGlycanCustomAnnotationPreference) preferences).getCustomAnnotations();
	}

	@Override
	protected void setPreferenceCustomAnnotations(Object preferences, List<MassSpecCustomAnnotation> annotations) {
		((MSGlycanCustomAnnotationPreference) preferences).setCustomAnnotations(annotations);
	}

	@Override
	protected String[] getHeaderColumns() {
		return MSGlycanAnnotationSpecialPeaksFile.HEADER_COLUMN_LABELS;
	}

	@Override
	protected MassSpecCustomAnnotationPeak getNewMassSpecCustomAnnotationPeak() {
		return new MSGlycanAnnotationCustomAnnotationPeak();
	}

	@Override
	protected MassSpecCustomAnnotation getNewMassSpecCustomAnnotation() {
		return new MSGlycanAnnotationCustomAnnotation();
	}

	@Override
	protected void fillRow( GridItem gi, MassSpecCustomAnnotationPeak peak ) {
		super.fillRow(gi, peak);
		int j = 3;
		if (peak != null && peak instanceof MSGlycanAnnotationCustomAnnotationPeak) {
			gi.setText(j++, ((MSGlycanAnnotationCustomAnnotationPeak) peak).getPeakSequence() != null ? ((MSGlycanAnnotationCustomAnnotationPeak) peak).getPeakSequence() : "");
		}
	}

	@Override
	protected void fillPeak( GridItem gi, MassSpecCustomAnnotationPeak peak ) {
		if( isBlankRow(gi) ) {
			return;
		}
		super.fillPeak(gi, peak);
		int j = 3;
		((MSGlycanAnnotationCustomAnnotationPeak) peak).setPeakSequence( gi.getText(j) );
	}

	@Override
	protected int getDefaultColumnWidth( int _iColNum ) {
		if( _iColNum < 3 ) { 
			return super.getDefaultColumnWidth(_iColNum);
		} else if ( _iColNum == 3 ) { // sequence
			return 200;
		} 
		return 50; // ??
	}

	@Override
	protected void readXMLFile(String sPath) {
		currentCustomAnnotation = MSGlycanAnnotationSpecialPeaksFile.readXMLFile(sPath);
	}

	@Override
	protected void readTxtFile(String sPath) {
		MSGlycanAnnotationSpecialPeaksFile.readFile(currentCustomAnnotation, sPath);
	}

	@Override
	protected void writeXMLFile(String sPath) {
		MSGlycanAnnotationSpecialPeaksFile.writeXMLFile(currentCustomAnnotation, sPath);
	}

	@Override
	protected void showInvalidImportFileMessage() {
		ErrorUtils.createErrorMessageBox(getShell(), "Not a valid MS Glycan Custom Annotation XML file.");
	}
}
