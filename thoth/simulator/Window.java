package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Window extends JFrame {
	private Simulator sim;

	public static final Color THEME_COLOR = new Color(43, 42, 42);

	public Window(Thoth thoth) {
		setSize(new Dimension(1280, 720));
		getContentPane().setBackground(THEME_COLOR);
		setBackground(THEME_COLOR);
		setAlwaysOnTop(true);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		JButton investBtn = new JButton("Invest");
		investBtn.setBackground(Color.ORANGE);
		investBtn.setForeground(Color.DARK_GRAY);
		investBtn.setOpaque(true);
        investBtn.setBorderPainted(false); 
        investBtn.setFocusPainted(false);
		investBtn.addActionListener((ActionEvent e) -> {
            System.out.println("Investing...");
                });
		JLabel capitalLabel = new JLabel("Capital: " + thoth.player.getCapital());
		
		Simulator simulator = new Simulator(thoth);
		this.sim = simulator;
		getContentPane().add(simulator);
		getContentPane().add(investBtn, BorderLayout.CENTER);
		getContentPane().add(capitalLabel, BorderLayout.LINE_END);
        setVisible(true);
	}

	public Simulator getSimulator() {
		return this.sim;
	}

	private static final long serialVersionUID = 1L;

}