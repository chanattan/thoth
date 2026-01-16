package thoth.simulator;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class Window extends JFrame {
	public final Simulator sim;
	public final InvestorPanel investorPanel;
	public final NewsPanel newsPanel;
	public final FundsPanel fundsPanel;

	public static final Color THEME_COLOR = new Color(43, 42, 42);

	public Window(Thoth thoth) {
		// os max screen size
		setTitle("Thoth Simulator");
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				// Save data before closing
				thoth.logger.saveData();
				System.exit(0);
			}
		});
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setSize(screenSize);
		getContentPane().setBackground(THEME_COLOR);
		setBackground(THEME_COLOR);
		setAlwaysOnTop(true);
		
		// To put in InvestorPanel.
		investorPanel = new InvestorPanel(thoth);
		newsPanel = new NewsPanel(thoth);
		fundsPanel = new FundsPanel(thoth);
		
		Simulator simulator = new Simulator(thoth);
		this.sim = simulator;
		JSplitPane investorAndFundsSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fundsPanel, investorPanel);
		investorAndFundsSplit.setDividerLocation(0.5);
		investorAndFundsSplit.setResizeWeight(0.5);
		investorAndFundsSplit.setBackground(THEME_COLOR);
		investorAndFundsSplit.setDividerSize(2);
		investorAndFundsSplit.setBorder(null);
		investorAndFundsSplit.setForeground(THEME_COLOR);
		JSplitPane investorSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, newsPanel, investorAndFundsSplit);
		investorSplit.setDividerLocation(0.3);
		investorSplit.setResizeWeight(0.3);
		investorSplit.setBackground(THEME_COLOR);
		investorSplit.setDividerSize(2);
		investorSplit.setBorder(null);
		investorSplit.setForeground(THEME_COLOR);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, simulator, investorSplit);
		newsPanel.parentPane = splitPane;
		splitPane.setDividerLocation(0.8);
		splitPane.setResizeWeight(0.8);
		splitPane.setBorder(null);
		splitPane.setDividerSize(2);
		splitPane.setBackground(THEME_COLOR);
		splitPane.setForeground(THEME_COLOR);
		getContentPane().add(splitPane);
        setVisible(true);
	}

	public Simulator getSimulator() {
		return this.sim;
	}

	private static final long serialVersionUID = 1L;

}