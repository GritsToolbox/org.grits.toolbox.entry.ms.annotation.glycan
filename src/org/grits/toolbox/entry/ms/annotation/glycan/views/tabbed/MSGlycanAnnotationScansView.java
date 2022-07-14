package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.glycan.process.loader.MSGlycanAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationScansView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSGlycanAnnotationScansView extends MSAnnotationScansView {

	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationScansView.class);
	public static final String VIEW_ID = "plugin.ms.annotation.views.MSGlycanAnnotationScansView"; //$NON-NLS-1$
	private MPart part;

	@Inject
	public MSGlycanAnnotationScansView(Entry entry, Property msEntityProperty,
			@Named(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT) int iMinMSLevel) {
		super(entry, msEntityProperty, iMinMSLevel);
	}
	
	@PostConstruct
	public void postConstruct (MPart part) {
		this.part = part;
	}

	public MPart getPart() {
		return part;
	}

	@Override
	public String toString() {
		return "MSGlycanAnnotationScansView (" + entry + ")";
	}	
	
	@Override
	protected void initResultsView( Composite parent ) throws Exception {
		compositeTop = new Composite(parent, SWT.BORDER);
		compositeTop.setLayout(new GridLayout(1,false));

		try {
			resultsComposite = getNewResultsComposite(compositeTop, SWT.NONE);
			( (MSGlycanAnnotationResultsComposite) resultsComposite).createPartControl(this.compositeTop, this, this.entityProperty, this.dataProcessor, FillTypes.Scans);
			resultsComposite.setLayout(new FillLayout());
			this.viewBase = resultsComposite.getBaseView();
		} catch( Exception e ) {
			viewBase = null;
			resultsComposite = null;
			logger.error("Error in MassSpecScansView: initResultsView");
			throw new Exception(e.getMessage());
		}		
	}
	

	@Override
	protected MSGlycanAnnotationResultsComposite getNewResultsComposite( Composite composite, int style ) {
		return new MSGlycanAnnotationResultsComposite(composite, style);
	}
		
	@Override
	protected TableDataProcessor getNewTableDataProcessor( Entry entry, Property entityProperty) {
		MSGlycanAnnotationTableDataProcessor proc = new MSGlycanAnnotationTableDataProcessor(
				entry, entityProperty, 
				FillTypes.Scans, getMinMSLevel() );
		proc.initializeTableDataObject(entityProperty);
		return proc;
	}

	@Override
	protected TableDataProcessor getNewTableDataProcessor(Property entityProperty) {		
		MSGlycanAnnotationMultiPageViewer parentViewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(part.getContext(), getEntry().getParent());
		if( parentViewer == null || parentViewer.getScansView() == null ) {
			return null;
		}
		TableDataProcessor parentProc = parentViewer.getScansView().getTableDataProcessor();
		if( parentProc == null ) 
			return null;
//		if ( ! parentProc.getSourceProperty().equals(entityProperty) ) {
//			return null;
//		}
		MSGlycanAnnotationTableDataProcessor proc = new MSGlycanAnnotationTableDataProcessor(parentProc, entityProperty, 
				FillTypes.Scans, getMinMSLevel());
		proc.initializeTableDataObject(entityProperty);
		proc.setEntry(getEntry());
		return proc;
	}	
}
