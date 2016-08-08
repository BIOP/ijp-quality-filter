package ch.epfl.biop;


import java.util.ArrayList;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.RoiEnlarger;
import ij.process.ImageStatistics;

public class QualityFilter {

	public static ArrayList<Roi> run(Roi[] rois, ImagePlus imp, double threshold, double pixels) {
		
		ArrayList<Roi> filtered = new ArrayList<Roi>(rois.length); 
		
		double[] ratios = new double[rois.length];
		// Loop through each roi
		for(int i=0; i < rois.length; i++) {
			// Measure in the ROI
			imp.setRoi(rois[i]);
			ImageStatistics stats_in = imp.getStatistics();
			
			//stats.mean;
			
			// Measure outside the ROI
			Roi large_roi = RoiEnlarger.enlarge(rois[i], pixels);
			imp.setRoi(large_roi);
			ImageStatistics stats_out = imp.getStatistics();

			// Compute ratio
			double denom = ( ( (stats_out.area * stats_out.mean) - (stats_in.area * stats_in.mean) ) / (stats_out.area - stats_in.area));
			ratios[i] = Math.abs( stats_in.mean / denom );
			
			// check if ratio higher than threshold
			if(ratios[i] > threshold) {
				// Keep if true
				filtered.add(rois[i]);
			}
		}
		imp.deleteRoi();
		// return filtered ROI list 
		return filtered;
	}

}
