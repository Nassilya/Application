package gab;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
/**
 * 
 * @author Jeanne
 *
 */
public class Ticket extends JFrame {
	
	private static final long serialVersionUID = 9086696308723308008L;
	
	private JPanel ticketPanel;
	private JLabel titre;
	private JLabel titre2;
	private JTextArea transactionLbl;
	
	public Ticket(String transaction) {
		super("Ticket");
		
		setSize(300, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setBackground(Color.WHITE);
		
		ticketPanel = new JPanel();
		ticketPanel.setSize(this.getSize());
		ticketPanel.setLayout(null);
		ticketPanel.setBackground(this.getBackground());
		
		setContentPane(ticketPanel);
		
		titre = new JLabel("Reçu", SwingConstants.CENTER);
		titre.setBackground(this.getBackground());
		titre.setFont(new Font("Arial", Font.BOLD, 30));
		titre.setSize(100, 100);
		titre.setLocation(100, 10);
		ticketPanel.add(titre);
		
		titre2 = new JLabel("Banque et Assurance de Paris Cité", SwingConstants.CENTER);
		titre2.setBackground(this.getBackground());
		titre2.setFont(new Font("Arial", Font.BOLD, 12));
		titre2.setSize(250, 100);
		titre2.setLocation(25, 50);
		ticketPanel.add(titre2);
		
		transactionLbl = new JTextArea(""+transaction);
		transactionLbl.setEditable(false);
		transactionLbl.setBackground(this.getBackground());
		transactionLbl.setSize(200, 100);
		transactionLbl.setFont(new Font("Arial", Font.BOLD, 13));
		transactionLbl.setLocation(50, 150);
		ticketPanel.add(transactionLbl);
		
		
		setVisible(true);
		
	}
}
