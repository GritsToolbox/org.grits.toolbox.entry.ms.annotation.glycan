package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationStandardQuantFileGrid;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSGlycanAnnotationStandardQuantFileGrid extends MSAnnotationStandardQuantFileGrid {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationStandardQuantFileGrid.class);

	public MSGlycanAnnotationStandardQuantFileGrid(Composite parent, MassSpecMultiPageViewer contextViewer) {
		super(parent, contextViewer);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the current open MSAnnotationMultiPageViewer
	 */
	@Override
	public MassSpecMultiPageViewer getCurrentViewer() {
		try {
			EPartService partService = getContextViewer().getPartService();
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) mPart.getObject();
						if(viewer != null && viewer.getEntry().getProperty() != null && viewer.getEntry().getProperty() instanceof MSAnnotationEntityProperty ) {						
							return viewer;
						}
					}
				}
			}	
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * Returns the MassSpecUISettings object to be used to list which files are associated with the entry.
	 * @return the MassSpecUISettings for the current entry
	 */
	protected MassSpecUISettings getEntrySettings() {
		MSGlycanAnnotationProperty prop = (MSGlycanAnnotationProperty) getEntryParentProperty();
		MSGlycanAnnotationMetaData msSettings = (MSGlycanAnnotationMetaData) prop.getMSAnnotationMetaData();
		return msSettings;
	}

}
