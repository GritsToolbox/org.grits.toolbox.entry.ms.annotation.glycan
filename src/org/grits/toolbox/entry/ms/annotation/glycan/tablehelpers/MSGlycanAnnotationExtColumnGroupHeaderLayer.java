package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class MSGlycanAnnotationExtColumnGroupHeaderLayer extends ColumnGroupHeaderLayer {
	private final ColumnGroupModel model;
	private final ILayer columnHeaderLayer;
	private final ILayer columnGroupHeaderLayer;

	public MSGlycanAnnotationExtColumnGroupHeaderLayer(ILayer columnHeaderLayer,
			SelectionLayer selectionLayer, ILayer columnGroupHeaderLayer, ColumnGroupModel columnGroupModel) {
		super(columnHeaderLayer, selectionLayer, columnGroupModel);
		
		this.model = columnGroupModel;
		this.columnHeaderLayer = columnHeaderLayer;
		this.columnGroupHeaderLayer = columnGroupHeaderLayer;
	}

//	@Override
//	public void setConfigLabelAccumulator(
//			IConfigLabelAccumulator cellLabelAccumulator) {
//		super.setConfigLabelAccumulator(cellLabelAccumulator);
//	}
	
	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			String mode = columnGroupHeaderLayer.getDisplayModeByPosition(columnPosition, rowPosition);
			if( mode != null )
				return mode;
			return DisplayMode.NORMAL;
		} else {
			return columnHeaderLayer.getDisplayModeByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			LabelStack stack = columnGroupHeaderLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
			if( stack != null ) 
				return stack;
			return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
		} else {
			return columnHeaderLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			return model.getColumnGroupByIndex(columnIndex).getName();
		} else {
			return columnHeaderLayer.getDataValueByPosition(columnPosition, 0);
		}
	}

	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		int columnIndex = getColumnIndexByPosition(getColumnPositionByX(x));
		if (model.isPartOfAGroup(columnIndex) && y < getRowHeightByPosition(0)) {
			LabelStack stack = columnGroupHeaderLayer.getRegionLabelsByXY(x, y - getRowHeightByPosition(0));
			if( stack != null ) 
				return stack;
			return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
		} else {
			return columnHeaderLayer.getRegionLabelsByXY(x, y - getRowHeightByPosition(0));
		}
	}
	
}
