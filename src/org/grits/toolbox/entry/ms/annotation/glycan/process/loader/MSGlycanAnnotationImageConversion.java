package org.grits.toolbox.entry.ms.annotation.glycan.process.loader;

import org.apache.log4j.Logger;
import org.grits.toolbox.datamodel.ms.annotation.glycan.report.tablemodel.MSGlycanAnnotationReportTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.utils.image.ImageCreationException;
import org.grits.toolbox.widgets.tools.NotifyingProcess;
public class MSGlycanAnnotationImageConversion extends NotifyingProcess {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationImageConversion.class);

	protected MSGlycanAnnotationTableDataObject msAnnotationTableData = null;
	private String sReportName = null;

	public boolean convertImages() {
		updateListeners("Converting images.", 0);
		for (int i = 0; i < msAnnotationTableData.getTableData().size(); i++ ) {
			try {
				if( ((i+1)%100) == 0 ) {
					updateListeners("Converting images. Row: " + (i+1) + " of " + msAnnotationTableData.getTableData().size(), i+1);
				}
				// note:  the # sequence cols should equal the # glycan cols
				//				if ( msAnnotationTableData.getCartoonCols().size() > 1 ) {
				if( msAnnotationTableData instanceof MSGlycanAnnotationReportTableDataObject ) {
					for( int j = 0; j < msAnnotationTableData.getCartoonCols().size(); j++ ) {
						Object cartoon = msAnnotationTableData.getTableData().get( i ).getDataRow()
								.get( msAnnotationTableData.getCartoonCols().get(j) );
						if ( cartoon == null )
							continue;

						int iInx = cartoon.toString().indexOf(".png");
						if( iInx < 0 )
							continue;
						String sSequence = cartoon.toString().substring(0, iInx);

						try {
							MSGlycanAnnotationTableDataObject.glycanImageProvider.addMergeImageToProvider(sSequence, cartoon.toString());
						} catch( ImageCreationException ice ) {
							updateErrorListener("Error creating image: " + (i+1));
						}
					}
				} else {
					for (int j = 0; j < msAnnotationTableData.getSequenceCols().size(); j++) {
						int iSeqCol = msAnnotationTableData.getSequenceCols().get(j);
						int iCartoonCol = msAnnotationTableData.getCartoonCols().get(j);
						GRITSListDataRow row = msAnnotationTableData.getTableData().get(i);

						Object sequence = row.getDataRow().get(iSeqCol);
						Object glycanID = row.getDataRow().get(iCartoonCol);

						if (sequence == null || glycanID == null)
							continue;
						int iInx = ((String) sequence).indexOf(".png");
						if( iInx > 0 ) {
							sequence = ((String) sequence).substring(0, iInx);
						}
						try {
							if ( ((String) sequence).contains("~|~") ) {
								MSGlycanAnnotationTableDataObject.glycanImageProvider.addMergeImageToProvider(sequence.toString(), glycanID.toString()); 
							} else {
								MSGlycanAnnotationTableDataObject.glycanImageProvider.addImageToProvider(sequence.toString(), glycanID.toString());
							}
						} catch( ImageCreationException ice ) {
							updateErrorListener("Error creating image: " + (i+1));
						}
					}
					/* 
					 * DBW 10/03/16: This just wasn't working right yet. It would display cartoons so long as you didn't rearrange columns.
					 * Saving for later if desired
					 *
					for( int j = 0; j < msAnnotationTableData.getExtraCartoonCols().size(); j++ ) {
						Object oSequenceId = msAnnotationTableData
								.getTableData()
								.get(i).getDataRow()
								.get(msAnnotationTableData.getExtraCartoonCols().get(j));
						if (oSequenceId == null)
							continue;
						String sequence = (String) oSequenceId;
						int iInx = ((String) sequence).indexOf(".png");
						if( iInx > 0 ) {
							sequence = ((String) sequence).substring(0, iInx);
						}

						try {
							MSGlycanAnnotationTableDataObject.glycanImageProvider.addImageToProvider(sequence, (String) oSequenceId);
						} catch( ImageCreationException ice ) {
							updateErrorListener("Error creating image: " + (i+1));
						}						
					}
					 */
				}
			} catch( Exception ex ) {
				logger.error(ex.getMessage(), ex);
			}
		}
		logger.debug("Current image cache size: " + MSGlycanAnnotationTableDataObject.glycanImageProvider.getCacheSize() + ", stack size: " + 
				MSGlycanAnnotationTableDataObject.glycanImageProvider.getStackSize());
		return true;

	}

	public void setSimianTableData(MSGlycanAnnotationTableDataObject msAnnotationTableData) {
		this.msAnnotationTableData = msAnnotationTableData;
	}

	public String getReportName() {
		return sReportName;
	}

	public void setReportName(String sReportName) {
		this.sReportName = sReportName;
	}

}
