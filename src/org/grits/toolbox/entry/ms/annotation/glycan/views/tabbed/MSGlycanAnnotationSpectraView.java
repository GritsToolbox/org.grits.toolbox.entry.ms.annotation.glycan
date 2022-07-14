package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;

import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.display.control.spectrum.chart.GRITSSpectralViewerChart;
import org.grits.toolbox.entry.ms.annotation.glycan.spectrum.chart.MSGlycanAnnotationSpectralViewerChart;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationPeaksView;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSpectraView;
import org.grits.toolbox.entry.ms.exceptions.MSException;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraControlPanelView;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;

public class MSGlycanAnnotationSpectraView extends MSAnnotationSpectraView {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSpectraView.class);
	protected MSGlycanAnnotationDetails detailView = null;

	@Inject
	public MSGlycanAnnotationSpectraView( Entry entry ) {
		super( entry );
	}

	public void setDetailsView(MSGlycanAnnotationDetails curDetailsView) {
		this.detailView = curDetailsView;
	}

	public MSGlycanAnnotationDetails getDetailsView() {
		return detailView;
	}

	@Override
	protected Object getPeakLabel(Double dMz, Object oFeatureId, Object oLabel, String sFeatureSeq) {
		if( oFeatureId == null ) 
			return super.getPeakLabel(dMz, oFeatureId, oLabel, sFeatureSeq);

		try {
			GlycanImageObject gio = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(sFeatureSeq.toString());
			return gio;
		} catch (Exception e) {
			logger.error("Error getting image for chart!", e);
		}
		return null;
	}

	@Override
	protected GRITSSpectralViewerChart getNewSpectralViewerChart() {
		return new MSGlycanAnnotationSpectralViewerChart( this.sDescription, 
				this.iScanNum, this.iMSLevel, ! this.bIsCentroid, true, this.sID, this.dMz );
	}

	@Override
	protected MassSpecSpectraControlPanelView getNewSpectraControlPanel() {
		return new MSGlycanAnnotationSpectraControlPanelView(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createPartControl(parent);
	}

	@Override
	public void createView() throws MSException {
		// TODO Auto-generated method stub
		super.createView();
	}

	public MSAnnotationPeaksView getCurrentPeaksView() {
		if( getDetailsView() == null ) 
			return null;
		int iInx = getDetailsView().getCurViewIndex();
		if ( getControlPanel() != null ) {
			iInx = ((MSGlycanAnnotationSpectraControlPanelView) getControlPanel()).getCurViewIndex();
		}
		return getDetailsView().getPeaksViews().get( iInx );
	}	

	@Override
	public void createChart(Composite parent) throws MSException {
		if( getDetailsView() != null ) {
			super.setPeakListTableProcessor( (MSAnnotationTableDataProcessor) getCurrentPeaksView().getTableDataProcessor() ); 
		}
		super.createChart(parent);
		if( getDetailsView() == null ) {
			return;
		}
		((MSGlycanAnnotationSpectraControlPanelView) getControlPanel()).setCurViewIndex(getDetailsView().getCurViewIndex());
		( (MSGlycanAnnotationSpectraControlPanelView) getControlPanel()).updateView(); //sets bottom to current peak view and instantiates entityScroller
	}

	@Override
	protected int getPrefEntityScrollerWeight() {
		int iTopWeight = 280;
		try {
			if( ( (MSGlycanAnnotationSpectraControlPanelView) controlPanel).getEntityScroller() == null ){
				return super.getPrefEntityScrollerWeight();
			}
			int iTopHeight = ( (MSGlycanAnnotationSpectraControlPanelView) controlPanel).getEntityScroller().getSize().y;
			if( iTopHeight != 0 ) {
				int iFormHeight = sashForm.getSize().y;
				if( iFormHeight < iTopHeight ) 
					return iTopWeight;
				iTopWeight = (int) Math.ceil( ((double) iTopHeight / (double) iFormHeight) * 1000.0) + 30;
			}
		} catch( Exception ex ) {		
			logger.error("Error setting weights.", ex); 
		}
		return iTopWeight;
	}

	@Override
	protected void setWeights() {
		try {
			if( getDetailsView() == null ) {
				super.setWeights();
				return;
			}
			int[] dWeights = getSashWeights();
			sashForm.setWeights(dWeights);	
		} catch( IllegalArgumentException ex ) {
			// just ignore
		} catch( Exception ex ) {		
			logger.error("Error setting weights.", ex); 
		}
	}

}
