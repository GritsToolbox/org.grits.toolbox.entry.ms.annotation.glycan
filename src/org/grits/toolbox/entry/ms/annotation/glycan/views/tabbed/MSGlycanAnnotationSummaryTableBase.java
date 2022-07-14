package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;

import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers.MSGlycanAnnotationSummaryTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationTableBase;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;


public class MSGlycanAnnotationSummaryTableBase extends MSGlycanAnnotationTableBase {	
	public MSGlycanAnnotationSummaryTableBase( Composite parent, EntryEditorPart parentEditor, 
			Property entityProperty, TableDataProcessor dataProcessor, FillTypes fillType ) throws Exception {
		super(parent, parentEditor, entityProperty, dataProcessor, fillType);
	}
			
	@Override
	public GRITSTable getNewSimianTable( MassSpecTableBase _viewBase, TableDataProcessor _extractor ) throws Exception {
		return new MSGlycanAnnotationSummaryTable( (MSAnnotationTableBase) _viewBase, _extractor);
	}
	
}
