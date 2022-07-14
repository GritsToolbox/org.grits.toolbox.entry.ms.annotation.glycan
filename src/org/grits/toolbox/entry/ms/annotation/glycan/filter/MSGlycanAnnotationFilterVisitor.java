package org.grits.toolbox.entry.ms.annotation.glycan.filter;

import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.ms.om.data.IntensityFilter;
import org.grits.toolbox.ms.om.data.MSFilterVisitor;
import org.grits.toolbox.util.structure.glycan.filter.GlycanFilterException;
import org.grits.toolbox.util.structure.glycan.filter.om.GlycanFilterVisitor;

public class MSGlycanAnnotationFilterVisitor extends GlycanFilterVisitor implements MSFilterVisitor {
	
	MSAnnotationTable table;
	Integer rowNumber;
	
	public boolean evaluate(MSAnnotationTable table, Integer index, Sugar sugar) throws GlycanFilterException {
		this.m_sugar = sugar;
		this.table = table;
		this.rowNumber = index;
        if ( this.m_filter == null )
        {
            throw new GlycanFilterException("No filter is set");
        }
        return this.m_filter.accept(this);
	}
	
	public boolean visit(IntensityFilter intensityFilter) throws GlycanFilterException {
		if (this.table == null || this.rowNumber == null)
			throw new GlycanFilterException("No data provided");
		
		// find the column index
		int columnIndex = table.getColumnIndexForKey(intensityFilter.getIntensityColumnKey());
		if (columnIndex == -1) // try using contains option????
			columnIndex = table.getColumnIndexForKeyByContains(intensityFilter.getIntensityColumnKey());
		if (columnIndex == -1) 
			throw new GlycanFilterException("Column " + intensityFilter.getIntensityColumnKey() + " does not exist in the table");
		
		// get the intensity value from the table for the given row
		Double intensityValue = (Double) table.getBottomDataLayer().getDataValueByPosition(columnIndex, this.rowNumber);
		if (intensityFilter.getMin() != null) {
            if (intensityFilter.getMin() > intensityValue) {
                return false;
            }
        }
        if (intensityFilter.getMax() != null) {
            if (intensityFilter.getMax() < intensityValue) {
                return false;
            }
        }
        return true;
	}

	public MSAnnotationTable getTable() {
		return table;
	}

	public void setTable(MSAnnotationTable table) {
		this.table = table;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}
}
