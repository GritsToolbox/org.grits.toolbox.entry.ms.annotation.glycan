
package org.grits.toolbox.entry.ms.annotation.glycan.spectrum.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYDataImageAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

import org.grits.toolbox.entry.ms.annotation.spectrum.chart.MSAnnotationSpectralViewerChart;

/**
 *
 * @author brentw
 */
public class MSGlycanAnnotationSpectralViewerChart extends MSAnnotationSpectralViewerChart {
	protected String sID;
	protected Double dObsMass = null;

	public MSGlycanAnnotationSpectralViewerChart( String _sDescription, int _iScanNum, 
			int _iMSLevel, boolean _bIsProfile, boolean _bVertLabels, String sID, Double _dObsMass ){
		super( _sDescription, _iScanNum, _iMSLevel, _bIsProfile, _bVertLabels, sID, _dObsMass );
	}


	@Override
	protected LegendItemCollection createLegendItems() {
		LegendItemCollection legenditemcollection = new LegendItemCollection();
		if ( sID == null ) 
			return legenditemcollection;
		LegendItem legenditem = new LegendItem("Glycan: " + sID, "-", null, null, Plot.DEFAULT_LEGEND_ITEM_BOX, Color.white);

		legenditemcollection.add(legenditem);
		return legenditemcollection;
	}   

	public class MYXYDataImageAnnotation extends XYDataImageAnnotation {

		public MYXYDataImageAnnotation(Image image, double x, double y,
				double w, double h) {
			super(image, x, y, w, h);
			// TODO Auto-generated constructor stub
		}

		public MYXYDataImageAnnotation(Image image, double x, double y, double w, double h, boolean includeInBounds ) {
			super(image, x, y, w, h, includeInBounds);

		}
		@Override
		public void addChangeListener(AnnotationChangeListener listener) {
			// TODO Auto-generated method stub
//			super.addChangeListener(listener);
		}

		@Override
		protected void fireAnnotationChanged() {
			// TODO Auto-generated method stub
			//			super.fireAnnotationChanged();
		}

		@Override
		public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
				ValueAxis domainAxis, ValueAxis rangeAxis,
				int rendererIndex,
				PlotRenderingInfo info) {

			PlotOrientation orientation = plot.getOrientation();
			AxisLocation xAxisLocation = plot.getDomainAxisLocation();
			AxisLocation yAxisLocation = plot.getRangeAxisLocation();
			RectangleEdge xEdge = Plot.resolveDomainAxisLocation(xAxisLocation,
					orientation);
			RectangleEdge yEdge = Plot.resolveRangeAxisLocation(yAxisLocation,
					orientation);
			float j2DX0 = (float) domainAxis.valueToJava2D(getX(), dataArea, xEdge);
			float j2DY0 = (float) rangeAxis.valueToJava2D(getY(), dataArea, yEdge);
			float j2DX1 = (float) domainAxis.valueToJava2D(getX()+ getWidth(), dataArea, xEdge);
			float j2DY1 = (float) rangeAxis.valueToJava2D(getY() + getHeight(),	dataArea, yEdge);
			float xx0 = 0.0f;
			float yy0 = 0.0f;
			float xx1 = 0.0f;
			float yy1 = 0.0f;
			if (orientation == PlotOrientation.HORIZONTAL) {
				xx0 = j2DY0;
				xx1 = j2DY1;
				yy0 = j2DX0;
				yy1 = j2DX1;
			}
			else if (orientation == PlotOrientation.VERTICAL) {
				xx0 = j2DX0;
				xx1 = j2DX1;
				yy0 = j2DY0;
				yy1 = j2DY1;
			}
			int x1 = (int) xx0;
			//			int y1 = (int) Math.min(yy0, yy1);
			int y1 = (int) yy0;
			int w = (int) (xx1 - xx0);
			int h =  (int) Math.abs(yy1 - yy0);

			int imgW = ((BufferedImage) getImage()).getWidth();
			int imgH = ((BufferedImage) getImage()).getHeight();
			// TODO: rotate the image when drawn with horizontal orientation?
			g2.drawImage(getImage(), x1, y1, imgW, imgH, null);
			String toolTip = getToolTipText();
			String url = getURL();
			if (toolTip != null || url != null) {
				addEntity(info, new Rectangle2D.Float(xx0, yy0, (xx1 - xx0),
						(yy1 - yy0)), rendererIndex, toolTip, url);
			}
		}
	}
}
