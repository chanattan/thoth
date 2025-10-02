package thoth.simulator;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JPanel;
import thoth.logic.Curve;

@SuppressWarnings("serial")
public class Simulator extends JPanel {

	private HashMap<String, Curve> data;
	
	/*
		Fills in the data about curves for a set of given funds names. 
	*/
	public void fillData(HashMap<String, Curve> data) {
		this.data = data;
	}

	/*
		Defines the panel's content, i.e., drawing the curves and any UI-related content for the main screen.
	*/
	@Override
	public void paint(Graphics g2) {
		super.paint(g2);
		Graphics2D g = (Graphics2D) g2;
		g.drawString("TEST", 500, 500);

		// For each fund, display its associated curve.
		/*for (Entry<String, Curve> e : data.entrySet()) {
			String fundName = e.getKey();
			Curve curve = e.getValue();

			// Display the curve for all the months.
			for (int i = 1; i < curve.getSteps(); i++) {
				float currValue = curve.getValue(i);
				// Plot the corresponding point.
				
			}
		}  */
	}
	
	public static HashMap<String, Curve> generateFunds() {
        return null;
    }
}
