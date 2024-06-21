package gab;
import javax.smartcardio.*;
import javax.swing.*;
import central.Central;
import secu.Securite;
import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;


/*Le module GAB est censé gérer l'interface graphique utilisateur :
 * - lire les données de la carte lu par la machine
 * - lire l'entrée clavier de l'utilisateur (saisie du code)
 * - gérer les interactions avec l'utilisateur (retirer/déposer de l'argent, consultant le solde actuel du compte). 
 * 
 * De plus, il fait le lien avec les 2 autres modules : (Centrale et Securité)
 * -- contexte : l'utilisateur insère la carte dans le lecteur et choisit l'opération qu'il souhaite effectuer. Il saisie ensuite le code.
 * 1) le code saisie  est crypté et vérifié par le module Sécurité
 * 
 * 2) Le module GAB va ensuite envoyer un message au préalable crypté au module Centrale : numéro de carte, opération souhaitée,
 * 		 par l'utilisateur (dépôt, retrait, etc...), 
 * -- !! si le code est erroné => envoie un message à l'interface graphique (annulation de la transaction au bout de 3 fois)
 * 
 * 3) Le module Centrale effectue l'opération choisie par l'utilisateur si elle est possible dans la BD et envoie un message au module GAB (traitement reussi/a échoué)
 * fonction du message reçu le module Gab effectuera une action :
 * - "consultation du solde" => affiche le solde du compte
 * - "impression" => renvoie les données du rib (à chaque impression le niveau de l'encre de l'imprimante et le niveau de papier diminuent)
 * - "retrait" => distribue les billet/ affiche un message de refus en cas de solde insufisant/dépassement de seuil (à chaque retrait le niveau de billet diminue)
 * 
 * 		
 * 				  */
/**
 * 
 * @author Jeanne
 *
 */
public class GAB {
	static int tentative = 1;
	
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		// Création de l'IU  _____________________________________________________________________________________________________
		/**
		 * 
		 */
		//relier les autres classes
		
		String url = "jdbc:mysql://localhost:3306/projet_gab"; //lien de la base de données à modifier
		String user = "root"; //nom de l'utilisateur
		String mdp = "ProjetGAB123"; // mot de passe de la BDD
		Central central = new Central(url, user, mdp);
		
		//clé pour le hachage du code secret 
		String key = new SimpleDateFormat("ddMMyyyy").format(new Date());
		
				//Constante (Couleurs, Dimensions)
				final Dimension dimensionFenetre = new Dimension(600,600);
				final Dimension dimensionEcran = new Dimension(550, 275);
				final Dimension dimensionBoutonMenu = new Dimension(175, 35);
				final Dimension tailleBtnMenuRetrait = new Dimension(175, 30);
				final Dimension dimensionPave = new Dimension(400, 200);
				final Color couleurAppareil = new Color(79, 79, 79);
				final Color couleurEcran = new Color(97, 25, 37);
				final Color couleurBoutonEcran = new Color(122, 49, 61);
				final Color couleurTexte = Color.WHITE;
				final Color couleurZoneSaisie = new Color(115, 64, 73);
				final Color couleurPave = new Color(59, 59, 59);
				final Font policeBtnMenu = new Font("Arial", Font.BOLD, 14);
				final Font policeBtnMenuRetrait = new Font("Arial", Font.BOLD, 16);
				
				//JLabel utilisés qui auront leurs contenus modifiés au cours de l'execution de l'appli
				 JLabel soldeLabel = new JLabel("Solde actuel du compte : ", SwingConstants.CENTER);
				 JLabel soldeLbl = new JLabel("",SwingConstants.CENTER);
				 JLabel resultatRetrait = new JLabel("Le solde est à présent de :", SwingConstants.CENTER);
				 JLabel soldeRetrait = new JLabel("", SwingConstants.CENTER);
				 JLabel resultatDepot = new JLabel("Le solde est à présent de :", SwingConstants.CENTER);
				 JLabel soldeDepot = new JLabel("",SwingConstants.CENTER);
				 JLabel soldeRefusRetrait = new JLabel("");
				 JLabel refusSolde = new JLabel("Solde actuel du compte : ");
				 JLabel seuilRestant = new JLabel("");
				 JLabel seuilErreur = new JLabel("Seuil restant : ");
				 
				 JLabel nbTentativeLbl = new JLabel("",SwingConstants.CENTER);
				 
				 
				 //tous les JTextField utilisé
				 JTextField zoneSaisieRetrait = new JTextField(5);
				 JPasswordField zoneCode = new JPasswordField(4);
				 JTextField saisieDepot = new JTextField(4);
				 JTextArea histoArea = new JTextArea("");
				 
				 //panel pour les pavé utilisé
				 JPanel pave = new JPanel();
				 JPanel pavNum = new JPanel(new GridLayout(4, 3, 5, 5));			
				
				 //panel pour les zones de saisie d'un montant
				 JPanel saisieCode = new JPanel();
				 JPanel saisieRetrait = new JPanel();
				  
				
				 /**
				  * @fenetre
				  */
				//Création de la fenêtre, source : "waytolearnx.com"
				   JFrame fenetre = new JFrame("Guichet Automatique Bancaire");
			       fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //"EXIT_ON_CLOSE" quitte l'application et libèrer la place en mémoire après avoir cliqué sur la croix
			       fenetre.setSize(dimensionFenetre);//dimension de la fenêtre      
			       fenetre.setLocationRelativeTo(null);// centre la fenêtre
			       fenetre.setResizable(false); //impossible de redimensionner      
			       //fenetre.setExtendedState(JFrame.MAXIMIZED_BOTH);// ouverture en plein écran, source : "askcodez.com"
			       
			       /**
			        * @panel
			        */			       
			       //config du panel principal
			       JPanel panel = new JPanel();//création et configuration du panel ecran
			       panel.setPreferredSize(dimensionFenetre);			       
			       //Changer la couleur de l'arrière
			       panel.setBackground(couleurAppareil); //couleur : vert foncé        
			      // panel.setLayout(new BorderLayout());// afin de pouvoir placer les composants (boutons) en fonction d'une position cardinal
			       panel.setLayout(null);	
			       fenetre.setContentPane(panel);
			       
			       
			       
			       
			       /**
			        * @CardLayout
			        */			       	
			       //CardLayout()
			       CardLayout cl = new CardLayout();
			       
			       //tableau pour stocker les cartes du cardlayout()
			       String[] affichages = {"Bienvenu", "Menu", "SaisieCode", "MenuRetrait", "SaisieRetrait", "RetirerCarte", "RetraitAccepte", "Solde", "Historique", "SaisieDepot", "Depot", "RetraitRefuse", "CarteBloquee", "RIB", "CarteNonReconnue", "Chargement"};//liste des conteneurs			      
			       
			       /**
			        * @ecran
			        */
			       //config du panel ecran
			       JPanel ecran = new JPanel();//création du panel afin de pouvoir des composants(boutons, menu, etc...)
			       ecran.setBackground(couleurEcran);
				   ecran.setSize(dimensionEcran);
				   ecran.setLayout(cl);
				   ecran.setLocation(18, 18);
				   
				   /**
				    * @panelPave
				    * On crée les differents panel qui serviront à stocker le bouton du pavé numérique
				    */
				   //config des panel pour les paves
				   pave.setLayout(null);
			       pave.setSize(dimensionPave);
			       pave.setBackground(couleurPave);
			       pave.setLocation(100, 330);

				   pavNum.setBackground(new Color(59, 59, 59));
				   pavNum.setSize(200, 190);
				   
				   
				   
				   					   
				   /**
				    * @cartes
				    * 
				    * On crée les différentes cartes correspondant aux affichages en fonction de l'action effectuée. Ces cartes seront stockées dans le @CardLayout cl
				    */
				   
				   //placer les conteneurs
				   /**
				    * @bienvenu
				    */
				    //création et configuration de l'écran de bienvenue
				    JPanel bienvenu = new JPanel();
				    bienvenu.setLayout(null);
				    bienvenu.setSize(ecran.getSize());
				    bienvenu.setBackground(couleurEcran);
				    JLabel inserer = new JLabel("Veuillez insérer votre carte.", SwingConstants.CENTER); //source : stackoverflow.com
				    inserer.setForeground(couleurTexte);
				    inserer.setFont(new Font("Arial", Font.BOLD, 30));
				    inserer.setBackground(couleurEcran);
				    inserer.setSize(400,75);
				    inserer.setLocation(75, 50);
				    bienvenu.add(inserer);
				  //panel pour le logo de la banque
				    JPanel logoPnl = new JPanel();
				    logoPnl.setBackground(new Color(82, 2, 16));
				    logoPnl.setSize(550, 30);
				    logoPnl.setLocation(0,0);
				    JLabel logoLbl =  new JLabel("Banque et Assurance de Paris Cité", SwingConstants.CENTER);
				    logoLbl.setBackground(new Color(82, 2, 16));
				    logoLbl.setForeground(couleurTexte);
				    logoLbl.setFont(new Font("Arial", Font.BOLD, 20));
				    logoLbl.setSize(logoPnl.getSize());
				    logoLbl.setLocation(0,0);
				    logoPnl.add(logoLbl);
				    bienvenu.add(logoPnl);    
				    	//ajout du bouton quitter -> permet de fermer l'application
				    JButton quitterBtn = new JButton("QUITTER");
				    quitterBtn.setBackground(couleurBoutonEcran);
				    quitterBtn.setForeground(couleurTexte);
				    quitterBtn.setFont(policeBtnMenuRetrait);
				    quitterBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    quitterBtn.setFocusPainted(false);			//|
				    quitterBtn.setMargin(new Insets(0,0,0,0));
				    quitterBtn.addActionListener(e ->{ 
				    	fenetre.dispose();//fermer la fenetre
				    	System.exit(0);//fermer l'application
				    	});
				    quitterBtn.setSize(150, 50);
				    quitterBtn.setLocation(200, 190 );
				    bienvenu.add(quitterBtn);
				    
				    JPanel echecPnl = new JPanel();
				    echecPnl.setLayout(null);
				    echecPnl.setSize(ecran.getSize());
				    echecPnl.setBackground(couleurEcran);
				    JLabel nonReconnueLbl = new JLabel("Carte non reconnue.", SwingConstants.CENTER); //source : stackoverflow.com
				    nonReconnueLbl.setForeground(Color.RED);
				    nonReconnueLbl.setFont(new Font("Arial", Font.BOLD, 30));
				    nonReconnueLbl.setBackground(couleurEcran);
				    nonReconnueLbl.setSize(400,75);
				    nonReconnueLbl.setLocation(75, 50);
				    echecPnl.add(nonReconnueLbl);
				    
				    /**
					  * @pageRib
					  */
					 //création de la page pour afficher le rib
					 JPanel pageRib = new JPanel();
					 pageRib.setBackground(couleurEcran);
					 pageRib.setLayout(null);
					 pageRib.setSize(ecran.getSize());
					 pageRib.setForeground(couleurTexte);
					 JButton imprimerBtn = new JButton("IMPRIMER");
					 imprimerBtn.setBackground(couleurBoutonEcran);
					 imprimerBtn.setForeground(couleurTexte);
					 imprimerBtn.setFont(new Font("Arial", Font.BOLD, 24));
					 imprimerBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
					 imprimerBtn.setFocusPainted(false);						//|
					 imprimerBtn.setMargin(new Insets(0,0,0,0));
					 imprimerBtn.setSize(300, 35);
					 imprimerBtn.setLocation(125,100);
					 //creation de tu label contenu l'instruction après avoir cliqué sur le bouton imprimer
					 JLabel ribCarteLbl = new JLabel("Vous pouvez à présent retirer votre carte.", SwingConstants.CENTER);
					 ribCarteLbl.setForeground(couleurTexte);
					 ribCarteLbl.setFont(new Font("Arial", Font.BOLD, 20));
					 ribCarteLbl.setBackground(couleurEcran);
					 ribCarteLbl.setSize(400, 45);
					 ribCarteLbl.setLocation(75, 50);			 
					 imprimerBtn.addActionListener(e ->{
						 RIB rib = new RIB(central.impressionRib());
						 pageRib.remove(imprimerBtn);
						 pageRib.add(ribCarteLbl);
						 pageRib.revalidate();
						 pageRib.repaint();
					 });
				    
				    /**
				     * @menu
				     */				

				    //création et config du menu (choix opérations)
				    JPanel menu = new JPanel();
				    menu.setLayout(null);
				    menu.setSize(ecran.getSize());
				    menu.setBackground(couleurEcran);
				    JLabel titreMenu = new JLabel("Quelle opération souhaitez-vous effectuer ?", SwingConstants.CENTER);
				    titreMenu.setForeground(couleurTexte);
				    titreMenu.setFont(new Font("Arial", Font.BOLD, 18));
				    titreMenu.setBackground(couleurEcran);
				    titreMenu.setSize(450, 45);
				    titreMenu.setLocation(50, 50);
				    menu.add(titreMenu);
				    //bouton retrait
				    JButton retraitBtn = new JButton("RETRAIT");
				    retraitBtn.setHorizontalAlignment(SwingConstants.LEFT);  //aligne le libellé du bouton à gauche source : stackoverflow.com
				    retraitBtn.setBackground(couleurBoutonEcran);
				    retraitBtn.setForeground(couleurTexte);
				    retraitBtn.setFont(policeBtnMenu);
				    retraitBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    retraitBtn.setFocusPainted(false);			//|
				    retraitBtn.setMargin(new Insets(0,0,0,0));
				    retraitBtn.addActionListener(e ->{ cl.show(ecran, "MenuRetrait");});
				    retraitBtn.setSize(dimensionBoutonMenu);
				    retraitBtn.setLocation(40, 115);
				    menu.add(retraitBtn);
				    //bouton dépôt
				    JButton depotBtn = new JButton("DEPOT ");
				    depotBtn.setHorizontalAlignment(SwingConstants.RIGHT); 
				    depotBtn.setBackground(couleurBoutonEcran);
				    depotBtn.setForeground(couleurTexte);
				    depotBtn.setFont(policeBtnMenu);
				    depotBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    depotBtn.setFocusPainted(false);			//|
				    depotBtn.setMargin(new Insets(0,0,0,0)); 
				    depotBtn.addActionListener(e ->{
				    	cl.show(ecran, "SaisieDepot");});
				    depotBtn.setSize(dimensionBoutonMenu);
				    depotBtn.setLocation(335, 115);
				    menu.add(depotBtn);
				    //bouton "consulter le solde"
				    JButton consultationBtn = new JButton("CONSULTER LE SOLDE");
				    consultationBtn.setHorizontalAlignment(SwingConstants.RIGHT); 
				    consultationBtn.setBackground(couleurBoutonEcran);
				    consultationBtn.setForeground(couleurTexte);
				    consultationBtn.setFont(policeBtnMenu);
				    consultationBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    consultationBtn.setFocusPainted(false);			//|
				    consultationBtn.setMargin(new Insets(0,0,0,0));
				    consultationBtn.addActionListener(e ->{ 
				    	soldeLbl.setText("");
				    	soldeLbl.setText(soldeLbl.getText()+central.getInfoById());
				    	cl.show(ecran, "Solde");});
				    consultationBtn.setSize(dimensionBoutonMenu);
				    consultationBtn.setLocation(335, 165);
				    menu.add(consultationBtn);
				    //bouton "imprimer le rib"
				    JButton impressionBtn = new JButton("IMPRIMER LE RIB");
				    impressionBtn.setHorizontalAlignment(SwingConstants.LEFT); 
				    impressionBtn.setBackground(couleurBoutonEcran);
				    impressionBtn.setForeground(couleurTexte);
				    impressionBtn.setFont(policeBtnMenu);
				    impressionBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    impressionBtn.setFocusPainted(false);			//|
				    impressionBtn.setMargin(new Insets(0,0,0,0));
				    impressionBtn.addActionListener(e ->{ 
				    	pageRib.add(imprimerBtn);
				    	cl.show(ecran,"RIB");});
				    impressionBtn.setSize(dimensionBoutonMenu);
				    impressionBtn.setLocation(40, 165);
				    
				    
				    JButton historisqueBtn = new JButton("HISTORIQUE");
				    historisqueBtn.setHorizontalAlignment(SwingConstants.LEFT); 
				    historisqueBtn.setBackground(couleurBoutonEcran);
				    historisqueBtn.setForeground(couleurTexte);
				    historisqueBtn.setFont(policeBtnMenu);
				    historisqueBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    historisqueBtn.setFocusPainted(false);			//|
				    historisqueBtn.setMargin(new Insets(0,0,0,0));
				    historisqueBtn.addActionListener(e ->{ 
				    	histoArea.setText("");
				    	histoArea.setText(histoArea.getText()+ central.afficherHistorique());
				    	cl.show(ecran,"Historique");});
				    historisqueBtn.setSize(dimensionBoutonMenu);
				    historisqueBtn.setLocation(40, 215);
				    
				    
				    //bouton "retirer carte"
				    JButton retirerCarteBtn = new JButton("RETIRER LA CARTE");
				    retirerCarteBtn.setHorizontalAlignment(SwingConstants.RIGHT); 
				    retirerCarteBtn.setBackground(couleurBoutonEcran);
				    retirerCarteBtn.setForeground(couleurTexte);
				    retirerCarteBtn.setFont(policeBtnMenu);
				    retirerCarteBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    retirerCarteBtn.setFocusPainted(false);			//|
				    retirerCarteBtn.setMargin(new Insets(0,0,0,0));
				    retirerCarteBtn.addActionListener(e ->{ cl.show(ecran,"RetirerCarte");});
				    retirerCarteBtn.setSize(dimensionBoutonMenu);
				    retirerCarteBtn.setLocation(335, 215);
				    menu.add(retirerCarteBtn);
				    /**
				     * @saisieCode
				     */
				    //creation et config de l'écran de saisie du code secret (pas fini)
				    //JPanel saisieCode = new JPanel();
				    saisieCode.setLayout(null);
				    saisieCode.setSize(ecran.getSize());
				    saisieCode.setBackground(couleurEcran);
				    JLabel titreSaisieCode = new JLabel("Veuillez tapez entrer votre code confidentiel", SwingConstants.CENTER);
				    titreSaisieCode.setForeground(couleurTexte);
				    titreSaisieCode.setFont(new Font("Arial", Font.BOLD, 18));
				    titreSaisieCode.setBackground(couleurEcran);
				    titreSaisieCode.setSize(400, 30);
				    titreSaisieCode.setLocation(75, 50);
				    saisieCode.add(titreSaisieCode);	    
				    //Zone de texte
			        //JPasswordField zoneCode = new JPasswordField(4);
			        zoneCode.setBackground(couleurEcran);
			        zoneCode.setBorder(BorderFactory.createEmptyBorder());
				    zoneCode.setEnabled(false);
				    zoneCode.setBackground(couleurZoneSaisie);
				    zoneCode.setFont(new Font("Arial", Font.BOLD, 30));
				    zoneCode.setForeground(couleurTexte);
				    zoneCode.setHorizontalAlignment(JTextField.CENTER);
				    zoneCode.setSize(300, 75);
				    zoneCode.setLocation(125, 125);
				    saisieCode.add(zoneCode);
				    //texte en cas d'erreur
				    JLabel erreurCodeLbl = new JLabel("Code erroné", SwingConstants.CENTER);
				    erreurCodeLbl.setForeground(Color.RED);
				    erreurCodeLbl.setBackground(couleurEcran);
				    erreurCodeLbl.setFont(new Font("Arial",Font.BOLD, 12));
				    erreurCodeLbl.setSize(100, 25);
				    erreurCodeLbl.setLocation(225, 80);
				    
					JLabel tentativeLbl = new JLabel("Tentative(s) restante(s) : ",SwingConstants.CENTER);
					tentativeLbl.setForeground(Color.RED);
					tentativeLbl.setBackground(couleurEcran);
					tentativeLbl.setFont(new Font("Arial",Font.BOLD, 12));
					tentativeLbl.setSize(150, 25);
					tentativeLbl.setLocation(200, 95);
					
					nbTentativeLbl.setForeground(Color.RED);
					nbTentativeLbl.setBackground(couleurEcran);
					nbTentativeLbl.setFont(new Font("Arial", Font.BOLD, 12));
					nbTentativeLbl.setSize(25, 25);
					nbTentativeLbl.setLocation(350, 95);
				    
				    /**
				     * @pageCarteBloquee
				     */
				    //création et config de la page s'affichant lorsque la carte se bloque à la suite de 3 tentative errone de la saisie du code
				    JPanel pageCarteBloquee = new JPanel();
				    pageCarteBloquee.setSize(ecran.getSize());
				    pageCarteBloquee.setBackground(couleurEcran);
				    pageCarteBloquee.setLayout(null);
				    JLabel carteBloqueeLbl = new JLabel("Carte Bloquée !", SwingConstants.CENTER);
				    carteBloqueeLbl.setForeground(Color.RED);
				    carteBloqueeLbl.setFont(new Font("Arial", Font.BOLD, 36));
				    carteBloqueeLbl.setBackground(couleurEcran);
				    carteBloqueeLbl.setSize(400,50);
				    carteBloqueeLbl.setLocation(75, 110);
				    pageCarteBloquee.add(carteBloqueeLbl);
				    JLabel retirerCarteLbl = new JLabel("Veuillez retirer la carte.", SwingConstants.CENTER);
				    retirerCarteLbl.setForeground(couleurTexte);
				    retirerCarteLbl.setBackground(couleurEcran);
				    retirerCarteLbl.setFont(new Font("Arial", Font.BOLD, 18));
				    retirerCarteLbl.setSize(400, 45);
				    retirerCarteLbl.setLocation(75, 220);
				    pageCarteBloquee.add(retirerCarteLbl);
				    
				    /**
				     * @pageChargement
				     */
				    JPanel pageChargement = new JPanel();
				    pageChargement.setSize(dimensionEcran);
				    pageChargement.setBackground(couleurEcran);
				    pageChargement.setLayout(null);
				    JLabel chargementLbl = new JLabel("Chargement...",SwingConstants.CENTER);
				    chargementLbl.setForeground(couleurTexte);
				    chargementLbl.setBackground(couleurEcran);
				    chargementLbl.setFont(new Font("Arial", Font.BOLD, 20));
				    chargementLbl.setSize(200, 50);
				    chargementLbl.setLocation(175, 100);
				    pageChargement.add(chargementLbl);
				    
				    /**
				     * @menuRetrait
				     */
				    //création et config du menu de retrait (choix du montant à retirer)
				    JPanel menuRetrait = new JPanel();
				    menuRetrait.setLayout(null);
				    menuRetrait.setSize(ecran.getSize());
				    menuRetrait.setBackground(couleurEcran);
				    JLabel titreMenuRetrait = new JLabel("Quel montant souhaitez-vous retirer ?", SwingConstants.CENTER);
				    titreMenuRetrait.setForeground(couleurTexte);
				    titreMenuRetrait.setFont(new Font("Arial", Font.BOLD, 18));
				    titreMenuRetrait.setBackground(couleurEcran);
				    titreMenuRetrait.setSize(450, 45);
				    titreMenuRetrait.setLocation(50, 50);
				    menuRetrait.add(titreMenuRetrait);
				    	//bouton 20 euros
				    JButton btn20 = new JButton("20€");
				    btn20.setHorizontalAlignment(SwingConstants.LEFT); 
				    btn20.setBackground(couleurBoutonEcran);
				    btn20.setForeground(couleurTexte);
				    btn20.setFont(policeBtnMenuRetrait);
				    btn20.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn20.setFocusPainted(false);			//|
				    btn20.setMargin(new Insets(0,0,0,0));
				    btn20.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		if(central.retrait(20)){
				    			soldeRetrait.setText("");
				    			soldeRetrait.setText(soldeRetrait.getText()+central.getInfoById());
				    			cl.show(ecran, "RetraitAccepte");
				    		}
				    		else {
				    			soldeRefusRetrait.setText("");
				    			soldeRefusRetrait.setText(soldeRefusRetrait.getText()+central.getInfoById());
				    			seuilRestant.setText("");
				    			seuilRestant.setText(seuilRestant.getText()+central.getSeuilRestantById());
				    			cl.show(ecran, "RetraitRefuse");
				    		}
				    	});
				    	thread.start();
				    });
				    btn20.setSize(tailleBtnMenuRetrait);
				    btn20.setLocation(40, 110 );
				    menuRetrait.add(btn20);
				    	//bouton 40 euros
				    JButton btn40 = new JButton("40€");
				    btn40.setHorizontalAlignment(SwingConstants.LEFT); 
				    btn40.setBackground(couleurBoutonEcran);
				    btn40.setForeground(couleurTexte);
				    btn40.setFont(policeBtnMenuRetrait);
				    btn40.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn40.setFocusPainted(false);			//|
				    btn40.setMargin(new Insets(0,0,0,0));
				    btn40.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		if(central.retrait(40)){
				    			soldeRetrait.setText("");
				    			soldeRetrait.setText(soldeRetrait.getText()+central.getInfoById());
				    			cl.show(ecran, "RetraitAccepte");
				    		}
				    		else {
				    			soldeRefusRetrait.setText("");
				    			soldeRefusRetrait.setText(soldeRefusRetrait.getText()+central.getInfoById());
				    			seuilRestant.setText("");
				    			seuilRestant.setText(seuilRestant.getText()+central.getSeuilRestantById());
				    			cl.show(ecran, "RetraitRefuse");
				    		}
				    	});
				    	thread.start();
				    });		    
				    btn40.setSize(tailleBtnMenuRetrait);
				    btn40.setLocation(40, 150 );
				    menuRetrait.add(btn40);
				    	//bouton 50 euros
				    JButton btn50 = new JButton("50€");
				    btn50.setHorizontalAlignment(SwingConstants.LEFT); 
				    btn50.setBackground(couleurBoutonEcran);
				    btn50.setForeground(couleurTexte);
				    btn50.setFont(policeBtnMenuRetrait);
				    btn50.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn50.setFocusPainted(false);			//|
				    btn50.setMargin(new Insets(0,0,0,0));
				    btn50.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		if(central.retrait(50)){
				    			soldeRetrait.setText("");
				    			soldeRetrait.setText(soldeRetrait.getText()+central.getInfoById());
				    			cl.show(ecran, "RetraitAccepte");
				    		}
				    		else {
				    			soldeRefusRetrait.setText("");
				    			soldeRefusRetrait.setText(soldeRefusRetrait.getText()+central.getInfoById());
				    			seuilRestant.setText("");
				    			seuilRestant.setText(seuilRestant.getText()+central.getSeuilRestantById());
				    			cl.show(ecran, "RetraitRefuse");
				    		}
				    	});
				    	thread.start();
				    });
				    btn50.setSize(tailleBtnMenuRetrait);
				    btn50.setLocation(40, 190 );
				    menuRetrait.add(btn50);
				    	//bouton 70 euros
				    JButton btn70 = new JButton("70€");
				    btn70.setHorizontalAlignment(SwingConstants.RIGHT); 
				    btn70.setBackground(couleurBoutonEcran);
				    btn70.setForeground(couleurTexte);
				    btn70.setFont(policeBtnMenuRetrait);
				    btn70.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn70.setFocusPainted(false);			//|
				    btn70.setMargin(new Insets(0,0,0,0));
				    btn70.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		if(central.retrait(70)){
				    			soldeRetrait.setText("");
				    			soldeRetrait.setText(soldeRetrait.getText()+central.getInfoById());
				    			cl.show(ecran, "RetraitAccepte");
				    		}
				    		else {
				    			soldeRefusRetrait.setText("");
				    			soldeRefusRetrait.setText(soldeRefusRetrait.getText()+central.getInfoById());
				    			seuilRestant.setText("");
				    			seuilRestant.setText(seuilRestant.getText()+central.getSeuilRestantById());
				    			cl.show(ecran, "RetraitRefuse");
				    		}
				    	});
				    	thread.start();
				    });
				    btn70.setSize(tailleBtnMenuRetrait);
				    btn70.setLocation(335, 110 );
				    menuRetrait.add(btn70);
				    	//bouton 80 euros
				    JButton btn80 = new JButton("80€");
				    btn80.setHorizontalAlignment(SwingConstants.RIGHT); 
				    btn80.setBackground(couleurBoutonEcran);
				    btn80.setForeground(couleurTexte);
				    btn80.setFont(policeBtnMenuRetrait);
				    btn80.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn80.setFocusPainted(false);			//|
				    btn80.setMargin(new Insets(0,0,0,0));
				    btn80.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		if(central.retrait(80)){
				    			soldeRetrait.setText("");
				    			soldeRetrait.setText(soldeRetrait.getText()+central.getInfoById());
				    			cl.show(ecran, "RetraitAccepte");
				    		}
				    		else {
				    			soldeRefusRetrait.setText("");
				    			soldeRefusRetrait.setText(soldeRefusRetrait.getText()+central.getInfoById());
				    			seuilRestant.setText("");
				    			seuilRestant.setText(seuilRestant.getText()+central.getSeuilRestantById());
				    			cl.show(ecran, "RetraitRefuse");
				    		}
				    	});
				    	thread.start();
				    });
				    btn80.setSize(tailleBtnMenuRetrait);
				    btn80.setLocation(335, 150 );
				    menuRetrait.add(btn80);
				    	//bouton 100
				    JButton btn100 = new JButton("100€");
				    btn100.setHorizontalAlignment(SwingConstants.RIGHT); 
				    btn100.setBackground(couleurBoutonEcran);
				    btn100.setForeground(couleurTexte);
				    btn100.setFont(policeBtnMenuRetrait);
				    btn100.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn100.setFocusPainted(false);			//|
				    btn100.setMargin(new Insets(0,0,0,0));
				    btn100.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		if(central.retrait(100)){
				    			soldeRetrait.setText("");
				    			soldeRetrait.setText(soldeRetrait.getText()+central.getInfoById());
				    			cl.show(ecran, "RetraitAccepte");
				    		}
				    		else {
				    			soldeRefusRetrait.setText("");
				    			soldeRefusRetrait.setText(soldeRefusRetrait.getText()+central.getInfoById());
				    			seuilRestant.setText("");
				    			seuilRestant.setText(seuilRestant.getText()+central.getSeuilRestantById());
				    			cl.show(ecran, "RetraitRefuse");
				    		}
				    	});
				    	thread.start();
				    });
				    btn100.setSize(tailleBtnMenuRetrait);
				    btn100.setLocation(335, 190 );
				    menuRetrait.add(btn100);
				    	//bouton retour
				    JButton btnRetour = new JButton("RETOUR");
				    btnRetour.setHorizontalAlignment(SwingConstants.LEFT); 
				    btnRetour.setBackground(couleurBoutonEcran);
				    btnRetour.setForeground(couleurTexte);
				    btnRetour.setFont(new Font("Arial", Font.BOLD, 12));
				    btnRetour.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnRetour.setFocusPainted(false);			//|
				    btnRetour.setMargin(new Insets(0,0,0,0));
				    btnRetour.addActionListener(e ->{ cl.show(ecran, affichages[1]);});
				    btnRetour.setSize(tailleBtnMenuRetrait);
				    btnRetour.setLocation(40, 230);
				    menuRetrait.add(btnRetour);
				    	//bouton saisir (pour saisir un autre montant)
				    JButton btnSaisir = new JButton("SAISIR UN AUTRE MONTANT");
				    btnSaisir.setHorizontalAlignment(SwingConstants.RIGHT); 
				    btnSaisir.setBackground(couleurBoutonEcran);
				    btnSaisir.setForeground(couleurTexte);
				    btnSaisir.setFont(new Font("Arial", Font.BOLD, 12));
				    btnSaisir.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnSaisir.setFocusPainted(false);			//|
				    btnSaisir.setMargin(new Insets(0,0,0,0));
				    btnSaisir.addActionListener(e ->{	
				    	cl.show(ecran, "SaisieRetrait");			
				    });
				    btnSaisir.setSize(tailleBtnMenuRetrait);
				    btnSaisir.setLocation(335, 230);
				    menuRetrait.add(btnSaisir);

				    /**
				     * @saisieRetrait
				     */
				    //création et config de la page d'affiche de la saisie du montant à retirer
				   
				    saisieRetrait.setLayout(null);
				    saisieRetrait.setSize(ecran.getSize());
				    saisieRetrait.setBackground(couleurEcran);
				    JLabel titreSaisieMontant = new JLabel("Quel montant souhaitez-vous retirer ?", SwingConstants.CENTER);
				    titreSaisieMontant.setForeground(couleurTexte);
				    titreSaisieMontant.setFont(new Font("Arial", Font.BOLD, 18));
				    titreSaisieMontant.setBackground(couleurEcran);
				    titreSaisieMontant.setSize(400, 30);
				    titreSaisieMontant.setLocation(75, 50);
				    saisieRetrait.add(titreSaisieMontant);	    
				    	//Zone de texte      
			        zoneSaisieRetrait.setBorder(BorderFactory.createEmptyBorder());
			        zoneSaisieRetrait.setEnabled(false);
			        zoneSaisieRetrait.setBackground(couleurZoneSaisie);
			        zoneSaisieRetrait.setForeground(couleurTexte);
			        zoneSaisieRetrait.setHorizontalAlignment(SwingConstants.CENTER);
			        zoneSaisieRetrait.setFont(new Font("Arial", Font.BOLD, 30));
			        zoneSaisieRetrait.setSize(200, 50);
			        zoneSaisieRetrait.setLocation(175, 100);					    
				    saisieRetrait.add(zoneSaisieRetrait);
					//bouton pour rajouter 10 euros au montant à retirer
				    JButton btn10Retrait = new JButton("10");
				    btn10Retrait.setBackground(couleurBoutonEcran);
				    btn10Retrait.setForeground(couleurTexte);
				    btn10Retrait.setFont(new Font("Arial", Font.BOLD, 24));
				    btn10Retrait.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn10Retrait.setFocusPainted(false);						//|
				    btn10Retrait.setMargin(new Insets(0,0,0,0));
				    btn10Retrait.addActionListener(e ->{
				    	if(zoneSaisieRetrait.getText().equals(""))
				    		zoneSaisieRetrait.setText("10");
				    	else {
				    		zoneSaisieRetrait.setText(""+(Integer.parseInt(zoneSaisieRetrait.getText())+10));
				    	}
				    });
				    btn10Retrait.setSize(90, 35);
				    btn10Retrait.setLocation(65,180);				    
				    saisieRetrait.add(btn10Retrait);
				    	//bouton pour rajouter 20 euros au montant à retirer
				    JButton btn20Retrait = new JButton("20");
				    btn20Retrait.setBackground(couleurBoutonEcran);
				    btn20Retrait.setForeground(couleurTexte);
				    btn20Retrait.setFont(new Font("Arial", Font.BOLD, 24));
				    btn20Retrait.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn20Retrait.setFocusPainted(false);						//|
				    btn20Retrait.setMargin(new Insets(0,0,0,0));
				    btn20Retrait.addActionListener(e ->{
				    	if(zoneSaisieRetrait.getText().equals(""))
				    		zoneSaisieRetrait.setText("20");
				    	else {
				    		zoneSaisieRetrait.setText(""+(Integer.parseInt(zoneSaisieRetrait.getText())+20));
				    	}
				    });
				    btn20Retrait.setSize(90, 35);
				    btn20Retrait.setLocation(175,180);				    
				    saisieRetrait.add(btn20Retrait);
				    	//bouton pour rajouter 50 euros au montant à retirer
				    JButton btn50Retrait = new JButton("50");
				    btn50Retrait.setBackground(couleurBoutonEcran);
				    btn50Retrait.setForeground(couleurTexte);
				    btn50Retrait.setFont(new Font("Arial", Font.BOLD, 24));
				    btn50Retrait.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn50Retrait.setFocusPainted(false);						//|
				    btn50Retrait.setMargin(new Insets(0,0,0,0));
				    btn50Retrait.addActionListener(e ->{
				    	if(zoneSaisieRetrait.getText().equals(""))
				    		zoneSaisieRetrait.setText("50");
				    	else {
				    		zoneSaisieRetrait.setText(""+(Integer.parseInt(zoneSaisieRetrait.getText())+50));
				    	}
				    });
				    btn50Retrait.setSize(90, 35);
				    btn50Retrait.setLocation(285,180);				    
				    saisieRetrait.add(btn50Retrait);
				    	//bouton pour rajouter 100 euros au montant à retirer
				    JButton btn100Retrait = new JButton("100");
				    btn100Retrait.setBackground(couleurBoutonEcran);
				    btn100Retrait.setForeground(couleurTexte);
				    btn100Retrait.setFont(new Font("Arial", Font.BOLD, 24));
				    btn100Retrait.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn100Retrait.setFocusPainted(false);						//|
				    btn100Retrait.setMargin(new Insets(0,0,0,0));
				    btn100Retrait.addActionListener(e ->{
				    	if(zoneSaisieRetrait.getText().equals(""))
				    		zoneSaisieRetrait.setText("100");
				    	else {
				    		zoneSaisieRetrait.setText(""+(Integer.parseInt(zoneSaisieRetrait.getText())+100));
				    	}
				    });
				    btn100Retrait.setSize(90, 35);
				    btn100Retrait.setLocation(385,180);				    
				    saisieRetrait.add(btn100Retrait);
				     //bouton pour effacer la zone de saisie
				    JButton btnEffacerRetrait = new JButton("EFFACER");
				    btnEffacerRetrait.setBackground(Color.RED);
				    btnEffacerRetrait.setForeground(couleurTexte);
				    btnEffacerRetrait.setFont(new Font("Arial", Font.BOLD, 10));
				    btnEffacerRetrait.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnEffacerRetrait.setFocusPainted(false);						//|
				    btnEffacerRetrait.setMargin(new Insets(0,0,0,0));
				    btnEffacerRetrait.addActionListener(e ->{
				    	zoneSaisieRetrait.setText("");
				    });
				    btnEffacerRetrait.setSize(50, 40);
				    btnEffacerRetrait.setLocation(380,105);				    
				    saisieRetrait.add(btnEffacerRetrait);
				    	//bouton pour valider le montant à retirer
				    JButton btnValiderRetrait = new JButton("VALIDER");
				    btnValiderRetrait.setBackground(Color.GREEN);
				    btnValiderRetrait.setForeground(couleurTexte);
				    btnValiderRetrait.setFont(new Font("Arial", Font.BOLD, 10));
				    btnValiderRetrait.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnValiderRetrait.setFocusPainted(false);						//|
				    btnValiderRetrait.setMargin(new Insets(0,0,0,0));
				    btnValiderRetrait.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		if(central.retrait(Integer.parseInt(zoneSaisieRetrait.getText()))){
				    			soldeRetrait.setText("");
				    			soldeRetrait.setText(soldeRetrait.getText()+central.getInfoById());
				    			cl.show(ecran, "RetraitAccepte");
				    		}
				    		else {
				    			soldeRefusRetrait.setText("");
				    			soldeRefusRetrait.setText(soldeRefusRetrait.getText()+central.getInfoById());
				    			seuilRestant.setText("");
				    			seuilRestant.setText(seuilRestant.getText()+central.getSeuilRestantById());
				    			cl.show(ecran, "RetraitRefuse");
				    		}
				    	});
				    	thread.start();
				    });			    	
				    btnValiderRetrait.setSize(50, 40);
				    btnValiderRetrait.setLocation(120,105);				    
				    saisieRetrait.add(btnValiderRetrait);
				    	//bouton retour
				    JButton btnRetourRetrait = new JButton("RETOUR");
				    btnRetourRetrait.setBackground(couleurBoutonEcran);
				    btnRetourRetrait.setForeground(couleurTexte);
				    btnRetourRetrait.setFont(new Font("Arial", Font.BOLD, 12));
				    btnRetourRetrait.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnRetourRetrait.setFocusPainted(false);						//|
				    btnRetourRetrait.setMargin(new Insets(0,0,0,0));
				    btnRetourRetrait.addActionListener(e ->{
				    	cl.show(ecran, "MenuRetrait");
				    });
				    btnRetourRetrait.setSize(550, 35);
				    btnRetourRetrait.setLocation(0,240);				    
				    saisieRetrait.add(btnRetourRetrait);
				    
				    
				    
				    /**
				     * @retirerLaCarte
				     */
				   //création et config de la page pour retirer la carte
				   JPanel retirerLaCarte = new JPanel();
				   retirerLaCarte.setLayout(null);
				   retirerLaCarte.setSize(ecran.getSize());
				   retirerLaCarte.setBackground(couleurEcran);
				   JLabel titreRetirerCarte = new JLabel("Veuillez retirer votre carte.", SwingConstants.CENTER);
				   titreRetirerCarte.setForeground(couleurTexte);
				   titreRetirerCarte.setFont(new Font("Arial", Font.BOLD, 30));
				   titreRetirerCarte.setBackground(couleurEcran);
				   titreRetirerCarte.setSize(400, 45);
				   titreRetirerCarte.setLocation(75, 50);
				   retirerLaCarte.add(titreRetirerCarte);
				   
				    /**
				     * @pageRetraitAccepte
				     */
				   //création de la page d'affichage du retrait s'il a puêtre effectué
				   JPanel pageRetraitAccepte = new JPanel();
				   pageRetraitAccepte.setBackground(couleurEcran);
				   pageRetraitAccepte.setLayout(null);
				   pageRetraitAccepte.setSize(ecran.getSize());
				   resultatRetrait.setForeground(couleurTexte);
				   resultatRetrait.setFont(new Font("Arial", Font.BOLD, 24));
				   resultatRetrait.setSize(450, 45);
				   resultatRetrait.setLocation(50, 50);
				   pageRetraitAccepte.add(resultatRetrait);
				   soldeRetrait.setForeground(couleurTexte);
				   soldeRetrait.setFont(new Font("Arial", Font.BOLD, 30));
				   soldeRetrait.setSize(100,100);
				   soldeRetrait.setLocation(225, 75);
				   pageRetraitAccepte.add(soldeRetrait);
				   	//bouton pour imprimer un reçu
				   JButton imprimerRecuBtn = new JButton("RECU");
				   imprimerRecuBtn.setBackground(couleurBoutonEcran);
				   imprimerRecuBtn.setForeground(couleurTexte);
				   imprimerRecuBtn.setFont(new Font("Arial", Font.BOLD, 12));
				   imprimerRecuBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				   imprimerRecuBtn.setFocusPainted(false);						//|
				   imprimerRecuBtn.setMargin(new Insets(0,0,0,0));
				   imprimerRecuBtn.addActionListener(e ->{
					   	Ticket ticket = new Ticket(central.impressionRecu());
				    	cl.show(ecran, "RetirerCarte");
				    });
				   imprimerRecuBtn.setSize(270, 35);
				   imprimerRecuBtn.setLocation(0,240);				    
				   pageRetraitAccepte.add(imprimerRecuBtn);
				   
					//bouton pour terminer la transaction sans reçu
				   JButton sansREBtn = new JButton("PAS DE RECU");
				   sansREBtn.setBackground(couleurBoutonEcran);
				   sansREBtn.setForeground(couleurTexte);
				   sansREBtn.setFont(new Font("Arial", Font.BOLD, 12));
				   sansREBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				   sansREBtn.setFocusPainted(false);						//|
				   sansREBtn.setMargin(new Insets(0,0,0,0));
				   sansREBtn.addActionListener(e ->{
				    	cl.show(ecran, "RetirerCarte");
				    });
				   sansREBtn.setSize(270, 35);
				   sansREBtn.setLocation(280,240);				    
				   pageRetraitAccepte.add(sansREBtn);
				    
				   /**
				    * @pageRetraitRefuse
				    */
				   //création de la d'affiche du retrait en cas de refus
				   JPanel pageRetraitRefuse = new JPanel();
				   pageRetraitRefuse.setBackground(couleurEcran);
				   pageRetraitRefuse.setLayout(null);
				   pageRetraitRefuse.setSize(ecran.getSize());
				   pageRetraitRefuse.setForeground(couleurTexte);
				   JLabel retraitRefuseLbl = new JLabel("Impossible d'effectuer le retrait.", SwingConstants.CENTER);
				   retraitRefuseLbl.setForeground(couleurTexte);
				   retraitRefuseLbl.setFont(new Font("Arial", Font.BOLD, 24));
				   retraitRefuseLbl.setSize(450, 45);
				   retraitRefuseLbl.setLocation(50, 50);
				   pageRetraitRefuse.add(retraitRefuseLbl);
				   refusSolde.setForeground(Color.RED);
				   refusSolde.setFont(new Font("Arial", Font.BOLD, 24));
				   refusSolde.setSize(300, 45);
				   refusSolde.setLocation(50, 100);
				   pageRetraitRefuse.add(refusSolde);
				   soldeRefusRetrait.setForeground(Color.RED);
				   soldeRefusRetrait.setFont(new Font("Arial", Font.BOLD, 24));
				   soldeRefusRetrait.setSize(100,45);
				   soldeRefusRetrait.setLocation(350, 100);
				   pageRetraitRefuse.add(soldeRefusRetrait);
				   seuilErreur.setForeground(Color.RED);
				   seuilErreur.setFont(new Font("Arial", Font.BOLD, 24));
				   seuilErreur.setSize(200, 45);
				   seuilErreur.setLocation(50, 150);
				   pageRetraitRefuse.add(seuilErreur);
				   seuilRestant.setForeground(Color.RED);
				   seuilRestant.setFont(new Font("Arial", Font.BOLD, 24));
				   seuilRestant.setSize(100,45);
				   seuilRestant.setLocation(250, 150);
				   pageRetraitRefuse.add(seuilRestant);
				   
				   	//bouton retour
				   JButton retourRetraitBtn = new JButton("RETOUR");
				   retourRetraitBtn.setBackground(couleurBoutonEcran);
				   retourRetraitBtn.setForeground(couleurTexte);
				   retourRetraitBtn.setFont(new Font("Arial", Font.BOLD, 12));
				   retourRetraitBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				   retourRetraitBtn.setFocusPainted(false);						//|
				   retourRetraitBtn.setMargin(new Insets(0,0,0,0));
				   retourRetraitBtn.addActionListener(e ->{
				    	cl.show(ecran, "MenuRetrait");
				    });
				   retourRetraitBtn.setSize(270, 35);
				   retourRetraitBtn.setLocation(0,240);				    
				   pageRetraitRefuse.add(retourRetraitBtn);
				   
					//bouton terminer
				   JButton terminerRetraitBtn = new JButton("TERMINER");
				   terminerRetraitBtn.setBackground(couleurBoutonEcran);
				   terminerRetraitBtn.setForeground(couleurTexte);
				   terminerRetraitBtn.setFont(new Font("Arial", Font.BOLD, 12));
				   terminerRetraitBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				   terminerRetraitBtn.setFocusPainted(false);						//|
				   terminerRetraitBtn.setMargin(new Insets(0,0,0,0));
				   terminerRetraitBtn.addActionListener(e ->{
				    	cl.show(ecran, "RetirerCarte");
				    });
				   terminerRetraitBtn.setSize(270, 35);
				   terminerRetraitBtn.setLocation(280,240);				    
				   pageRetraitRefuse.add(terminerRetraitBtn);
				   
				   /**
				    * @pageConsulter
				    */
				   //création de la page d'affichage de la consultation de code
				    JPanel pageConsulter = new JPanel();
				    pageConsulter.setBackground(couleurEcran);
				    pageConsulter.setLayout(null);
				    pageConsulter.setSize(ecran.getSize());
				    	//Texte indiquant le solde actuel
				    soldeLabel.setForeground(couleurTexte);
				    soldeLabel.setFont(new Font("Arial", Font.BOLD, 24));
				    soldeLabel.setSize(450, 45);
				    soldeLabel.setLocation(50, 50);
				    pageConsulter.add(soldeLabel);
				    soldeLbl.setForeground(couleurTexte);
				    soldeLbl.setFont(new Font("Arial", Font.BOLD, 30));
				    soldeLbl.setSize(100,100);
				    soldeLbl.setLocation(225, 75);
				    pageConsulter.add(soldeLbl);
				    	/*bouton retour*/
				    JButton retourBtn = new JButton("RETOUR");
				    retourBtn.setBackground(couleurBoutonEcran);
				    retourBtn.setForeground(couleurTexte);
				    retourBtn.setFont(new Font("Arial", Font.BOLD, 20));
				    retourBtn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
					retourBtn.setFocusPainted(false);			//|
					retourBtn.setMargin(new Insets(0,0,0,0));
				    retourBtn.setSize(tailleBtnMenuRetrait);
				    retourBtn.setLocation(175, 200);
				    retourBtn.addActionListener(e ->{cl.show(ecran, "Menu");});
				    pageConsulter.add(retourBtn);
				    /**
				     * @pageHistorique
				     */
				    //création de la page d'affiche de l'historique des transactions
				    JPanel pageHistorique = new JPanel();
				    pageHistorique.setBackground(couleurEcran);
				    pageHistorique.setLayout(null);
				    pageHistorique.setSize(ecran.getSize());
				    histoArea.setEditable(false);
				    histoArea.setBackground(couleurEcran);
				    histoArea.setForeground(couleurTexte);
				    histoArea.setFont(new Font("Arial", Font.BOLD, 11));
				    histoArea.setSize(500,150);
				    histoArea.setLocation(25, 75);
				    JScrollPane barreDefilementHisto = new JScrollPane(histoArea);
				    barreDefilementHisto.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				    pageHistorique.add(barreDefilementHisto);
				    pageHistorique.add(histoArea);
				    
				    
				    /**
				     * @pageDepot
				     */
				    
				    //création de la page d'affichage pour le depot
				    JPanel pageDepot = new JPanel();
				    pageDepot.setBackground(couleurEcran);
				    pageDepot.setLayout(null);
				    pageDepot.setSize(ecran.getSize());
				    JLabel titreDepot = new JLabel("Quel montant souhaitez-vous déposer ?", SwingConstants.CENTER);
				    titreDepot.setForeground(couleurTexte);
				    titreDepot.setFont(new Font("Arial", Font.BOLD, 18));
				    titreDepot.setBackground(couleurEcran);
				    titreDepot.setSize(400, 45);
				    titreDepot.setLocation(75, 50);
				    pageDepot.add(titreDepot);	    				    	
				    	//config de la zone de texte    
			        saisieDepot.setBorder(BorderFactory.createEmptyBorder());
			        saisieDepot.setEnabled(false);
			        saisieDepot.setBackground(couleurZoneSaisie);
			        saisieDepot.setForeground(couleurTexte);
			        saisieDepot.setFont(new Font("Arial", Font.BOLD, 34));
			        saisieDepot.setHorizontalAlignment(JTextField.CENTER);
			        saisieDepot.setSize(200, 50);
			        saisieDepot.setLocation(175, 100);					    
			        pageDepot.add(saisieDepot);
			     
				    	//bouton pour rajouter 10 euros au montant à déposer
				    JButton btn10Depot = new JButton("10");
				    btn10Depot.setBackground(couleurBoutonEcran);
				    btn10Depot.setForeground(couleurTexte);
				    btn10Depot.setFont(new Font("Arial", Font.BOLD, 24));
				    btn10Depot.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn10Depot.setFocusPainted(false);						//|
				    btn10Depot.setMargin(new Insets(0,0,0,0));
				    btn10Depot.addActionListener(e ->{
				    	if(saisieDepot.getText().equals(""))
				    		saisieDepot.setText("10");
				    	else {
				    		saisieDepot.setText(""+(Integer.parseInt(saisieDepot.getText())+10));
				    	}
				    });
				    btn10Depot.setSize(90, 35);
				    btn10Depot.setLocation(65,180);				    
				    pageDepot.add(btn10Depot);
				    	//bouton pour rajouter 20 euros au montant à déposer
				    JButton btn20Depot = new JButton("20");
				    btn20Depot.setBackground(couleurBoutonEcran);
				    btn20Depot.setForeground(couleurTexte);
				    btn20Depot.setFont(new Font("Arial", Font.BOLD, 24));
				    btn20Depot.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn20Depot.setFocusPainted(false);						//|
				    btn20Depot.setMargin(new Insets(0,0,0,0));
				    btn20Depot.addActionListener(e ->{
				    	if(saisieDepot.getText().equals(""))
				    		saisieDepot.setText("20");
				    	else {
				    		saisieDepot.setText(""+(Integer.parseInt(saisieDepot.getText())+20));
				    	}
				    });
				    btn20Depot.setSize(90, 35);
				    btn20Depot.setLocation(175,180);				    
				    pageDepot.add(btn20Depot);
				    	//bouton pour rajouter 50 euros au montant à déposer
				    JButton btn50Depot = new JButton("50");
				    btn50Depot.setBackground(couleurBoutonEcran);
				    btn50Depot.setForeground(couleurTexte);
				    btn50Depot.setFont(new Font("Arial", Font.BOLD, 24));
				    btn50Depot.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn50Depot.setFocusPainted(false);						//|
				    btn50Depot.setMargin(new Insets(0,0,0,0));
				    btn50Depot.addActionListener(e ->{
				    	if(saisieDepot.getText().equals(""))
				    		saisieDepot.setText("50");
				    	else {
				    		saisieDepot.setText(""+(Integer.parseInt(saisieDepot.getText())+50));
				    	}
				    });
				    btn50Depot.setSize(90, 35);
				    btn50Depot.setLocation(285,180);				    
				    pageDepot.add(btn50Depot);
				    	//bouton pour rajouter 100 euros au montant à déposer
				    JButton btn100Depot = new JButton("100");
				    btn100Depot.setBackground(couleurBoutonEcran);
				    btn100Depot.setForeground(couleurTexte);
				    btn100Depot.setFont(new Font("Arial", Font.BOLD, 24));
				    btn100Depot.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btn100Depot.setFocusPainted(false);						//|
				    btn100Depot.setMargin(new Insets(0,0,0,0));
				    btn100Depot.addActionListener(e ->{
				    	if(saisieDepot.getText().equals(""))
				    		saisieDepot.setText("100");
				    	else {
				    		saisieDepot.setText(""+(Integer.parseInt(saisieDepot.getText())+100));
				    	}
				    });
				    btn100Depot.setSize(90, 35);
				    btn100Depot.setLocation(385,180);				    
				    pageDepot.add(btn100Depot);
				     //bouton pour effacer la zone de saisie
				    JButton btnEffacerDepot = new JButton("EFFACER");
				    btnEffacerDepot.setBackground(Color.RED);
				    btnEffacerDepot.setForeground(couleurTexte);
				    btnEffacerDepot.setFont(new Font("Arial", Font.BOLD, 10));
				    btnEffacerDepot.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnEffacerDepot.setFocusPainted(false);						//|
				    btnEffacerDepot.setMargin(new Insets(0,0,0,0));
				    btnEffacerDepot.addActionListener(e ->{
				    	saisieDepot.setText("");
				    });
				    btnEffacerDepot.setSize(50, 40);
				    btnEffacerDepot.setLocation(380,105);				    
				    pageDepot.add(btnEffacerDepot);
				    	//bouton pour valider le montant à retirer
				    JButton btnValiderDepot = new JButton("VALIDER");
				    btnValiderDepot.setBackground(Color.GREEN);
				    btnValiderDepot.setForeground(couleurTexte);
				    btnValiderDepot.setFont(new Font("Arial", Font.BOLD, 10));
				    btnValiderDepot.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnValiderDepot.setFocusPainted(false);						//|
				    btnValiderDepot.setMargin(new Insets(0,0,0,0));
				    btnValiderDepot.addActionListener(e ->{
				    	cl.show(ecran, "Chargement");
				    	Thread thread = new Thread(() ->{
				    		central.depot(Integer.parseInt(saisieDepot.getText()));
					    	soldeDepot.setText("");
					    	soldeDepot.setText(soldeDepot.getText()+central.getInfoById());
					    	cl.show(ecran, "Depot");
				    	});
				    	thread.start();
				    });
				    	
				    btnValiderDepot.setSize(50, 40);
				    btnValiderDepot.setLocation(120,105);				    
				    pageDepot.add(btnValiderDepot);
				    	//bouton retour
				    JButton btnRetourDepot = new JButton("RETOUR");
				    btnRetourDepot.setBackground(couleurBoutonEcran);
				    btnRetourDepot.setForeground(couleurTexte);
				    btnRetourDepot.setFont(new Font("Arial", Font.BOLD, 12));
				    btnRetourDepot.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				    btnRetourDepot.setFocusPainted(false);						//|
				    btnRetourDepot.setMargin(new Insets(0,0,0,0));
				    btnRetourDepot.addActionListener(e ->{
				    	cl.show(ecran, "Menu");
				    });
				    btnRetourDepot.setSize(550, 35);
				    btnRetourDepot.setLocation(0,240);				    
				    pageDepot.add(btnRetourDepot);
			        	
			        //création de la page d'affichage du depot
					 JPanel pageDepotAccepte = new JPanel();
					 pageDepotAccepte.setBackground(couleurEcran);
					 pageDepotAccepte.setLayout(null);
					 pageDepotAccepte.setSize(ecran.getSize());
					 resultatDepot.setForeground(couleurTexte);
					 resultatDepot.setFont(new Font("Arial", Font.BOLD, 24));
					 resultatDepot.setSize(450, 45);
					 resultatDepot.setLocation(50, 50);
					 pageDepotAccepte.add(resultatDepot);
					 soldeDepot.setForeground(couleurTexte);
					 soldeDepot.setFont(new Font("Arial", Font.BOLD, 30));
					 soldeDepot.setSize(100,100);
					 soldeDepot.setLocation(225, 75);
					 pageDepotAccepte.add(soldeDepot);
						//bouton pour imprimer un reçu
					   JButton imprimerRecu2Btn = new JButton("RECU");
					   imprimerRecu2Btn.setBackground(couleurBoutonEcran);
					   imprimerRecu2Btn.setForeground(couleurTexte);
					   imprimerRecu2Btn.setFont(new Font("Arial", Font.BOLD, 12));
					   imprimerRecu2Btn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
					   imprimerRecu2Btn.setFocusPainted(false);						//|
					   imprimerRecu2Btn.setMargin(new Insets(0,0,0,0));
					   imprimerRecu2Btn.addActionListener(e ->{
						   	Ticket ticket = new Ticket(central.impressionRecu());
					    	cl.show(ecran, "RetirerCarte");
					    });
					   imprimerRecu2Btn.setSize(270, 35);
					   imprimerRecu2Btn.setLocation(0,240);				    
					   pageDepotAccepte.add(imprimerRecu2Btn);   
						//bouton pour terminer la transaction sans reçu
					   JButton sansRE2Btn = new JButton("PAS DE RECU");
					   sansRE2Btn.setBackground(couleurBoutonEcran);
					   sansRE2Btn.setForeground(couleurTexte);
					   sansRE2Btn.setFont(new Font("Arial", Font.BOLD, 12));
					   sansRE2Btn.setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
					   sansRE2Btn.setFocusPainted(false);						//|
					   sansRE2Btn.setMargin(new Insets(0,0,0,0));
					   sansRE2Btn.addActionListener(e ->{
					    	cl.show(ecran, "RetirerCarte");
					    });
					   sansRE2Btn.setSize(270, 35);
					   sansRE2Btn.setLocation(280,240);				    
					   pageDepotAccepte.add(sansRE2Btn);
					 
					  
					 				    
					 
					 
				    //ajout des différents affichages dans le panel ecran
				    ecran.add(bienvenu, affichages[0]);
				    ecran.add(menu, affichages[1]);
				    ecran.add(saisieCode, affichages[2]);
				    ecran.add(menuRetrait, affichages[3]);
				    ecran.add(saisieRetrait, affichages[4]);
				    ecran.add(retirerLaCarte,affichages[5]);
				    ecran.add(pageRetraitAccepte, affichages[6]);
				    ecran.add(pageConsulter, affichages[7]);
				    ecran.add(pageHistorique, affichages[8]);
				    ecran.add(pageDepot, affichages[9]);
				    ecran.add(pageDepotAccepte, affichages[10]);
				    ecran.add(pageRetraitRefuse, affichages[11]);
				    ecran.add(pageCarteBloquee, affichages[12]);
				    ecran.add(pageRib, affichages[13]);
				    ecran.add(echecPnl, affichages[14]);
				    ecran.add(pageChargement, affichages[15]);
				    			    
				    /**
				     * 
				     */
				    
				    //ajout du panel ecran dans le panel principal
				    panel.add(ecran);
				    
				    /**
				     * 
				     */
				    //creation et configuration du pave numérique (pour saisir le code)______________________________________________________
				    JButton[] numBoutons;
					numBoutons = new JButton[10];
					for (int i = 1; i <= 9; i++) {
				        numBoutons[i] = new JButton(String.valueOf(i));//bouton 'i' i représente le chiffre
				        numBoutons[i].setBackground(new Color(49, 49, 49)); //le bouton est de couleur vert foncé
				        numBoutons[i].setBorder(BorderFactory.createEmptyBorder());//enlève la bordure grise 
				        numBoutons[i].setFocusPainted(false);			//|
				        numBoutons[i].setMargin(new Insets(0,0,0,0));	//| enlevé la bordure du bouton sélectionné, source : "stackoverflow.com"  
				        numBoutons[i].setForeground(Color.BLACK);// le texte est en NOIR (en l'occurence les chiffres)
				        numBoutons[i].setFont(new Font("Arial", Font.BOLD, 36));
				        //on associe à chaque bouton créé l'action : saisir le chiffre associé au bouton dans la zone de saisie du code, source : "stackoverflow.com"
				        numBoutons[i].addActionListener(e ->{//
				        	for(int j=1;j<10;j++){
				        		if(e.getSource() == numBoutons[j]) {			        			
				        		   zoneCode.setText(zoneCode.getText() +String.valueOf(j));
				        		 }
				       		 }
				        });
				        //ajoute ensuite le bouton au pavé numérique
				        pavNum.add(numBoutons[i]);	
					}
					
					JButton boutonVierge1 = new JButton();//ajout d'un bouton, il n'enclenche aucune action
				    boutonVierge1.setBackground(new Color(49, 49, 49));
				    boutonVierge1.setForeground(Color.BLACK); 
				    boutonVierge1.setBorder(BorderFactory.createEmptyBorder());
				    boutonVierge1.setFocusPainted(false);			//|
				    boutonVierge1.setMargin(new Insets(0,0,0,0));	//| enlevé la bordure du bouton sélectionné, source : "stackoverflow.com"
				    pavNum.add(boutonVierge1);
					
					numBoutons[0] = new JButton("0");//ajout du bouton zero
					numBoutons[0].setBackground(new Color(49, 49, 49));
					numBoutons[0].setForeground(Color.BLACK);  
					numBoutons[0].setFont(new Font("Arial", Font.BOLD, 36));
				    numBoutons[0].setBorder(BorderFactory.createEmptyBorder());
				    numBoutons[0].setFocusPainted(false);
				    numBoutons[0].setMargin(new Insets(0,0,0,0));
				    numBoutons[0].addActionListener(e ->{
			        	for(int j=0;j<10;j++){
			        		if(e.getSource() == numBoutons[j]) {
			        			 zoneCode.setText(zoneCode.getText() +String.valueOf(j));			        			
			        		}
			        	}			        
				    });
				    pavNum.add(numBoutons[0]);
			        
				    JButton boutonVierge2 = new JButton();	       
				    boutonVierge2.setBackground(new Color(49, 49, 49));
				    boutonVierge2.setForeground(Color.BLACK); 
				    boutonVierge2.setBorder(BorderFactory.createEmptyBorder());
				    boutonVierge2.setFocusPainted(false);			//|
			        boutonVierge2.setMargin(new Insets(0,0,0,0));	//| enlevé la bordure du bouton sélectionné, source : "stackoverflow.com"  
			        pavNum.add(boutonVierge2);
				    
				    /* Résultat pavé numérique : 
				     * 		|1|2|3|
				     * 		|4|5|6|
				     * 		|7|8|9|
				     * 		| |0| |      */
				    
				    /**
				     * 
				     */
				    //pave numerique + autres boutons

			        //création et configuration des boutons : VALIDER, ANNULER, CORRIGER
			        
			      //bouton corriger
			        JButton boutonAnnuler = new JButton("ANNULER");
				    boutonAnnuler.setBackground(new Color(49, 49, 49));
				    boutonAnnuler.setForeground(Color.RED); //couleur rouge
				    boutonAnnuler.setFont(new Font("Arial", Font.BOLD, 24));
				    boutonAnnuler.setBorder(BorderFactory.createEmptyBorder());
				    boutonAnnuler.setFocusPainted(false);	
			        boutonAnnuler.setMargin(new Insets(0,0,0,0));
			        boutonAnnuler.addActionListener(e ->{
			        	cl.show(ecran, "RetirerCarte");
			        });
			        boutonAnnuler.setSize(150, 50 );
			        boutonAnnuler.setLocation(225, 75);
			        pave.add(boutonAnnuler);
			        //bouton corriger
			        JButton boutonCorriger = new JButton("CORRIGER");
				    boutonCorriger.setBackground(new Color(49, 49, 49));
				    boutonCorriger.setForeground(Color.YELLOW); //couleur verte
				    boutonCorriger.setFont(new Font("Arial", Font.BOLD, 24));
				    boutonCorriger.setBorder(BorderFactory.createEmptyBorder());
				    boutonCorriger.setFocusPainted(false);	
			        boutonCorriger.setMargin(new Insets(0,0,0,0));
			        boutonCorriger.addActionListener(e ->{
			        	zoneCode.setText("");
			        });
			        boutonCorriger.setSize(150, 50);
			        boutonCorriger.setLocation(225, 135);
			        pave.add(boutonCorriger);
			        pavNum.setLocation(5, 5);
			        pave.add(pavNum);
				    
				    JButton boutonValider = new JButton("VALIDER");
				    boutonValider.setBackground(new Color(49, 49, 49));
				    boutonValider.setForeground(Color.GREEN); //couleur verte
				    boutonValider.setFont(new Font("Arial", Font.BOLD, 24));
				    boutonValider.setBorder(BorderFactory.createEmptyBorder());
				    boutonValider.setFocusPainted(false);	
				    boutonValider.addActionListener(e ->{
				    	String code = new String(zoneCode.getPassword());
				    	String codeHash = null;
				    	try {
				    		codeHash = Securite.sha256WithKey(Securite.SHA256(Securite.MD5(code)), key);
				    	}catch (NoSuchAlgorithmException a) {
				    		
				    	}
				    	try {
							if(!Securite.verifierCodeSecret(codeHash, Securite.sha256WithKey(central.getMdpById(), key)) && tentative < 3){
							    tentative++;
								saisieCode.add(erreurCodeLbl);
								saisieCode.add(tentativeLbl);
								nbTentativeLbl.setText(""+(3 - tentative +1));
								saisieCode.add(nbTentativeLbl);
								saisieCode.revalidate();
								saisieCode.repaint();
								zoneCode.setText("");
								cl.show(ecran, "SaisieCode");
							}
							else{
								try {
									if(tentative==3 && !Securite.verifierCodeSecret(codeHash, Securite.sha256WithKey(central.getMdpById(), key))){
										central.mettreAJourBlock();
										cl.show(ecran, "CarteBloquee");										
									}
									
									else {//code saisie est correct 
										//on reset le nb de tentatives
										tentative = 0;
										//on accède au menu
										cl.show(ecran, "Menu");
										//on desactive les boutons annuler, corriger, valider
										boutonAnnuler.setEnabled(false);
										boutonAnnuler.revalidate();
										boutonAnnuler.repaint();
										boutonCorriger.setEnabled(false);
										boutonCorriger.revalidate();
										boutonCorriger.repaint();
										boutonValider.setEnabled(false);
										boutonValider.revalidate();
										boutonValider.repaint();
									}
								} catch (NoSuchAlgorithmException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						} catch (NoSuchAlgorithmException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				    });
			        boutonValider.setMargin(new Insets(0,0,0,0));
			        boutonValider.setSize(150, 50);
			        boutonValider.setLocation(225, 15);
			        pave.add(boutonValider);
			        
			        //_________________________________________________________________________________________________________________________________________
			        
			     
				    //ajout du grand pave de boutons dans le panel
				    panel.add(pave);
				   		    
			        //rendre la fenêtre visible sur l'écran de l'ordinateur
				    fenetre.setVisible(true);
				    
				  //__________________________ FIN de la création de l'UI______________________________________________________
				    
				    while(!quitterBtn.getModel().isPressed()) {
					    try {
					    // Récupérer le premier lecteur de carte disponible
					    TerminalFactory terminalFactory = TerminalFactory.getDefault();
				        List<CardTerminal> terminals = terminalFactory.terminals().list();
				        if (terminals.isEmpty()) {
				        throw new CardException("Aucun lecteur de carte disponible");
		            	}
				      	CardTerminal terminal = terminals.get(0);
				        System.out.println("Lecteur de carte sélectionné: " + terminal.getName());			    
				        //Attendre qu'une carte soit insérée
				        terminal.waitForCardPresent(0);			        				        
				        //aller au menu
				        //cl.show(ecran, "Menu"); 
				        // Se connecter à la carte				            
				        Card card = terminal.connect("*");
				        System.out.println("Carte connectée: " + card);
				        //obtenir le signal de la carte
				        int id =  card.getATR().hashCode(); 
				        //envoyer le signal de la carte à la bdd;
				        central.setID(id);
				        System.out.println("id : " + id);
				        //on réinitialise la page de saisie du code pour chaque nouvelle session
				        tentative = 1;
				        saisieCode.remove(nbTentativeLbl);
				        saisieCode.remove(erreurCodeLbl);
				        saisieCode.remove(tentativeLbl);
				        zoneCode.setText("");
				        saisieCode.revalidate();
				        saisieCode.repaint();
				        
				        //on réactive les boutons valider, annuler, corriger qui ont été désactiver lors de la dernière session
				       boutonAnnuler.setEnabled(true);
						boutonAnnuler.revalidate();
						boutonAnnuler.repaint();
						boutonCorriger.setEnabled(true);
						boutonCorriger.revalidate();
						boutonCorriger.repaint();
						boutonValider.setEnabled(true);
						boutonValider.revalidate();
						boutonValider.repaint();
				        
				        //on réinitialise la zone de saisie du depot et du retrait
				        saisieDepot.setText("");
				        zoneSaisieRetrait.setText("");
				       
				        if(central.isNumberExists()) {	 //si la carte appartient à la banque
				        	//on rajoute les options imprimer le rib et consultation de l'historique de transaction
				        	menu.add(impressionBtn);
				        	menu.add(historisqueBtn);
				        	menu.revalidate();
				        	menu.repaint();
				        	
				        	//afficher la page de saisie du code secret
				        	cl.show(ecran, "SaisieCode");
				       }
				       else if(central.isNumberExistsOutsideOfMyBank()) {//si la carte est enregistré dans les cartes externes à la banque
				    	   //on procède à l'authentification
				    	   cl.show(ecran,  "SaisieCode");
				    	   menu.remove(impressionBtn);
				    	   menu.remove(historisqueBtn);
				    	   menu.revalidate();
				    	   menu.repaint();
				       }
				       else
				    	   cl.show(ecran, "CarteNonReconnue");
				        		
				      //Attendre qu'une carte soit retirée
		            	terminal.waitForCardAbsent(0);
		            	//revenir à l'écran de démarrage
		            	cl.show(ecran, "Bienvenu");
		            	//Se déconnecter de la carte
		            	card.disconnect(false);
				
		            	System.out.println("Carte déconnectée");
			    	} catch (CardException e) {
			    		System.err.println("Erreur lors de la communication avec la carte: " + e.getMessage());
			    	}
			            	
		}
					    
	}
}
	


