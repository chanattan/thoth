package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import thoth.logic.Action;
import thoth.logic.Fund;
import thoth.logic.Player;

public class Window extends JFrame {
	private final Simulator sim;
	private JTextField investAmountField;
	private final JLabel timeLabel;
	private final JLabel capitalLabel;

	public static final Color THEME_COLOR = new Color(43, 42, 42);

	public Window(Thoth thoth) {
		setSize(new Dimension(1280, 720));
		getContentPane().setBackground(THEME_COLOR);
		setBackground(THEME_COLOR);
		setAlwaysOnTop(true);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		// To put in InvestorPanel.

		capitalLabel = new JLabel("Capital: " + thoth.player.getCapital());

		JButton investBtn = new JButton("Invest");
		investBtn.setBackground(Color.ORANGE);
		investBtn.setForeground(Color.DARK_GRAY);
		investBtn.setOpaque(true);
        investBtn.setBorderPainted(false); 
        investBtn.setFocusPainted(false);
		investBtn.addActionListener((ActionEvent e) -> {
				Fund selectedFund = getSimulator().selectedFund;
				if (selectedFund != null) {
					Action a = new Action(getSimulator().getTime(), Float.parseFloat(investAmountField.getText()), selectedFund);
					try {
						thoth.player.invest(a);
						capitalLabel.setText("Capital: " + thoth.player.getCapital());
					} catch (Player.InsufficientCapital ex) {
						System.out.println(ex.getMessage());
					}
				}
			});

		investAmountField = new JTextField("Amount to invest");
		investAmountField.setMaximumSize(new Dimension(200, 30));
		investAmountField.setBackground(Color.LIGHT_GRAY);
		investAmountField.setForeground(Color.DARK_GRAY);
		investAmountField.setOpaque(true);
		investAmountField.setBorder(null);
		investAmountField.addActionListener((ActionEvent e) -> {
			// The value should be bound to the max capital of the user, upon typing.
			double val = Double.parseDouble(investAmountField.getText());
			if (val > thoth.player.getCapital()) {
				investAmountField.setText("" + thoth.player.getCapital());
			}
		});

		timeLabel = new JLabel("Month: 0");
		timeLabel.setForeground(Color.WHITE);
		timeLabel.setBackground(THEME_COLOR);
		timeLabel.setOpaque(true);
		timeLabel.setBorder(null);
		
		Simulator simulator = new Simulator(thoth);
		this.sim = simulator;
		getContentPane().add(simulator);
		getContentPane().add(investBtn, BorderLayout.CENTER);
		getContentPane().add(capitalLabel, BorderLayout.LINE_END);
		getContentPane().add(investAmountField, BorderLayout.LINE_START);
		getContentPane().add(timeLabel, BorderLayout.PAGE_START);
        setVisible(true);
	}

	public void updateTimeLabel(int timeStep) {
		this.timeLabel.setText("Month: " + timeStep);
	}

	public Simulator getSimulator() {
		return this.sim;
	}

	private static final long serialVersionUID = 1L;

}