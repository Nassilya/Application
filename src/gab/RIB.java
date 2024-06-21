package gab;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class RIB extends JFrame {

	private static final long serialVersionUID = 3655316158867624211L;
	private JPanel ribPanel;
	private JLabel titre;
	private JLabel titre2;
	private JTextArea ribLbl;
	
	public RIB(String rib) {
		super("RIB");
		
	
		setSize(300, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setBackground(Color.WHITE);
		
		ribPanel = new JPanel();
		ribPanel.setSize(this.getSize());
		ribPanel.setLayout(null);
		ribPanel.setBackground(this.getBackground());
		
		setContentPane(ribPanel);
		
		titre = new JLabel("RIB", SwingConstants.CENTER);
		titre.setBackground(this.getBackground());
		titre.setFont(new Font("Arial", Font.BOLD, 30));
		titre.setSize(100, 100);
		titre.setLocation(100, 10);
		ribPanel.add(titre);
		
		titre2 = new JLabel("Banque et Assurance de Paris Cité", SwingConstants.CENTER);
		titre2.setBackground(this.getBackground());
		titre2.setFont(new Font("Arial", Font.BOLD, 18));
		titre2.setSize(250, 100);
		titre2.setLocation(25, 50);
		ribPanel.add(titre2);
		
		ribLbl = new JTextArea(""+rib);
		ribLbl.setEditable(false);
		ribLbl.setBackground(this.getBackground());
		ribLbl.setSize(200, 300);
		ribLbl.setFont(new Font("Arial", Font.BOLD, 11));
		ribLbl.setLocation(50, 90);
		ribPanel.add(ribLbl);
		
		setVisible(true);
		
	}
	
}
