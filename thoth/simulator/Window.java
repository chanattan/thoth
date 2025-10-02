package thoth.simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Window extends JFrame {
	private Simulator sim;

	public Window(Thoth thoth) {
		setSize(new Dimension(1280, 720));
		getContentPane().setBackground(new Color(54, 54, 54));
		setBackground(new Color(54, 54, 54));
		setAlwaysOnTop(true);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		JButton btnNewButton = new JButton("Invest");
		btnNewButton.addActionListener((ActionEvent e) -> {
            System.out.println("Investing...");
                });
		
		Simulator simulator = new Simulator(thoth);
		this.sim = simulator;
		getContentPane().add(simulator);
		getContentPane().add(btnNewButton);
        setVisible(true);
	}

	public Simulator getSimulator() {
		return this.sim;
	}

	private static final long serialVersionUID = 1L;

}