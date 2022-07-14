package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.ExtCheckBoxPainter;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.SharedCheckboxWidget;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationDetails;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationEntityScroller;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.utils.data.CartoonOptions;

public class MSGlycanAnnotationEntityScroller extends MSAnnotationEntityScroller {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationEntityScroller.class);

	protected MSGlycanAnnotationFigureCanvas imageCanvas = null;
	protected CartoonOptions cartoonOptions = null;
	protected List<String> sequences = null;
	
	public MSGlycanAnnotationEntityScroller(Composite parent, int style, 
			MSAnnotationDetails msAnnotationDetails,  CartoonOptions cartoonOptions) {
		super(parent, style, msAnnotationDetails);
		this.cartoonOptions = cartoonOptions;
	}

	@Override
	public String toString() {
		return "MSGlycanAnnotationEntityScroller (" + feature + ")";
	}

	public void setCartoonOptions(CartoonOptions cartoonOptions) {
		this.cartoonOptions = cartoonOptions;
		imageCanvas.updateCartoonOptions(getCartoonOptions());
	}

	public CartoonOptions getCartoonOptions() {
		return cartoonOptions;
	}

	public void setFeature(Feature glycanFeature) {
		super.setFeature(glycanFeature);
	}

	@Override
	public String getFeatureDesc() {
		final DecimalFormat df = new DecimalFormat("0.00");
		Annotation annot = getAnnotation();
		MSAnnotationEntityProperty prop = (MSAnnotationEntityProperty) ((MSGlycanAnnotationDetails) getMsAnnotationDetails()).getEntry().getProperty();
		String sDesc = MSAnnotationDetails.getLabelForCheckbox(annot.getStringId(), getFeature().getId(), prop.getMsLevel());
		Double dScore = annot.getScores().get( scan.getScanNo().toString() );
		if( dScore != null ) {
			String sScoreText = ", Score: " + df.format(dScore);
			sDesc += sScoreText;
		}

		return sDesc;
	}	

	public CLabel getNewCLabel(Composite parent, int style) {
		CLabel newLabel = super.getNewCLabel(parent, style);		
		newLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDown(MouseEvent e) {
				Feature feat = getFeature();
				Peak peak = ((MSGlycanAnnotationDetails) getMsAnnotationDetails()).getPeakFromFeature(feat);
				Integer iParentScanNum = ((MSGlycanAnnotationDetails) getMsAnnotationDetails()).getParentViewScanNum();
				MSAnnotationEntityProperty prop = (MSAnnotationEntityProperty) ((MSGlycanAnnotationDetails) getMsAnnotationDetails()).getEntry().getProperty();
				if (prop.getMsLevel() > 3)
					iParentScanNum = prop.getParentScanNum();    // actual scan number for MSn (for MS1, direct infusion, the parent scan number is always the first one)
				
				String sCheckBoxKey = MSAnnotationEntityScroller.getCombinedKeyForLookup(peak.getId(), feat.getId());
				if (iParentScanNum != null && peak.getId() != null) {
					MSAnnotationTable table = ((MSGlycanAnnotationDetails) getMsAnnotationDetails()).getParentSubsetTable();					
					String sRowId = Feature.getRowId(peak.getId(), iParentScanNum, ((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getUsesComplexRowId());
					if (table != null && table.getParentTable() != null &&
							table.getParentTable().getGRITSTableDataObject().isLockedPeak(iParentScanNum, sRowId)) {
						MessageDialog.openInformation(getShell(), "Disabled", "Selections are locked, cannot make changes. Please unlock if you still wish to change candidate selections!");
						return;
					}
				}
//				getCurrentPeaksView()
//				MSGlycanAnnotationMultiPageViewer msAnnotView1 = MSGlycanAnnotationMultiPageViewer.getActiveViewer( parentView.getParentEditor().getContext() );
//				MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(context, entry)
				((MSGlycanAnnotationDetails) getMsAnnotationDetails()).toggleParentSelectedRow(sCheckBoxKey);

				// no need to redraw here. there is automatic redraw after toggle
				//				reDrawLabel();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		return newLabel;
	}

	@Override
	public void reDrawLabel() {
		try {
			super.reDrawLabel();
			if( this.idLabel != null ) {
				SharedCheckboxWidget scw = getParentSharedCheckboxWidget();
				Feature feat = getFeature();
				Peak peak = ((MSGlycanAnnotationDetails) getMsAnnotationDetails()).getPeakFromFeature(feat);

				String sCheckBoxKey = MSAnnotationEntityScroller.getCombinedKeyForLookup(peak.getId(), feat.getId());
				ExtCheckBoxPainter ecbp = scw.getHtGlycanToCheckBox().get( sCheckBoxKey );
				if( ecbp != null ) {
					this.idLabel.setImage(ecbp.getCurCheckboxImage());
				}
			}
		} catch(Exception ex) {
			logger.error("Error in reDrawLabel.", ex);
		}
	}

	@Override
	protected void drawLabel() {
		super.drawLabel();
	}

	@Override
	public void reDraw() {
		super.reDraw();
		if( imageCanvas == null ) 
			return;

		imageCanvas.updateImage(getCurViewIndex());
		imageCanvas.drawImage();
	}

	@Override
	protected void drawFeature() {
		//		super.drawFeature();
		if( this.featureControl != null )
			this.featureControl.dispose();
		featureControl = new Composite(compositeTop, SWT.NONE);
		GridData featureData = new GridData();
		featureData.grabExcessHorizontalSpace = true;
		featureData.horizontalAlignment = GridData.FILL;
		featureData.grabExcessVerticalSpace = true;
		featureData.verticalAlignment = GridData.FILL;
		featureData.minimumHeight = calcMaxHeight();
		featureData.minimumWidth = calcMaxWidth();
		featureControl.setLayoutData(featureData);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.verticalSpacing = 10;
		layout.numColumns = 2;
		featureControl.setLayout(layout);
		if( getCartoonOptions() != null ) {
			imageCanvas = new MSGlycanAnnotationFigureCanvas(featureControl, SWT.H_SCROLL | SWT.V_SCROLL, getCartoonOptions());
			sequences = new ArrayList<>();
			for( int i = 0; i < msAnnotationDetails.getNumFeatures(); i++ ) {
				sequences.add(msAnnotationDetails.getFeature(i).getSequence());
			}
			imageCanvas.setGlycans(sequences, this);
			GridData icData = new GridData();
			icData.grabExcessHorizontalSpace = true;
			icData.grabExcessVerticalSpace = true;
			icData.horizontalAlignment = GridData.FILL;
			icData.verticalAlignment = GridData.FILL;
			icData.horizontalSpan = 2;
			imageCanvas.setLayoutData(icData);
		}
	}

	public void refreshImages() {
		imageCanvas.setGlycans(sequences, this);
		imageCanvas.drawImage();
	}

	protected List<String> getSequences() {
		return sequences;
	}
	
	
}
