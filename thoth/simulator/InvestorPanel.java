package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
    private JButton investButton;
    private JPanel inventoryPanel;
    private ArrayList<Object[]> shownActions;
    private JLabel capitalLabel;
    private JPanel actionsListPanel;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public InvestorPanel(Thoth thoth) {
        this.thoth = thoth;
        capital = 0;
        elapsedTime = 0;

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
        capitalLabel.setText("$" + df.format(capital));
    }

    private void setupPanel() {
        setLayout(new BorderLayout());
        add(createCapitalPanel(), BorderLayout.NORTH);
        inventoryPanel = createInventoryPanel();
        add(inventoryPanel, BorderLayout.CENTER);
        add(createInvestmentPanel(), BorderLayout.SOUTH);
        setBackground(Window.THEME_COLOR);
    }

    private JPanel createCapitalPanel() {
        JPanel panel = new JPanel();
        capitalLabel = new JLabel("$" + df.format(capital));
        capitalLabel.setOpaque(false);
        capitalLabel.setForeground(Color.BLACK);
        capitalLabel.setFont(Thoth.customFont.deriveFont(java.awt.Font.BOLD, 24f));
        capitalLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        capitalLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 20, 2, 20));
        panel.setBackground(Color.YELLOW);
        panel.add(capitalLabel);
        return panel;
    }

    private JPanel createInvestmentPanel() {
        JPanel panel = new JPanel();

        investmentField = new JTextField(10);
        investmentField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!((c >= '0') && (c <= '9') || (c == java.awt.event.KeyEvent.VK_BACK_SPACE) || (c == java.awt.event.KeyEvent.VK_DELETE) || (c == '.'))) {
                    evt.consume();
                }
            }
        });
        investmentField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                investmentField.select(0, investmentField.getText().length());
            }
        });
        
        investmentField.setOpaque(false);
        investmentField.setText("0");
        investmentField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        investmentField.setBackground(new Color(0, 0, 0, 0));
        investmentField.setCaretColor(Color.BLACK);
        investmentField.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 20f));
        investmentField.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        investButton = new JButton("Invest");
        investButton.setFont(Thoth.customFont.deriveFont(Font.BOLD, 20f));
        investButton.setOpaque(true);
        investButton.setBackground(Color.ORANGE);
        investButton.setBorder(null);
        // add enter action listener
        investButton.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    investButton.doClick();
                }
            }
        });
        investButton.addActionListener((ActionEvent e) -> {
            Fund selectedFund = thoth.window.getSimulator().selectedFund;
            if (investmentField.getText().isEmpty()) {
                investmentField.setText("0");
            }

            double val = Double.parseDouble(df.format(Double.parseDouble(investmentField.getText())));
            double c = thoth.player.getCapital();
            if (selectedFund != null && val > 0) {
                try {
                    thoth.player.invest(thoth.window.getSimulator().getTime(), val, selectedFund);
                    capitalLabel.setText("$" + df.format(c) + " - $" + df.format(val) + " [" + selectedFund.getName() + "]");
                    updateInventoryPanel();
                } catch (Player.InsufficientCapital ex) {
                    capitalLabel.setText("Insufficient capital!!");
                }
            }
            
            if (val > thoth.player.getCapital()) {
                investmentField.setText("" + df.format(thoth.player.getCapital()));
            }
        });
        investButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // animation
                investButton.setFont(Thoth.customFont.deriveFont(Font.BOLD, 22f));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                investButton.setFont(Thoth.customFont.deriveFont(Font.BOLD, 20f));
            }
        });
        panel.add(investmentField);
        panel.add(investButton);
        panel.setBackground(Color.ORANGE);
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
                JLabel valueLabel = new JLabel("Value: " + (a.getPlusValue() >= 0 ? "+" : "") + df.format(a.getPlusValue()) + "%");
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
                        valueLabel.setText("Value: " + (a.getPlusValue() >= 0 ? "+" : "") + df.format(a.getPlusValue()) + "%");
                        JLabel shareLabel = (JLabel) obj[2];
                        shareLabel.setText("Shares: " + df.format(a.getShare()));
                    });
                System.out.println("Updated action display for fund: " + f.getName() + ", value: " + a.getPlusValue());
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