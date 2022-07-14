package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import java.io.File;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationTableBase;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;


public class MSGlycanAnnotationTableBase extends MSAnnotationTableBase {	
	public MSGlycanAnnotationTableBase( Composite parent, EntryEditorPart parentEditor, 
			Property entityProperty, TableDataProcessor dataProcessor, FillTypes fillType ) throws Exception {
		super(parent, parentEditor, entityProperty, dataProcessor, fillType);
	}

	@Override
	public void initializeTable() throws Exception {
		this.natTable = (MSGlycanAnnotationTable) getNewSimianTable(this, dataProcessor);				
		//this.natTable.setMzXMLPathName( ( (MassSpecProperty) ( (MassSpecEntityProperty) getEntityProperty()).getMassSpecParentProperty() ).getFullyQualifiedMzXMLFileName(this.parentEditor.getEntry()));
		this.natTable.loadData();
		this.natTable.setMzXMLPathName(((MassSpecTableDataProcessor) dataProcessor).getMSPath() + File.separator +
				((MassSpecTableDataProcessor) dataProcessor).getMSSourceFile().getName());
		this.natTable.createMainTable();
	}

	@Override
	public GRITSTable getNewSimianTable( MassSpecTableBase _viewBase, TableDataProcessor _extractor ) throws Exception {
		return new MSGlycanAnnotationTable( (MSAnnotationTableBase) _viewBase, _extractor);
	}

	/*protected static void processView( Object view ) {
		if( view instanceof IMSAnnotationPeaksViewer ) {
			try {
				MSGlycanAnnotationSelectionView sv = (MSGlycanAnnotationSelectionView) ((IMSAnnotationPeaksViewer) view).getCurrentSelectionView();
				if( sv != null && sv.getSubTable() != null ) {
					((MSGlycanAnnotationTable) sv.getSubTable()).refreshTableImages();
				}
			} catch( Exception e ) {
				e.printStackTrace();
			}

		} 
		if ( view instanceof IMSPeaksViewer ) {
			try {
				MassSpecTableBase viewBase = (MassSpecTableBase) ( (IMSPeaksViewer) view ).getViewBase();
				((MSGlycanAnnotationTable) viewBase.getNatTable()).refreshTableImages();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} 
		if ( view instanceof MSGlycanAnnotationMultiPageViewer ) {
			MSGlycanAnnotationMultiPageViewer overview = (MSGlycanAnnotationMultiPageViewer) view;
			for( int j = 0; j < overview.getPageCount(); j++) {
				Object obj = overview.getPageItem(j);
				processView(obj);
			}
		} else if( view instanceof MSGlycanAnnotationDetails ) {
			MSGlycanAnnotationDetails ad = (MSGlycanAnnotationDetails) view;
			if( ad.getPeaksViews() != null ) {
				for( int k = 0; k < ad.getPeaksViews().size(); k++ ) {
					MSGlycanAnnotationPeaksView pv = (MSGlycanAnnotationPeaksView) ad.getPeaksViews().get(k);
					processView(pv);
				}
				MSGlycanAnnotationEntityScroller entityScroller = (MSGlycanAnnotationEntityScroller) ad.getEntityScroller();
				entityScroller.refreshImages();
			}

		}
	}*/

	// no need to push cartoon preference changes
	/*public static void propigateImagePreferences() {
		try {
			for( int win = 0; win < PlatformUI.getWorkbench().getWorkbenchWindowCount(); win++ ) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[win];	
				IWorkbenchPage page = window.getActivePage();
				IEditorReference[] editors = page.getEditorReferences();
				for( int i = 0; i < editors.length; i++ ) {
					IEditorPart editorPart = editors[i].getEditor(false);
					processView(editorPart);
				}
			}
		}catch( Exception ex ) {
			//			logger.error("Error propigating images.", ex);
			ex.printStackTrace();
		}
	}*/
}
