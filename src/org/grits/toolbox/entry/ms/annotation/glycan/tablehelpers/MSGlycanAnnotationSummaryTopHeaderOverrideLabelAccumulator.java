package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;

public class MSGlycanAnnotationSummaryTopHeaderOverrideLabelAccumulator<T> extends ColumnOverrideLabelAccumulator {
	protected DataLayer dataLayer = null;
	protected ColumnGroupHeaderLayer groupHeaderLayer = null;
	public MSGlycanAnnotationSummaryTopHeaderOverrideLabelAccumulator(
			DataLayer dataLayer, ColumnGroupHeaderLayer groupHeaderLayer ) {
		super(dataLayer);
		this.dataLayer = dataLayer;
		this.groupHeaderLayer = groupHeaderLayer;
	}
	
	public int getGroupedColPosition( int columnPosition, int rowPosition ) {
		Object grpObj = this.groupHeaderLayer.getDataValueByPosition(columnPosition, rowPosition);
		if( grpObj == null )
			return -1;
		for( int i = 0; i < this.dataLayer.getColumnCount(); i++ ) {
			Object rowObj =  this.dataLayer.getDataValueByPosition(i, rowPosition);
			if( grpObj.equals(rowObj) ) 
				return i;
		}
		return -1;
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels,
			int columnPosition, int rowPosition) {
		int iGroupedColPos = getGroupedColPosition(columnPosition, rowPosition);
		if( iGroupedColPos < 0 )
			return;
		Object rowObj =  this.dataLayer.getDataValueByPosition(iGroupedColPos, rowPosition);
		if ( rowObj == null )
			return;
		
		if( rowObj != null )
		{
			String sStructureID = rowObj.toString();
//			if ( ! sStructureID.startsWith( MSGlycanAnnotationTable.GLYCAN_ID_PREFIX) ) 
			if ( ! isAnnotationStructureId(sStructureID) ) 
				return;
			configLabels.addLabel(sStructureID);
		} 
	}

	/**
	 * Return true if given String is structure ID.
	 * @param sStructureID - String to be checked as structure ID
	 * @return True if given String is structure ID
	 */
	protected boolean isAnnotationStructureId(String sStructureID) {
		return sStructureID.startsWith( MSGlycanAnnotationTable.GLYCAN_ID_PREFIX );
	}
}
