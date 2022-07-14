package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataProvider;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.DoubleFormat;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationCellOverrideLabelAccumulator;

public class MSGlycanAnnotationCellOverrideLabelAccumulator<T> extends
	MSAnnotationCellOverrideLabelAccumulator<T> {
	private List<Integer> alCartoonCols = null;
	
	public MSGlycanAnnotationCellOverrideLabelAccumulator(IRowDataProvider<T> dataProvider, List<Integer> alCartoonCols, Integer filterCol, Integer commentCol, Integer ratioCol, List<Integer> intensityCols) {
		super(dataProvider, null, filterCol, commentCol, ratioCol, intensityCols);
		this.alCartoonCols = alCartoonCols;
	}

	public MSGlycanAnnotationCellOverrideLabelAccumulator(IRowDataProvider<T> dataProvider, List<Integer> alCartoonCols, Integer iSelectedCol, Integer filterCol, Integer commentCol, Integer ratioCol, List<Integer> intensityCols) {
		super(dataProvider, iSelectedCol, filterCol, commentCol, ratioCol, intensityCols);
		this.alCartoonCols = alCartoonCols;
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		super.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
		GRITSListDataRow rowObj = ((GRITSListDataProvider) dataProvider).getGRITSListDataRow(rowPosition);
		if ( rowObj == null ) 
			return;
		if (filterCol != null) {
			Integer filterVal = (Integer) rowObj.getDataRow().get(filterCol);
			if (filterVal != null && filterVal.equals(11)) { // MATCH
				configLabels.addLabel(GRITSTable.FILTEREDSELECTED);
			} else if (filterVal != null && filterVal.equals(1)) { // there is a MATCH in candidates
				configLabels.addLabel(GRITSTable.FILTEREDNOTSELECTED);
			}
		}
		
		if (filterCol != null && columnPosition == filterCol) {
			Integer filterVal = (Integer) rowObj.getDataRow().get(filterCol);
			if (filterVal == null || filterVal.intValue() < 0) { // NOT FILTERED
				configLabels.addLabel(GRITSTable.NOTFILTERED);
			}
			else if (filterVal != null && filterVal.equals(0)) { // NO MATCH
				configLabels.addLabel(GRITSTable.NOMATCHLABEL);
			}
			else if (filterVal != null && filterVal.equals(11)) { // MATCH
				configLabels.addLabel(GRITSTable.FILTEREDSELECTEDLABEL);
			} else if (filterVal != null && filterVal.equals(1)) { // there is a MATCH in candidates
				configLabels.addLabel(GRITSTable.FILTEREDNOTSELECTEDLABEL);
			}
		}
		
		if(alCartoonCols.contains( columnPosition ) )
		{
			String sCartoonFile = (String) rowObj.getDataRow().get(columnPosition);
			if ( sCartoonFile == null || sCartoonFile.equals("") ) {
				return;
			}
			configLabels.addLabel(sCartoonFile);
		} else if ( iSelectedCol != null && columnPosition == iSelectedCol ) {
			configLabels.addLabel( TableDataProcessor.selColHeader.getLabel() );
		} 
		
		if (intensityCols != null && intensityCols.contains(columnPosition)) {
			configLabels.addLabel(DoubleFormat.SCIENTIFIC_NOTATION.name());
		}
	}
}
