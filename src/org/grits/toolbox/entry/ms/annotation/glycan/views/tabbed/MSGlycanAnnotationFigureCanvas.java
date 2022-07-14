package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationEntityScroller;
import org.grits.toolbox.utils.data.CartoonOptions;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;
import org.grits.toolbox.widgets.tools.IGRITSEventListener;

/**
 * Glycan-centric FigureCanvas to be displayed on an Entity Scroller at the top of the Details or Spectra objects for the MS/MS tab.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationFigureCanvas extends FigureCanvas implements IGRITSEventListener {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationFigureCanvas.class);	
	ImageFigure iFig = null;
	protected String glycanKey = null;
	protected MSGlycanAnnotationCartoonPreferences preference = null;

	protected List<GlycanImageObject> imageList;
	protected GlycanImageObject image = null;

	public MSGlycanAnnotationFigureCanvas(Composite parent, int style,
			CartoonOptions cartoonOptions) {
		super(parent, style);
		initializeCanvas();
		MSGlycanAnnotationTableDataObject.glycanImageProvider.setCartoonOptions(cartoonOptions);
		imageList = new ArrayList<>();
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				removeListener();
			}
		});
	}
	
	/**
	 * Updates the image provider's cartoon options.
	 * 
	 * @param cartoonOptions, desired cartoon options
	 */
	public void updateCartoonOptions(CartoonOptions cartoonOptions) {
		MSGlycanAnnotationTableDataObject.glycanImageProvider.setCartoonOptions(cartoonOptions);	
	}

	/**
	 * Initializes the canvas object that displays the cartoons.
	 */
	private void initializeCanvas() {
		setViewport(new Viewport(true));
		setScrollBarVisibility(FigureCanvas.AUTOMATIC);
		setBackground(new org.eclipse.swt.graphics.Color(getShell().getDisplay(), 255, 255, 255));
	}

	/**
	 * Sets the current glycanKey to the specified sequence.
	 * 
	 * @param sequence, GWB sequence that should be displayed.
	 */
	public void updateGlycan(String sequence) {
		this.glycanKey = sequence;
	}

	/**
	 * Creates a list of GlycanImageObjects from the specified list of sequences and sets the size of the Entity Scroller based on the size of the images.
	 * 
	 * @param sequences, list of GWB sequences
	 * @param scroller, reference to the calling Enity Scroller
	 */
	public void setGlycans(List<String> sequences, MSAnnotationEntityScroller scroller) {
		try {
			scroller.setMaxHeight(0);
			scroller.setMaxWidth(0);
			if( this.imageList == null ) {
				imageList = new ArrayList<>();
			} else {
				imageList.clear();
			}
			for( String sequence : sequences ) {
				glycanKey = sequence;
				double dMaxScale = MSGlycanAnnotationTableDataObject.glycanImageProvider.getCartoonOptions().getImageScaleFactor() > 1.0 ?
						1.0 : MSGlycanAnnotationTableDataObject.glycanImageProvider.getCartoonOptions().getImageScaleFactor();
				MSGlycanAnnotationTableDataObject.glycanImageProvider.getCartoonOptions().setImageScaleFactor(dMaxScale);
				GlycanImageObject gio = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(sequence);
				this.image = gio;
				if( image.getSwtImage().getBounds().width > scroller.getMaxWidth() ) {
					scroller.setMaxWidth(image.getSwtImage().getBounds().width);
				}
				if( image.getSwtImage().getBounds().height > scroller.getMaxHeight() ) {
					scroller.setMaxHeight(image.getSwtImage().getBounds().height);
				}
				this.imageList.add(gio);
			}
			MSGlycanAnnotationTableDataObject.glycanImageProvider.addEventListener(this);
		} catch (Exception e) {
			logger.info("Exception in MSGlycanAnnotationFigureCanvas.createGlycanFromSequence.", e);
		}
	}

	/**
	 * Updates the image in the iFig ImageFigure to the SWT image in the current image object
	 */
	public void drawImage() {
		if( iFig == null ) {
			iFig = new ImageFigure(image.getSwtImage());
			setContents(iFig);
		} else {
			iFig.setImage(image.getSwtImage());
		}
		redraw();
	}

	/**
	 * Updates the image based on the current glycan key.
	 */
	public void updateImage() {
		boolean bAddListener = (image == null);
		image = null;
		try {
			this.image = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(glycanKey);
			this.image.setIsOriginalSize(false);
		} catch (Exception e) {
			logger.error( "Exception in MSGlycanAnnotationFigureCanvas.updateImage", e);
		}
		if (bAddListener && image != null) {
			addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					setContents(new ImageFigure(image.getSwtImage()));
				}
			});
		}
	}

	/**
	 * Updates the current image from the list using the specified index. 
	 * 
	 * @param _iImageNum, the index of the image to draw in the canvas
	 */
	public void updateImage( int _iImageNum ) {
		boolean bAddListener = (image == null);
		image = null;
		try {			
			image = this.imageList.get(_iImageNum);
		} catch (Exception e) {
			logger.error("Exception in MSGlycanAnnotationFigureCanvas.updateImage", e);
		}
		if (bAddListener && image != null) {
			addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					setContents(new ImageFigure(image.getSwtImage()));
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.IGRITSEventListener#handleEvent(int)
	 */
	@Override
	public void handleEvent(int arg0) {
		drawImage();
	}
	
	/**
	 * removes this listener from the glycan image cache's list of listeners
	 */
	protected void removeListener() {
		MSGlycanAnnotationTableDataObject.glycanImageProvider.removeEventListener(this);
	}
	
}
