
import java.awt.AWTEvent;
import java.awt.Color;
import java.util.ArrayList;

import ch.epfl.biop.QualityFilter;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

/**
 * Plugin that implements an Imaris-like quality criterion to sort out ROIs
 * @author Olivier Burri
 * @version 1.0
 */
 public class Quality_Filter implements PlugIn {

	
	private RoiManager rm;
	double threshold;
	double enlarge_by;
	Roi[] rois;
	ImagePlus imp;
	
	@Override
	public void run(String arg) {		
	
		imp = IJ.getImage();
		rm = RoiManager.getInstance();
		
		if(rm == null || rm.getCount() == 0) {
			IJ.showMessage("Need ROIs in RoiManager...");
			return;
		}
		
		this.threshold  = Prefs.get("quality.filter.thr", 4.0);
		this.enlarge_by = Prefs.get("quality.filter.pix", 10.0);
		
		this.rois = rm.getRoisAsArray();
		GenericDialog gd;
		if (arg.matches(".*interactive.*")) {
			gd = showInteractiveDialog();
		} else {
			gd = showDialog();
		}
		
		if(gd.wasCanceled()) {
			return;
		}
		
		ArrayList<Roi> filtered = QualityFilter.run(rois, imp, this.threshold, this.enlarge_by);
		
		if(imp.getOverlay() != null )
			imp.getOverlay().clear();

		// Show new ROIs in ROI manager with different color
		for(Roi r : filtered) {
			r.setName("ROI "+r.getName()+" Filtered");
			//r.setFillColor(null);
			rm.addRoi(r);
		}
	}
	
	private GenericDialog showDialog() {
		
		GenericDialog gd = new GenericDialog("Quality Filtering");
		
		gd.addNumericField("Threshold", this.threshold, 3);
		gd.addNumericField("Area Around", this.enlarge_by, 3);
		
		gd.showDialog();
		
		if(gd.wasCanceled()) {
			return gd;			
		}
		
		this.threshold = gd.getNextNumber();
		this.enlarge_by = gd.getNextNumber();
		
		Prefs.set("quality.filter.thr", this.threshold);
		Prefs.set("quality.filter.pix", this.enlarge_by);
		
		return gd;
	}

	private GenericDialog showInteractiveDialog() {
		GenericDialog gd = new GenericDialog("Quality Filtering");
		
		gd.addNumericField("Threshold", this.threshold, 3);
		gd.addNumericField("Area Around", this.enlarge_by, 3);
		gd.addDialogListener(new DialogListener() {
			
			@Override
			public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
				if(gd.wasOKed() || gd.wasCanceled()) {
					return true;
				}
				double thr = gd.getNextNumber();
				double px = gd.getNextNumber();
				Overlay ov = imp.getOverlay();
				if (ov == null) {
					ov = new Overlay();
				} else {
					ov.clear();
				}
					ArrayList<Roi> filtered = QualityFilter.run(rois, imp, thr, px);
				for(Roi r : filtered) {
					r.setFillColor(new Color(255, 0, 255, 128));
					ov.addElement(r);
				}
				imp.setOverlay(ov);
				return true;
			}
		});
		
		gd.showDialog();
		
		if(gd.wasCanceled()) {
			return gd;			
		}
		
		this.threshold = gd.getNextNumber();
		this.enlarge_by = gd.getNextNumber();
		
		Prefs.set("quality.filter.thr", this.threshold);
		Prefs.set("quality.filter.pix", this.enlarge_by);
		
		return gd;			

	}

	/**
	 * Main method for debugging.
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Quality_Filter.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();
		ImagePlus imp = IJ.openImage("E:\\AuPbSn40.tif");
		IJ.setAutoThreshold(imp, "Otsu");
		IJ.run(imp, "Analyze Particles...", "display exclude clear add");
		imp.show();
		IJ.resetThreshold(imp);
		IJ.run("Quality Filter");
		
	}




}
