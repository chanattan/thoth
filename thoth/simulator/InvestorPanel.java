package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

    private final Thoth thoth;

    private double capital;
    private long elapsedTime;
    private JTextField investmentField;
    private final JButton investButton;
    private JPanel inventoryPanel;
    private ArrayList<Object[]> shownActions;
    private JLabel capitalLabel;
    private final JLabel timeLabel;
    private JPanel actionsListPanel;

    private static final DecimalFormat df = new DecimalFormat("0.00");

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
					try {
						thoth.player.invest(thoth.window.getSimulator().getTime(), Double.parseDouble(investmentField.getText()), selectedFund);
						capitalLabel.setText("Capital: " + thoth.player.getCapital());
                        updateInventoryPanel();
					} catch (Player.InsufficientCapital ex) {
						System.out.println(ex.getMessage());
					}
				}
        });

		timeLabel = new JLabel("Month: 0");
        shownActions = new ArrayList<Object[]>();
        actionsListPanel = new JPanel();
        actionsListPanel.setLayout(
            new javax.swing.BoxLayout(actionsListPanel, javax.swing.BoxLayout.Y_AXIS)
        );
        
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
        inventoryPanel = createInventoryPanel();
        add(inventoryPanel, BorderLayout.CENTER);
        add(createInvestmentPanel(), BorderLayout.SOUTH);
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        
        JLabel title = new JLabel("Inventory");
        panel.add(title, BorderLayout.NORTH);

        actionsListPanel = new JPanel();
        actionsListPanel.setBackground(Color.BLACK);
        actionsListPanel.setLayout(new javax.swing.BoxLayout(
            actionsListPanel, javax.swing.BoxLayout.Y_AXIS
        ));

        JScrollPane scrollPane = new JScrollPane(actionsListPanel);
        scrollPane.setBackground(Color.BLACK);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void updateInventoryPanel() {
        // Each action is a row with its details and a sell button.
        
        Player p = this.thoth.player;
        for (Fund f : p.getActions().keySet()) {
            Action a = p.getActions().get(f);
            if (!shownActions.stream().anyMatch(obj -> obj[0] == a)) {
                JPanel actionPanel = new JPanel();
                actionPanel.setBackground(Color.BLACK);
                JLabel nameLabel = new JLabel("Fund: " + f.getName());
                nameLabel.setForeground(f.getColor());
                JLabel valueLabel = new JLabel("Value: " + df.format(a.getValue()));
                valueLabel.setForeground(Color.GREEN.darker());
                JLabel shareLabel = new JLabel("Shares: " + df.format(a.getShare()));
                shareLabel.setForeground(Color.BLUE.darker());
                JLabel dateLabel = new JLabel("Date: " + a.getBoughtTime());
                dateLabel.setForeground(Color.RED.darker());
                JButton sellButton = new JButton("Sell");
                sellButton.addActionListener((ActionEvent e) -> {
                    p.sellAction(a);
                    updatePanel(thoth);
                });
                actionPanel.add(nameLabel);
                actionPanel.add(valueLabel);
                actionPanel.add(shareLabel);
                actionPanel.add(dateLabel);
                actionPanel.add(sellButton);
                actionsListPanel.add(actionPanel);
                shownActions.add(new Object[]{a, valueLabel, shareLabel});
            } else {
                // Update existing action display
                shownActions.stream()
                    .filter(obj -> obj[0] == a)
                    .forEach(obj -> {
                        JLabel valueLabel = (JLabel) obj[1];
                        valueLabel.setText("Value: " + df.format(a.getValue()));
                        JLabel shareLabel = (JLabel) obj[2];
                        shareLabel.setText("Shares: " + df.format(a.getShare()));
                    });
                System.out.println("Updated action display for fund: " + f.getName() + ", value: " + a.getValue());
            }
        }
        // Remove sold actions from display
        shownActions.removeIf(obj -> {
            Action a = (Action) obj[0];
            if (!p.getActions().containsValue(a)) {
                // Find and remove the corresponding panel
                for (int i = 0; i < actionsListPanel.getComponentCount(); i++) {
                    JPanel actionPanel = (JPanel) actionsListPanel.getComponent(i);
                    JLabel nameLabel = (JLabel) actionPanel.getComponent(0);
                    if (nameLabel.getText().equals("Fund: " + a.getFund().getName())) {
                        actionsListPanel.remove(actionPanel);
                        break;
                    }
                }
                return true;
            }
            return false;
        });
        actionsListPanel.revalidate();
        actionsListPanel.repaint();
        repaint();
    }
}