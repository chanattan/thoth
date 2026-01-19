package thoth.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
    private ArrayList<Action> shownActions;
    private JLabel capitalLabel;
    private JPanel actionsListPanel;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public InvestorPanel(Thoth thoth) {
        this.thoth = thoth;
        capital = 0;
        elapsedTime = 0;

        shownActions = new ArrayList<Action>();
        actionsListPanel = new JPanel();
        actionsListPanel.setLayout(
            new javax.swing.BoxLayout(actionsListPanel, javax.swing.BoxLayout.Y_AXIS)
        );
        actionsListPanel.add(javax.swing.Box.createVerticalStrut(5));
        
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
                    // Logger
                    String action = selectedFund == thoth.prediction.fund ? "accept" :  "override"; // ignore ou choisit autre chose
                    
                    thoth.logger.logUserAction(
                        action,  // accept/override/ignore (override = ignore)
                        "Investissement dans " + selectedFund.getName()
                    );

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
    
	public void registerGlobalEnterKey(javax.swing.JRootPane rootPane) {
		javax.swing.InputMap inputMap = rootPane.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
		javax.swing.ActionMap actionMap = rootPane.getActionMap();
		
		inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "invest");
		actionMap.put("invest", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				investButton.doClick();
			}
		});
	}

    private JLabel makeHeaderLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setForeground(Color.WHITE);
        l.setFont(Thoth.customFont.deriveFont(Font.BOLD, 12f));
        return l;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        // header row
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(Color.BLACK);

        JLabel title = new JLabel("Inventory", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(Thoth.customFont.deriveFont(Font.BOLD, 16f));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        north.add(title, BorderLayout.NORTH);

        // column headers
        JPanel header = new JPanel(new GridLayout(1, 5));
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(BorderFactory.createEmptyBorder(2, 40, 2, 8));
        header.add(makeHeaderLabel("Fund"));
        header.add(makeHeaderLabel("Value"));
        header.add(makeHeaderLabel("Shares"));
        header.add(makeHeaderLabel("Date"));
        header.add(makeHeaderLabel("Action"));
        north.add(header, BorderLayout.CENTER);

        panel.add(north, BorderLayout.NORTH);

        actionsListPanel = new JPanel();
        actionsListPanel.setBackground(Color.BLACK);
        actionsListPanel.setLayout(new javax.swing.BoxLayout(
            actionsListPanel, javax.swing.BoxLayout.Y_AXIS
        ));

        JScrollPane scrollPane = new JScrollPane(actionsListPanel);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
            this.thumbColor = Window.THEME_COLOR;
            this.trackColor = Color.BLACK;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
            }
            private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
            }
        });
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionRow(Fund f, Action a) {
        JPanel actionRow = new JPanel(new GridLayout(1, 5));
        actionRow.setBackground(new Color(25, 25, 25));
        actionRow.setBorder(BorderFactory.createEmptyBorder(9, 33, 5, 5)); // match header padding
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        actionRow.setAlignmentX(LEFT_ALIGNMENT);

        JPanel fundCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fundCell.setOpaque(false);
        JLabel nameLabel = new JLabel(f.getName());
        nameLabel.setFont(Thoth.customFont.deriveFont(Font.BOLD, 15f));
        nameLabel.setForeground(f.getColor());
        fundCell.add(nameLabel);

        JPanel valueCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valueCell.setOpaque(false);
        JLabel valueLabel = new JLabel((a.getPlusValue() >= 0 ? "+" : "") + df.format(a.getPlusValue()) + "%");
        valueLabel.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 15f));
        valueLabel.setForeground(a.getPlusValue() >= 0 ? Color.GREEN.darker() : Color.RED.darker());
        valueCell.add(valueLabel);

        JPanel shareCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        shareCell.setOpaque(false);
        JLabel shareLabel = new JLabel(df.format(a.getShare()));
        shareLabel.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 15f));
        shareLabel.setForeground(Color.GRAY.brighter());
        shareCell.add(shareLabel);

        JPanel dateCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateCell.setOpaque(false);
        Object[] dateInfo = Thoth.getDateStatic(a.getBoughtTime());
        JLabel dateLabel = new JLabel(dateInfo[0] + "/" + dateInfo[1]);
        dateLabel.setFont(Thoth.customFont.deriveFont(Font.PLAIN, 15f));
        dateLabel.setForeground(Color.GRAY.brighter());
        dateCell.add(dateLabel);

        JPanel btnCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnCell.setOpaque(false);
        JButton sellButton = new JButton("SELL");
        sellButton.setFont(Thoth.customFont.deriveFont(Font.BOLD, 13f));
        sellButton.setOpaque(true);
        sellButton.setBackground(Color.LIGHT_GRAY);
        sellButton.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        sellButton.addActionListener(e -> {
            capitalLabel.setText("$" + df.format(thoth.player.getCapital()) + (a.getValue() >= 0 ? "+" : "-") + " $" + df.format(a.getValue()) + " [" + a.getFund().getName() + "]");
            thoth.player.sellAction(a);
            elapsedTime = thoth.window.getSimulator().getTime();
            updateInventoryPanel(); // refresh list
        });
        btnCell.add(sellButton);

        actionRow.add(fundCell);
        actionRow.add(valueCell);
        actionRow.add(shareCell);
        actionRow.add(dateCell);
        actionRow.add(btnCell);

        return actionRow;
    }

    public void updateInventoryPanel() {
        // Each action is a row with its details and a sell button.
        
        Player p = this.thoth.player;
        actionsListPanel.removeAll();
        actionsListPanel.add(Box.createVerticalStrut(5));
        
        // Rebuild all current actions
        for (Fund f : p.getActions().keySet()) {
            for (Action a : p.getActions().get(f)) {
                JPanel actionRow = createActionRow(f, a);
                actionsListPanel.add(actionRow);
                actionsListPanel.add(Box.createVerticalStrut(5));
            }
        }
        // Remove sold actions from display
        shownActions.removeIf(a -> {
            if (!p.getActions().containsKey(a.getFund()) || !p.getActions().get(a.getFund()).contains(a)) {
                actionsListPanel.removeAll();
                shownActions.remove(a);
                return true;
            }
            return false;
        });

        actionsListPanel.revalidate();
        actionsListPanel.repaint();
        repaint();
    }
}