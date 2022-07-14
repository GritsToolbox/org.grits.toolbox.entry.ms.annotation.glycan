package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;

public class MSGlycanAnnotationSummaryLastHeaderOverrideLabelAccumulator<T> extends ColumnOverrideLabelAccumulator {
	private List<Integer> alCartoonCols = null;
	protected DataLayer dataLayer = null;
	public MSGlycanAnnotationSummaryLastHeaderOverrideLabelAccumulator(
			DataLayer dataLayer, List<Integer> alCartoonCols ) {
		super(dataLayer);
		this.alCartoonCols = alCartoonCols;
		this.dataLayer = dataLayer;
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels,
			int columnPosition, int rowPosition) {
		Object rowObj =  this.dataLayer.getDataValueByPosition(columnPosition, rowPosition);
		if ( rowObj == null )
			return;
		
		if( alCartoonCols.contains( columnPosition ) )
		{
			String sCartoonFile = rowObj.toString();
			if ( sCartoonFile == null || sCartoonFile.equals("") ) 
				return;
			configLabels.addLabel(sCartoonFile);
		} 
	}
	
}
