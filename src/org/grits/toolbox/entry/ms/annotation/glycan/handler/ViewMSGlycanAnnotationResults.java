package org.grits.toolbox.entry.ms.annotation.glycan.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.ICancelableEditor;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;


/**
 * Create a new MS dialog
 * @author dbrentw
 *
 */
public class ViewMSGlycanAnnotationResults {
	
	//log4J Logger
	private static final Logger logger = Logger.getLogger(ViewMSGlycanAnnotationResults.class);

//	public final static String VIEW_ID = "plugin.ms.annotation.glycan.views.MSGlycanAnnotationMultiPageViewer";
	
	public static final String PARAMETER_ID = "viewAnnotationResults_Entry";
	public static final String COMMAND_ID = "org.grits.toolbox.entry.ms.handler.viewMSGlycanAnnotationResults";
	
	@Inject static IGritsDataModelService gritsDataModelService = null;
    @Inject static IGritsUIService gritsUIService = null;
    @Inject EPartService partService;

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell, 
			@Optional @Named (PARAMETER_ID) Entry entry) {
		if ( entry == null ) {
			Entry selectedEntry = null;
			if(object instanceof Entry)
			{
				selectedEntry = (Entry) object;
			}
			else if (object instanceof StructuredSelection)
			{
				if(((StructuredSelection) object).getFirstElement() instanceof Entry)
				{
					selectedEntry = (Entry) ((StructuredSelection) object).getFirstElement();
				}
			}
			// try getting the last selection from the data model
			if(selectedEntry == null
					&& gritsDataModelService.getLastSelection() != null
					&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
			{
				selectedEntry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
			}
			entry = selectedEntry;
		}
		showPlugInView(shell, entry);
	}
		
	private void showPlugInView(Shell shell, Entry entry) {
		
		if(entry != null)
		{
			MPart part = null;
			try {
				part = gritsUIService.openEntryInPart(entry);
				if (part != null && part.getObject() != null && part.getObject() instanceof ICancelableEditor) {
					if ( ((ICancelableEditor) part.getObject()).isCanceled()) {
						partService.hidePart(part, true);
					}
				}
			}
			catch (Exception e) {
				Exception pie = new Exception("There was an error converting the XML to a table.", e);
				logger.error(pie.getMessage(),pie);
				ErrorUtils.createErrorMessageBox(shell, "Unable to open the results viewer", pie);
				if (part != null)
					partService.hidePart(part, true);
			}
		}
	}
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object, @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		Entry entry = null;
		if(object instanceof Entry) {
			entry = (Entry) object;
		}
		else if (object instanceof StructuredSelection) {
			if(((StructuredSelection) object).getFirstElement() instanceof Entry) {
				entry = (Entry) ((StructuredSelection) object).getFirstElement();
			}
		}
		if (entry == null && gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry) {
			// try getting the last selection from the data model
			entry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
		}
		
        if (entry != null) {
        	if ( entry.getProperty().getType().equals( MSGlycanAnnotationProperty.TYPE )  ||
        			entry.getProperty() instanceof MSAnnotationEntityProperty) 
            	return true;
        }
        
        if (part != null && part.getObject() instanceof MSGlycanAnnotationMultiPageViewer)
        	return true;
        else { // try to find an open part of the required type
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						return true;
					}
				}
			}
    	}
		return false;
	}
}
