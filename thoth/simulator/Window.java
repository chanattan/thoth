package thoth.simulator;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class Window extends JFrame {
	private final Simulator sim;
	public final InvestorPanel investorPanel;

	public static final Color THEME_COLOR = new Color(43, 42, 42);

	public Window(Thoth thoth) {
		setSize(new Dimension(1280, 720));
		getContentPane().setBackground(THEME_COLOR);
		setBackground(THEME_COLOR);
		setAlwaysOnTop(true);
		
		// To put in InvestorPanel.
		investorPanel = new InvestorPanel(thoth);
		
		Simulator simulator = new Simulator(thoth);
		this.sim = simulator;
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, simulator, investorPanel);
		splitPane.setDividerLocation(0.7);
		splitPane.setResizeWeight(0.7);
		getContentPane().add(splitPane);
        setVisible(true);
	}

	public Simulator getSimulator() {
		return this.sim;
	}

	private static final long serialVersionUID = 1L;

}