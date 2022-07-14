package org.grits.toolbox.entry.ms.annotation.glycan.preference.cartoon;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferencesLoader;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationFigureCanvas;
import org.grits.toolbox.utils.data.CartoonOptions;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;
import org.grits.toolbox.utils.image.ImageCreationException;
import org.grits.toolbox.widgets.processDialog.ProgressDialog;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.progress.ProgressThread;
import org.grits.toolbox.widgets.tools.IGRITSEventHandler;
import org.grits.toolbox.widgets.tools.IGRITSEventListener;

public class MSGlycanAnnotationCartoonPreferencePage extends PreferencePage 
implements IPropertyChangeListener {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationCartoonPreferencePage.class);
	public static final String PREFERENCE_PAGE_ID = "org.grits.toolbox.core.datamodel.ms.annotation.glycan.cartoon";

	private MSGlycanAnnotationCartoonPreferences cartoonPreferences = null;

	// Layout
	private Label imageLayout = null;
	private Combo imageLayoutCombo = null;

	// Style
	private Label imageStyle = null;
	private Combo imageStyleCombo = null;

	// Orientation
	private Label lblImageOrientation = null;
	private Combo cImageOrientation = null;

	// Show Info
	private Label lblShowInfo = null;
	private Button btnShowInfoYes = null;
	private Button btnShowInfoNo = null;

	// Show Masses
	private Label lblShowMasses = null;
	private Button btnShowMassesYes = null;
	private Button btnShowMassesNo = null;

	// Show Reducing End
	private Label lblShowRedEnd = null;
	private Button btnShowRedEndYes = null;
	private Button btnShowRedEndNo = null;

//	private Button refreshReports = null;

	//Canvas canvas
	private MSGlycanAnnotationFigureCanvas canvas = null;

	private Text scaleValue = null;
	private Label scaleLabel = null;
	private Scale scale = null;

	private Composite container;

	//	private static String SEQUENCE = "Gal(b1-4)GlcNAc(b1-2)Man(a1-3)[Gal(b1-4)GlcNAc(b1-2)Man(a1-6)][GlcNAc(b1-4)]Man(b1-4)GlcNAc(b1-4)[Fuc(a1-6)]GlcNAc";
	private static String SEQUENCE = "freeEnd--??1D-GlcNAc,p(--4b1D-GlcNAc,p--4b1D-Man,p((--3a1D-Man,p--2b1D-GlcNAc,p--4b1D-Gal,p)--6a1D-Man,p--2b1D-GlcNAc,p--4b1D-Gal,p)--4b1D-GlcNAc,p)--6a1D-Fuc,p$MONO,perMe,Na,0,freeEnd";
	private static final double SCALE_FACTOR = 32.0;
	private final DecimalFormat dcF = new DecimalFormat("0.00");
	private List<IGRITSEventListener> eventListeners = new ArrayList();
	public final static int GLYCAN_BLOCK_RELEASED = 12345;

	public MSGlycanAnnotationCartoonPreferencePage()
	{
		super();
		// this used to be in the init method before the E3->E4 migration...may need to go to createContents
		loadWorkspacePreferences();
	}

	@Override
	protected Control createContents(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.numColumns = 6;
		container.setLayout(layout);

		createImageLayoutCombo();
		createImageStyleCombo();
		addImageOrientationParts();
		//		addShowInfoParts();
		addShowMassesParts();
		addShowRedEndParts();
	//	refreshReports = new Button(parent, SWT.CHECK);
	//	refreshReports.setText("Propagate changes to open reports.");
	//	refreshReports.setSelection(true);
		//TODO find a way to enable/disable this checkbox
	/*	if (!hasOpenResultsViewer()) {
			refreshReports.setEnabled(false);
		}	*/	

		createScalePart();		
		createShowImagePart();

		setPageComplete(true);
		return container;
	}

	private void createImageStyleCombo() {
		//create label
		imageStyle = new Label(container, SWT.NONE);
		imageStyle.setText("Image Style");

		//create combo
		GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
		imageStyleCombo = new Combo(container,SWT.VERTICAL | SWT.BORDER | SWT.READ_ONLY);
		comboData.horizontalSpan = 5;
		imageStyleCombo.setLayoutData(comboData);

		//add elements
		//		String[] elements = CartoonTypes.Type.ImageStyle.getElements().split(PropertyHandler.getPreferenceDelimiter());
		Object[] elements = cartoonPreferences.getAllStyles().toArray();
		for(int i = 0; i < elements.length; i++ ) {
			imageStyleCombo.add( (String) elements[i] );
			if(this.cartoonPreferences.getImageStyle().equals((String) elements[i]))
			{
				imageStyleCombo.select(i);
			}
		}

		//add listener
		imageStyleCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cartoonPreferences.setImageStyle(imageStyleCombo.getText());
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createImageLayoutCombo() {
		//create label
		imageLayout = new Label(container, SWT.NONE);
		imageLayout.setText("Image Layout");

		//create combo
		GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
		imageLayoutCombo = new Combo(container,SWT.VERTICAL | SWT.BORDER | SWT.READ_ONLY);
		comboData.horizontalSpan = 5;
		imageLayoutCombo.setLayoutData(comboData);

		//add elements
		//		String[] elements = CartoonTypes.Type.ImageLayout.getElements().split(PropertyHandler.getPreferenceDelimiter());
		Object[] elements = cartoonPreferences.getAllLayouts().toArray();
		for(int i = 0; i < elements.length; i++ ) {
			imageLayoutCombo.add( (String) elements[i] );
			if(this.cartoonPreferences.getImageLayout().equals((String) elements[i]))
			{
				imageLayoutCombo.select(i);
			}
		}
		//add listener
		imageLayoutCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cartoonPreferences.setImageLayout(imageLayoutCombo.getText());
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}


	private void addImageOrientationParts() {
		// Orientation
		lblImageOrientation = new Label(container, SWT.NONE);
		lblImageOrientation.setText("Image Orientation");
		//create combo
		GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
		cImageOrientation = new Combo(container,SWT.VERTICAL | SWT.BORDER | SWT.READ_ONLY);
		comboData.horizontalSpan = 5;
		cImageOrientation.setLayoutData(comboData);

		//add elements
		//		String[] elements = CartoonTypes.Type.ImageOrientation.getElements().split(PropertyHandler.getPreferenceDelimiter());
		Object[] elements = cartoonPreferences.getAllOrientations().toArray();
		for(int i = 0; i < elements.length; i++ ) {
			cImageOrientation.add( (String) elements[i] );
			if(this.cartoonPreferences.getOrientation().equals((String) elements[i]))
			{
				cImageOrientation.select(i);
			}
		}

		if( cImageOrientation.getSelectionIndex() < 0 ) {
			cImageOrientation.select(0);
		}

		//add listener
		cImageOrientation.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cartoonPreferences.setOrientation(cImageOrientation.getText());
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	/*
	 * DBW: ok, this is confusing. I didn't see any difference in glycan images using GWB's show info interface. I don't like the term "Show Masses" but like
	 * "Show Info". So I'm not using the showInfo components but using the showMasses, while displaying "Show Info" on the label
	 *
	private void addShowInfoParts() {
		// ShowInfo
		lblShowInfo = new Label(container, SWT.NONE);
		lblShowInfo.setText("Show Info");

		Composite cmp = new Composite(container, SWT.NONE);
		GridData cmpGD = new GridData(GridData.FILL_HORIZONTAL);
		cmpGD.horizontalSpan = 2;
		cmp.setLayoutData(cmpGD);
		cmp.setLayout(new RowLayout());

		btnShowInfoYes = new Button(cmp, SWT.RADIO);
		btnShowInfoYes.setText("Yes");
		if( this.preference.isShowInfo() ) {
			btnShowInfoYes.setSelection(true);
		}
		btnShowInfoNo = new Button(cmp, SWT.RADIO);
		btnShowInfoNo.setText("No");
		if( ! this.preference.isShowInfo() ) {
			btnShowInfoNo.setSelection(true);
		}

		//add listener
		btnShowInfoYes.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				preference.setShowInfo(btnShowInfoYes.getSelection());
				btnShowInfoNo.setSelection(! btnShowInfoYes.getSelection() );
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnShowInfoNo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				preference.setShowInfo(btnShowInfoYes.getSelection());
				btnShowInfoYes.setSelection( btnShowInfoYes.getSelection() );
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		GridData filler = new GridData(GridData.FILL_HORIZONTAL);
		filler.horizontalSpan = 3;
		Label lblFill = new Label(container, SWT.NONE);
		lblFill.setLayoutData(filler);
	}
	 */
	/*
	 *	DBW: ok, this is confusing. I didn't see any difference in glycan images using GWB's show info interface. I don't like the term "Show Masses" but like
	 * "Show Info". So I'm not using the showInfo components but using the showMasses, while displaying "Show Info" on the label.
	 * 
	 *  Thus the addShowInfoParts() method actually initializes the showMasses parts!
	 */
	private void addShowMassesParts() {
		// ShowInfo
		lblShowMasses = new Label(container, SWT.NONE);
		lblShowMasses.setText("Show Masses");

		Composite cmp = new Composite(container, SWT.NONE);
		GridData cmpGD = new GridData(GridData.FILL_HORIZONTAL);
		cmpGD.horizontalSpan = 2;
		cmp.setLayoutData(cmpGD);
		cmp.setLayout(new RowLayout());

		btnShowMassesYes = new Button(cmp, SWT.RADIO);
		btnShowMassesYes.setText("Yes");
		if( this.cartoonPreferences.isShowMasses() ) {
			btnShowMassesYes.setSelection(true);
		}

		btnShowMassesNo = new Button(cmp, SWT.RADIO);
		btnShowMassesNo.setText("No");
		if( ! this.cartoonPreferences.isShowMasses() ) {
			btnShowMassesNo.setSelection(true);
		}


		//add listener
		btnShowMassesYes.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cartoonPreferences.setShowMasses(btnShowMassesYes.getSelection());
				btnShowMassesNo.setSelection(! btnShowMassesYes.getSelection() );
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnShowMassesNo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cartoonPreferences.setShowMasses(btnShowMassesYes.getSelection());
				btnShowMassesYes.setSelection( btnShowMassesYes.getSelection() );
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		GridData filler = new GridData(GridData.FILL_HORIZONTAL);
		filler.horizontalSpan = 3;
		Label lblFill = new Label(container, SWT.NONE);
		lblFill.setLayoutData(filler);
	}

	private void addShowRedEndParts() {
		// ShowMasses
		lblShowRedEnd = new Label(container, SWT.NONE);
		lblShowRedEnd.setText("Show Reducing End");
		Composite cmp = new Composite(container, SWT.NONE);
		GridData cmpGD = new GridData(GridData.FILL_HORIZONTAL);
		cmpGD.horizontalSpan = 2;
		cmp.setLayoutData(cmpGD);
		cmp.setLayout(new RowLayout());

		btnShowRedEndYes = new Button(cmp, SWT.RADIO);
		btnShowRedEndYes.setText("Yes");
		if( this.cartoonPreferences.isShowRedEnd() ) {
			btnShowRedEndYes.setSelection(true);
		}

		GridData rbNo = new GridData(GridData.FILL_HORIZONTAL);
		btnShowRedEndNo = new Button(cmp, SWT.RADIO);
		btnShowRedEndNo.setText("No");
		if( ! this.cartoonPreferences.isShowRedEnd() ) {
			btnShowRedEndNo.setSelection(true);
		}


		//add listener
		btnShowRedEndYes.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cartoonPreferences.setShowRedEnd(btnShowRedEndYes.getSelection());
				btnShowRedEndNo.setSelection(! btnShowRedEndYes.getSelection() );
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnShowRedEndNo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cartoonPreferences.setShowRedEnd(btnShowRedEndYes.getSelection());
				btnShowRedEndYes.setSelection( btnShowRedEndYes.getSelection() );
				drawImage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		GridData filler = new GridData(GridData.FILL_HORIZONTAL);
		filler.horizontalSpan = 3;
		Label lblFill = new Label(container, SWT.NONE);
		lblFill.setLayoutData(filler);
	}

	private void createScalePart() {
		scaleLabel = new Label(container, SWT.NONE);
		scaleLabel.setText("Image Scaling Factor");
		scale = new Scale(container, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 4;
		scale.setMinimum(1);
		int iMax = (int) (SCALE_FACTOR * 4.0);
		scale.setMaximum(iMax);
		scale.setIncrement(1);

		int iPrefVal = (int) (Double.parseDouble( cartoonPreferences.getImageScaleFactor() ) * SCALE_FACTOR );
		scale.setSelection(iPrefVal);		
		scale.setLayoutData(data);

		scaleValue = new Text(container, SWT.BORDER);
		scaleValue.setEditable(true);
		double dVal = (double) iPrefVal / SCALE_FACTOR;
		scaleValue.setText( dcF.format(dVal) );
		//		scaleValue.setBackground( scale.getBackground() );
		//		Font font = scaleValue.getFont();
		//		Font newFont = new Font(font.getDevice(), font.toString(), 9, SWT.BOLD);
		//		scaleValue.setFont(newFont);
		scale.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				double dVal = (double) scale.getSelection() / SCALE_FACTOR;
				cartoonPreferences.setImageScaleFactor(String.valueOf(dVal));
				scaleValue.setText(dcF.format(dVal));
				drawImage();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		scaleValue.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				double dVal = (double) scale.getSelection() / SCALE_FACTOR;
				try {
					dVal = Double.parseDouble( scaleValue.getText() );
					cartoonPreferences.setImageScaleFactor(String.valueOf(dVal));
					int iScaleVal = (int) Math.round(dVal * SCALE_FACTOR);
					scale.setSelection(iScaleVal);
					drawImage();						
				} catch( NumberFormatException ex ) {
					;
				}
				isValid();
			}
		});

	}

	private void drawImage() {
		CartoonOptions options = getCartoonOptions();
		if( options != null ) {
			canvas.updateCartoonOptions(options);
			canvas.updateImage();
			canvas.drawImage();
		}		
	}

	private void createShowImagePart() {
		try {
			GridData gridData1 = new GridData(GridData.FILL_BOTH);
			Group imageGroup = new Group(container, SWT.NONE);
			imageGroup.setLayout(new FillLayout());
			imageGroup.setText("Image");
			imageGroup.setLayoutData(gridData1);
			gridData1.horizontalSpan = 6;
			//		updateImage();
			CartoonOptions options = getCartoonOptions();
			if( options != null ) {
				canvas = new MSGlycanAnnotationFigureCanvas(imageGroup, SWT.H_SCROLL | SWT.V_SCROLL, options);
				canvas.updateGlycan(SEQUENCE);
				MSGlycanAnnotationTableDataObject.glycanImageProvider.addImageToProvider(SEQUENCE, SEQUENCE);
			}
			drawImage();
		} catch (ImageCreationException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);			
		}
	}

	public synchronized CartoonOptions getCartoonOptions() {
		try {
			CartoonOptions options = new CartoonOptions(
					MSGlycanAnnotationCartoonPreferences.getGWBlayoutString(this.imageLayoutCombo.getText()),
					MSGlycanAnnotationCartoonPreferences.getGWBStyleString(this.imageStyleCombo.getText()), 
					scale.getSelection() / SCALE_FACTOR,
					MSGlycanAnnotationCartoonPreferences.getGWBOrientationCode(this.cImageOrientation.getText()),
					true,
					this.btnShowMassesYes.getSelection(),
					this.btnShowRedEndYes.getSelection());
			return options;
		} catch (Exception e) {
			logger.error("Exception in MSGlycanAnnotationFigureCanvas.updateCartoonOptions", e);
		}
		return null;
	}

	@Override
	public boolean isValid() {
		try {
			double dVal = Double.parseDouble(scaleValue.getText());
			if( dVal > 0 && dVal <= 4.0 )  {
				setPageComplete(true);
				return true;
			}
		} catch( NumberFormatException ex ) {
			;
		}
		setErrorMessage("Please enter a Scale Factor value greater than 0 and less than or equal to 4.");
		setPageComplete(false);
		return false;
	}

	protected void setPageComplete(boolean b) {
		//To do 
		if(b)
		{
			setErrorMessage(null);
		}
		setValid(b);
	}


	@Override
	//when apply button is clicked
	protected void performApply() {
		cartoonPreferences.saveValues();
	}

	/*private boolean hasOpenResultsViewer() {
		MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getTopViewer();
		return (viewer != null);
	}*/

	@Override
	public boolean performOk() {
		//if ( refreshReports != null && refreshReports.getSelection() ) { // propagate changes
			try {
				ImageConverterThread converter = new ImageConverterThread(getCartoonOptions());
				ProgressDialog t_dialog = new ProgressDialog(getShell());
				t_dialog.setWorker(converter);
				t_dialog.open();
				MSGlycanAnnotationTableDataObject.glycanImageProvider.notifyListeners(GLYCAN_BLOCK_RELEASED);
			} catch( Exception ex ) {
				logger.info("Exception in MSGlycanAnnotationFigureCanvas.performOk", ex);
			}
	
		//need to save, save will trigger update on the open tables
		cartoonPreferences.saveValues();
		return true;
	}

	private class ImageConverterThread extends ProgressThread {		
		CartoonOptions cartoonOptions = null;
		public ImageConverterThread(CartoonOptions cartoonOptions) {
			this.cartoonOptions = cartoonOptions;
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
		}

		@Override
		public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
			try {
				MSGlycanAnnotationTableDataObject.glycanImageProvider.blockAccess();
				MSGlycanAnnotationTableDataObject.glycanImageProvider.setCartoonOptions(cartoonOptions);
				for( GlycanImageObject gio : MSGlycanAnnotationTableDataObject.glycanImageProvider.getImageStack() ) {
					if (!gio.getImageId().equals(SEQUENCE)) {
						gio.dispose();
					}
				}

				MSGlycanAnnotationTableDataObject.glycanImageProvider.releaseBlock();
			} catch( Exception ex ) {
				logger.error("Error updating images from new preferences.", ex);
				MSGlycanAnnotationTableDataObject.glycanImageProvider.releaseBlock();
				return false;
			}
			return true;
		}

		@Override
		public void cancelWork() {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		//we do not do anything here
	}

	@Override
	//when default button is clicked
	protected void performDefaults() {
		//set all values to empty or I am not sure what to do..
	}

	private boolean loadWorkspacePreferences() {
		try {
			cartoonPreferences =  MSGlycanAnnotationCartoonPreferencesLoader.getCartoonPreferences();
		} catch (Exception ex) {
			logger.error("Error getting the Preference variable for Position", ex);
		}
		return (cartoonPreferences != null);
	}	
}
