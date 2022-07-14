package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataProvider;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationCellOverrideLabelAccumulator;

public class MSGlycanAnnotationCellOverrideLabelAccumulator_ForExtraCartoons<T> extends
MSAnnotationCellOverrideLabelAccumulator<T> {

	private List<List<Integer>> alCartoonCols = null;

	public MSGlycanAnnotationCellOverrideLabelAccumulator_ForExtraCartoons(IRowDataProvider<T> dataProvider, List<Integer> alCartoonCols ) {
		super(dataProvider);
		addCellOverrideLabelAccumulator(alCartoonCols);
		//		this.alCartoonCols = alCartoonCols;
		this.iSelectedCol = null;
	}

	public MSGlycanAnnotationCellOverrideLabelAccumulator_ForExtraCartoons(IRowDataProvider<T> dataProvider, List<Integer> alCartoonCols, Integer iSelectedCol ) {
		super(dataProvider);
		addCellOverrideLabelAccumulator(alCartoonCols);
		//		this.alCartoonCols = alCartoonCols;
		this.iSelectedCol = iSelectedCol;
	}

	public void addCellOverrideLabelAccumulator(List<Integer> alCartoonCols) {
		if( this.alCartoonCols == null ) {
			this.alCartoonCols = new ArrayList<List<Integer>>();
		}
		this.alCartoonCols.add(alCartoonCols);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		GRITSListDataRow rowObj = ((GRITSListDataProvider) dataProvider).getGRITSListDataRow(rowPosition);
		if ( rowObj == null ) 
			return;

		for( List<Integer> cartoonList : this.alCartoonCols ) {		
			if(cartoonList.contains( columnPosition ) ) {
				String sCartoonFile = (String) rowObj.getDataRow().get(columnPosition);
				if ( sCartoonFile == null || sCartoonFile.equals("") || ! sCartoonFile.endsWith(".png")) 
					continue;
				configLabels.addLabel(sCartoonFile);
				return;
//			} else if ( iSelectedCol != null && columnPosition == iSelectedCol ) {
//				configLabels.addLabel( TableDataProcessor.selColHeader.getLabel() );
			}
		}
	}
}
