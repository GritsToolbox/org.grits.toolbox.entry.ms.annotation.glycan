package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;

import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationResultsComposite;

public class MSGlycanAnnotationSummaryResultsComposite extends MSAnnotationResultsComposite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MSGlycanAnnotationSummaryResultsComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	public void createPartControl(Composite parent, EntryEditorPart parentEditor, Property entityProprty, 
			TableDataProcessor dataProcessor, FillTypes fillType ) throws Exception {
		this.baseView = new MSGlycanAnnotationSummaryTableBase(parent, parentEditor, entityProprty, dataProcessor, fillType);	
		this.baseView.initializeTable();
		this.baseView.layout();
	}
	
}
