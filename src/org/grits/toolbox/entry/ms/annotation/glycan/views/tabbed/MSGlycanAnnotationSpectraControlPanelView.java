package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSpectraControlPanelView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraView;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Scan;

public class MSGlycanAnnotationSpectraControlPanelView extends MSAnnotationSpectraControlPanelView {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSpectraControlPanelView.class);
	protected Button cbLabelsAsImages = null;
	protected MSGlycanAnnotationEntityScroller entityScroller = null;
	private int iMyCurView = -1;
	
	public MSGlycanAnnotationSpectraControlPanelView(MassSpecSpectraView parentView) {
		super(parentView);
	}
		
	public MSGlycanAnnotationEntityScroller getEntityScroller() {
		return entityScroller;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createPartControl(parent);
	}
	
	@Override
	protected GridLayout getNewGridLayout() {
		MSGlycanAnnotationSpectraView view = (MSGlycanAnnotationSpectraView) getParentView();
		if( view.getDetailsView() == null ) 
			return super.getNewGridLayout();
		return new GridLayout(4, false);
	}

	protected void setMSGlycanAnnotationElements() {
		MSGlycanAnnotationSpectraView view = (MSGlycanAnnotationSpectraView) getParentView();
		if( view.getDetailsView() == null ) 
			return;
		entityScroller = getNewMSAnnotationEntityScroller();
		RowLayout rowLayout = new RowLayout();
		entityScroller.setLayout(rowLayout);
		entityScroller.createPartControl(entityScroller);			
		entityScroller.getNextButton().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				goNext();
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		entityScroller.getPrevButton().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				goPrev();
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		entityScroller.addPaintListener(new PaintListener() {			
			@Override
			public void paintControl(PaintEvent e) {
				updateView();
				entityScroller.removePaintListener(this);
				
			}
		});
		GridData gd = new GridData(SWT.END, SWT.BEGINNING, true, true, 1, 4);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		entityScroller.reDraw();
		entityScroller.setLayoutData(gd);
	}
	
	@Override
	protected void addElements() {
		setMSElements();
		setMSGlycanAnnotationElements();
		setPickedPeaksElements();
		setAnnotatedElements();
		setUnAnnotatedElements();
	}
	
	
	public void setCurViewIndex(int iMyCurView) {
		this.iMyCurView = iMyCurView;
	}
	
	public int getCurViewIndex() {
		return iMyCurView;
	}
	
	protected MSGlycanAnnotationEntityScroller getNewMSAnnotationEntityScroller() {
		MSGlycanAnnotationSpectraView view = (MSGlycanAnnotationSpectraView) getParentView();
		return new MSGlycanAnnotationEntityScroller(this.parent, SWT.None, view.getDetailsView(), view.getDetailsView().getCartoonOptions());
	}
	
	@Override
	public void updateView() {
		try {
			MSGlycanAnnotationSpectraView view = (MSGlycanAnnotationSpectraView) getParentView();
			if( view.getCurrentPeaksView() == null ) 
				return;
			view.setPeakListTableProcessor( (MSAnnotationTableDataProcessor) view.getCurrentPeaksView().getTableDataProcessor());
			view.createThisChart();
			Feature curFeature = view.getDetailsView().getFeatures().get(this.iMyCurView);
			Annotation annot =  ((MSAnnotationTableDataProcessor) view.getCurrentPeaksView().getTableDataProcessor()).getAnnotation(curFeature.getAnnotationId());
			Scan scan = ((MSAnnotationTableDataProcessor) view.getCurrentPeaksView().getTableDataProcessor()).getScan(view.getDetailsView().getMsEntityProperty().getScanNum());
			if( entityScroller == null ) 
				return;
			entityScroller.setFeature( curFeature );
			entityScroller.setAnnotation( annot );
			entityScroller.setScan(scan);
			entityScroller.reDraw();
		} catch ( Exception e ) {
			logger.error("Failed attempt to update view.", e);
		}		
	}

	public void goNext() {
		try {
			this.iMyCurView++;
			MSGlycanAnnotationSpectraView view = (MSGlycanAnnotationSpectraView) getParentView();
			if( this.iMyCurView >= view.getDetailsView().getPeakComposites().size() ) {
				this.iMyCurView = 0;
			}
			updateView();
		} catch ( Exception e ) {
			logger.error("Attempted to go to next page when at end of list", e);
		}
	}

	public void goPrev() {
		try {
			this.iMyCurView--;
			MSGlycanAnnotationSpectraView view = (MSGlycanAnnotationSpectraView) getParentView();
			if( this.iMyCurView < 0 ) {
				this.iMyCurView = view.getDetailsView().getPeakComposites().size() - 1;
			}
			updateView();
		} catch ( Exception e ) {
			logger.error("Attempted to go to prev page when at end of list", e);
		}
	}
		
	@Focus
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
