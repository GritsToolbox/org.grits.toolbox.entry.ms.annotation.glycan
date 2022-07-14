package org.grits.toolbox.entry.ms.annotation.glycan.process.loader;

import org.apache.log4j.Logger;

import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
public class MSGlycanAnnotationSummaryImageConversion extends MSGlycanAnnotationImageConversion{
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSummaryImageConversion.class);

	@Override
	public boolean convertImages() {
//		boolean bParentSuccess = super.convertImages();
//		if( ! bParentSuccess ) 
//			return false;
		try{
			updateListeners("Converting images.", 0);
			for( int i = 0; i < msAnnotationTableData.getLastHeader().size(); i++ ) {
				updateListeners("Converting images. Row: " + (i+1) + " of " + msAnnotationTableData.getLastHeader().size(), i+1);
				GRITSColumnHeader header = msAnnotationTableData.getLastHeader().get(i);
				if( header.getLabel().endsWith(".png") ) {
					Object cartoon = header.getLabel();
					if ( cartoon == null )
						continue;

					int iInx = cartoon.toString().indexOf(".png");
					if( iInx < 0 )
						continue;
					String sSequence = cartoon.toString().substring(0, iInx);

					MSGlycanAnnotationTableDataObject.glycanImageProvider.addMergeImageToProvider(sSequence, cartoon.toString()); 
				}
			}
			logger.debug("Current image cache size: " + MSGlycanAnnotationTableDataObject.glycanImageProvider.getCacheSize() + ", stack size: " + MSGlycanAnnotationTableDataObject.glycanImageProvider.getStackSize());
			return true;
		}catch(Exception e)	{
			logger.error(e.getMessage(), e);
		}
		return false;
	
	}
}
