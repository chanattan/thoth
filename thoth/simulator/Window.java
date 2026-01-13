package thoth.simulator;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class Window extends JFrame {
	private final Simulator sim;
	public final InvestorPanel investorPanel;
	public final NewsPanel newsPanel;

	public static final Color THEME_COLOR = new Color(43, 42, 42);

	public Window(Thoth thoth) {
		setSize(new Dimension(1280, 720));
		getContentPane().setBackground(THEME_COLOR);
		setBackground(THEME_COLOR);
		setAlwaysOnTop(true);
		
		// To put in InvestorPanel.
		investorPanel = new InvestorPanel(thoth);
		newsPanel = new NewsPanel(thoth);
		
		Simulator simulator = new Simulator(thoth);
		this.sim = simulator;
		JSplitPane investorSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, newsPanel, investorPanel);
		investorSplit.setDividerLocation(0.3);
		investorSplit.setResizeWeight(0.3);
		investorSplit.setBackground(THEME_COLOR);
		investorSplit.setDividerSize(1);
		investorSplit.setBorder(null);
		investorSplit.setForeground(THEME_COLOR);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, simulator, investorSplit);
		newsPanel.parentPane = splitPane;
		splitPane.setDividerLocation(0.8);
		splitPane.setResizeWeight(0.8);
		splitPane.setBorder(null);
		splitPane.setDividerSize(1);
		getContentPane().add(splitPane);
        setVisible(true);
	}

	public Simulator getSimulator() {
		return this.sim;
	}

	private static final long serialVersionUID = 1L;

}