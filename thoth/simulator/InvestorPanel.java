package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import thoth.logic.Action;
import thoth.logic.Fund;
import thoth.logic.Player;

public class InvestorPanel extends JPanel {
	/***
     * Cette classe représente le panneau de l'investisseur, affichant :
     * - Le capital actuel de l'investisseur.
     * - Le temps écoulé dans la simulation.
     * - Un champ de texte pour saisir le montant à investir.
     * - Un bouton pour effectuer l'investissement.
     * - Un inventaire (sous-panel) listant les actions détenues par l'investisseur.
     * Dans ce sous-panel, s'affiche :
     *  - Le nom de chaque action détenue.
     *  - La valeur actuelle de chaque action.
     *  - La date d'achat de chaque action.
     *  - Un bouton pour vendre chaque action.
     *  - La plus-value de chaque action.
     *  - Des prédictions pour les actions détenues.
     * Le panel global se met à jour automatiquement et en temps réel, il est situé sur la droite de la fenêtre principale.
     */

    private Thoth thoth;

    private double capital;
    private long elapsedTime;
    private JTextField investmentField;
    private JButton investButton;
    private JPanel inventoryPanel;
    private JLabel capitalLabel;
    private JLabel timeLabel;

    public InvestorPanel(Thoth thoth) {
        this.thoth = thoth;
        capital = 0;
        elapsedTime = 0;
        investmentField = new JTextField(10);
        investmentField.addActionListener((ActionEvent e) -> {
			// The value should be bound to the max capital of the user, upon typing.
			double val = Double.parseDouble(investmentField.getText());
			if (val > thoth.player.getCapital()) {
				investmentField.setText("" + thoth.player.getCapital());
			}
        });
        investButton = new JButton("Investir");
        investButton.addActionListener((ActionEvent e) -> {
            Fund selectedFund = thoth.window.getSimulator().selectedFund;
				if (selectedFund != null) {
					Action a = new Action(thoth.window.getSimulator().getTime(), Float.parseFloat(investmentField.getText()), selectedFund);
					try {
						thoth.player.invest(a);
						capitalLabel.setText("Capital: " + thoth.player.getCapital());
                        updateInventoryPanel();
					} catch (Player.InsufficientCapital ex) {
						System.out.println(ex.getMessage());
					}
				}
        });

		timeLabel = new JLabel("Month: 0");
        
        setupPanel();
    }

    public void updatePanel(Thoth thoth) {
        // Update capital, elapsed time, and inventory display
        capital = thoth.player.getCapital();
        elapsedTime = thoth.window.getSimulator().getTime();
        capitalLabel.setText("Capital: " + capital);
        timeLabel.setText("Month: " + elapsedTime);
    }

    private void setupPanel() {
        setLayout(new BorderLayout());
        add(createCapitalPanel(), BorderLayout.NORTH);
        add(createInvestmentPanel(), BorderLayout.CENTER);
        inventoryPanel = createInventoryPanel();
        add(inventoryPanel, BorderLayout.SOUTH);
        add(createTimePanel(), BorderLayout.EAST);
    }

    private JPanel createCapitalPanel() {
        JPanel panel = new JPanel();
        capitalLabel = new JLabel("Capital: " + capital);
        panel.add(capitalLabel);
        return panel;
    }

    private JPanel createInvestmentPanel() {
        JPanel panel = new JPanel();
        panel.add(investmentField);
        panel.add(investButton);
        return panel;
    }

    private JPanel createTimePanel() {
        JPanel panel = new JPanel();
        panel.add(timeLabel);
        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    private void updateInventoryPanel() {
        // Each action is a row with its details and a sell button.
        
        Player p = this.thoth.player;
        for (Fund f : p.getActions().keySet()) {
            for (Action a : p.getActions().get(f)) {
                JPanel actionPanel = new JPanel();
                JLabel nameLabel = new JLabel("Fund: " + f.getName());
                JLabel valueLabel = new JLabel("Value: " + a.getValue());
                JLabel dateLabel = new JLabel("Date: " + a.getTime());
                JButton sellButton = new JButton("Sell");
                sellButton.addActionListener((ActionEvent e) -> {
                    p.sellAction(a);
                    updatePanel(thoth);
                });
                actionPanel.add(nameLabel);
                actionPanel.add(valueLabel);
                actionPanel.add(dateLabel);
                actionPanel.add(sellButton);
                inventoryPanel.add(actionPanel);
            }
        }
        inventoryPanel.repaint();
        repaint();
    }
}