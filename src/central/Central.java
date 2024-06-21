package central;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class Central {
	
	private String url; //url pour se connecter à la base
	private String user; //nom de l'admin ayant tout les droits à la bdd
	private String mdp; //mdp d'acces à la bdd

	private static int numeroOperation; // numero de transaction
	
	private int id ;
	
	
	
	
	

	public Central(String url, String user, String mdp) {
		// TODO Auto-generated constructor stub
		this.url = url;
		this.user = user;
		this.mdp = mdp;
		
		//pour incrementer le num d'opération à chaque fois
		String maChaine =executeQueryForString("select count(*) from transaction");
		if (maChaine.equals("0")) {
			numeroOperation=0;
			
		}
		
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	//***********************************************************************************
	public String getMdpById() {
		String info = null;
        // Connexion à la base de données
        try (Connection conn = DriverManager.getConnection(url,user,mdp);
             Statement stmt = conn.createStatement(); ){
        	//ma requete sql
             ResultSet rs = null; 
        	if(isNumberExists()) {
        		rs = stmt.executeQuery("SELECT mdp FROM compte WHERE numero_id = " + id);
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		rs = stmt.executeQuery("SELECT mdp FROM compte_externe WHERE id = " + id);
        	}

        	if (!rs.next()) {
       
            }
            // Récupération du mdp associé à l'identifiant
            info = rs.getString("mdp");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return info;
    }
	
	//***********************************************************************************methode pour modifier l'etat de la carte 
	public void mettreAJourBlock() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(url,user, mdp);
            String sql = null;
            if(isNumberExists()) {
            	sql = "UPDATE compte SET block = 'bloquée' WHERE numero_id = ?";
            }else if(isNumberExistsOutsideOfMyBank()) {
            	sql = "UPDATE compte_externe SET block = 'bloquée' WHERE numero_id = ?";
            }
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
	
	//*********************************************************************************** methode qui check si l'id existe dans la base
	public  boolean isNumberExists() throws SQLException {
	    boolean exists = false;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    
	    Connection conn = DriverManager.getConnection(url,user,mdp);
	    try {
	      // Préparer la requête SQL 
	      String sql = "SELECT * FROM compte  WHERE  numero_id  = ? and block= ? ";
	      stmt = conn.prepareStatement(sql);
	      stmt.setInt(1, id);	      
	      stmt.setString(2, "ok");
	      // Exécuter la requête SQL et récupérer le résultat
	      rs = stmt.executeQuery();
	      
	      // Vérifier si un enregistrement a été trouvé
	      if (rs.next()) {
	        exists = true;
	      }
	      
	    } finally {
	      // Fermer les ressources JDBC
	      if (rs != null) rs.close();
	      if (stmt != null) stmt.close();
	    }
	    return exists;
	  }
	//***********************************************************************************methode pour verifier si le num existe dans une autre banque 
	public  boolean isNumberExistsOutsideOfMyBank() throws SQLException {
	    boolean exists = false;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    
	    Connection conn = DriverManager.getConnection(url,user,mdp);
	    try {
	      // Préparer la requête SQL 
	      String sql = "SELECT * FROM compte_externe  WHERE  id  = ? and block= ? ";
	      stmt = conn.prepareStatement(sql);
	      stmt.setInt(1, id);	      
	      stmt.setString(2, "ok");
	      // Exécuter la requête SQL et récupérer le résultat
	      rs = stmt.executeQuery();
	      
	      // Vérifier si un enregistrement a été trouvé
	      if (rs.next()) {
	        exists = true;
	      }
	      
	    } finally {
	      // Fermer les ressources JDBC
	      if (rs != null) rs.close();
	      if (stmt != null) stmt.close();
	    }
	    return exists;
	  }
	
	//*********************************************************************************** methode qui retourne le solde en entrant l'id
	public int getInfoById() {
		int info = 0 ;
        // Connexion à la base de données
        try (Connection conn = DriverManager.getConnection(url,user,mdp);
             Statement stmt = conn.createStatement();){
             ResultSet rs = null;
        	if(isNumberExists()) {
        		rs = stmt.executeQuery("SELECT solde FROM compte WHERE numero_id = " + id);
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		rs = stmt.executeQuery("SELECT solde FROM compte_externe WHERE id = " + id);
        	}
        		
            // Vérification s'il ya une information à retourner
            if (!rs.next()) {
               
            }

            // Récupération de l'information associée à l'identifiant
            info = rs.getInt("solde");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;  // solde 
    }
	//*********************************************************************************** connexion à la base
	
	public void connexionBase() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection(url,user,mdp);
			
		} catch (Exception e) {
			
		}

	}
	//***************************************************************** methode qui mets a jour le solde dans la base de données
	public void mettreAJour(int nouveauSolde) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(url,user, mdp);
            String sql = null;
            if(isNumberExists()) {
            	sql = "UPDATE compte SET solde = ? WHERE numero_id = ?";
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		sql = "UPDATE compte_externe SET solde = ? WHERE id = ?";
        	}
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, nouveauSolde);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
	// **********************************************************methode pour recuperer la date et convertir en string
	public static String getDateAsString() {
        LocalDate currentDate = LocalDate.now();
        String currentDateAsString = currentDate.toString();
        return currentDateAsString;
    }
	// **********************************************************methode pour recuperer l'heure et convertir en string
	public static String getHourAsString() {
		LocalTime heure = LocalTime.now();
		DateTimeFormatter formatSouhaite = DateTimeFormatter.ofPattern("HH:mm"); //set le format
		String heureFormattee = heure.format(formatSouhaite);
        return heureFormattee;
    }
	
	// ********************************************************** methode pour afficher le recu apres une certaine transaction
	
	public String impressionRecu() { 
		StringBuffer chaine = new StringBuffer("");
	    //ResultSet rs = null; //à supprimer ?

	try (Connection connexion = DriverManager.getConnection(url, user, mdp)) {
        String requete = "SELECT * FROM transaction WHERE num_compte= ?  ORDER BY num_op DESC LIMIT 1";
        PreparedStatement stmt = connexion.prepareStatement(requete);
        stmt.setInt(1, id);
        ResultSet resultat = stmt.executeQuery();
	      
	      // Exécuter la requête SQL et récupérer les résultats
        if (resultat.next()) {
        	
            int colonne1 = resultat.getInt("num_op");
            String colonne2 = resultat.getString("type_transaction");
            int  colonne3 = resultat.getInt("montant");
            String colonne4 = resultat.getString("date");
            String colonne5 = resultat.getString("heure");
            int  colonne6 = resultat.getInt("num_compte");
            
	
            // remplir ma chaine 
            chaine.append("\tbanque et assurance de Paris Cité \nnumero de l'operation : " + colonne1+"\ndate : " + colonne4+"\nheure : " + colonne5+"\nnumero de compte : "
            + colonne6 + "\ntype de transaction : "+ colonne2+ "\nmontant saisi : "  + colonne3);
            
        } 
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
	return chaine.toString();
	
	}
	//**********************************************************************impression du rib 
	public String impressionRib() {
        // Connexion à la base de données
		String info = null;
        try (Connection conn = DriverManager.getConnection(url,user,mdp);
             Statement stmt = conn.createStatement(); ){
        	//ma requete sql
             ResultSet rs = null; 
        	if(isNumberExists()) {
            	rs = stmt.executeQuery("SELECT rib FROM compte WHERE numero_id = " + id);
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		rs = stmt.executeQuery("SELECT rib FROM compte_externe WHERE id = " + id);
        	}

        	
            // Récupération du rib associé à l'identifiant
        	if (rs.next()) {
        		info = rs.getString("rib");
        	}
        	
        } catch (SQLException e) {
            e.printStackTrace();
            
        }
        return info;
    }
	
	//********************************************************************** pour donner sa valeur à num_op dans la table transaction
	public int getNumOp() {
		int numOp = 0;
        // Connexion à la base de données
        try (Connection conn = DriverManager.getConnection(url,user,mdp);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT num_op FROM transaction order by num_op desc limit 1")) {
        	if (!rs.next()) {
               
            }
            numOp = rs.getInt("num_op");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numOp;
       
    }
	//***********************************************************methode pour recuperer le seuil
	public int getSeuilById() {
		int seuilDuCompte = 0 ;
        // Connexion à la base de données
        try (Connection conn = DriverManager.getConnection(url,user,mdp);
            Statement stmt = conn.createStatement();){
        	ResultSet rs= null;
        	if(isNumberExists()) {
        		rs = stmt.executeQuery("SELECT seuil FROM compte WHERE numero_id = " + id);
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		rs = stmt.executeQuery("SELECT seuil FROM compte_externe WHERE id = " + id);
        	}

            // Vérification s'il ya une information à retourner
            if (!rs.next()) {
                
            }

            // Récupération du seuil associé à l'identifiant
            seuilDuCompte = rs.getInt("seuil");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seuilDuCompte;
        
       
    }
	//***********************************************************methode pour recuperer le seuil
	public int getSeuilRestantById() {
		int seuilRestant = 0 ;
        // Connexion à la base de données
        try (Connection conn = DriverManager.getConnection(url,user,mdp);
             Statement stmt = conn.createStatement();){
             ResultSet rs =  null;

        	if(isNumberExists()) {
        		rs =  stmt.executeQuery("SELECT seuil_restant FROM compte WHERE numero_id = " + id);
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		rs =  stmt.executeQuery("SELECT seuil_restant FROM compte_externe WHERE id = " + id);
        	}
            // Vérification s'il ya une information à retourner
            if (!rs.next()) {
            
            }

            // Récupération de l'information associée à l'identifiant
            seuilRestant = rs.getInt("seuil_restant");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seuilRestant;
        
       
    }
	//***********************************************************methode pour reactualiser le seuil restant 
	public void actualiserLeSeuilRestant(int nouveauSeuil) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(url,user, mdp);
            String sql = null;
            if(isNumberExists()) {
            	sql = "UPDATE compte SET seuil_restant = ? WHERE numero_id = ?";
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		sql = "UPDATE compte_externe SET seuil_restant = ? WHERE id = ?";
        	}
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, nouveauSeuil);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
	
	//***********************************************************methode pour reinitialiser le seuil restant 
	public void reinitialiserLeSeuilRestant() {
        Connection conn = null;
        PreparedStatement stmt = null;
        int seuil;
        seuil = getSeuilById();
        try {
            conn = DriverManager.getConnection(url,user, mdp);
            String sql = null ;
            if(isNumberExists()) {
            	sql = "UPDATE compte SET seuil_restant = ? WHERE numero_id = ?";
        	}else if(isNumberExistsOutsideOfMyBank()) {
        		sql = "UPDATE compte_externe SET seuil_restant = ? WHERE id = ?";
        	}
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, seuil);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
	//***********************************************************methode pour reinitialiser le seuil chaque minuit
	public void scheduleMidnightMethod() {
        Timer timer = new Timer();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        
        // Si l'heure actuelle est déjà passée minuit, on planifie la première exécution pour minuit de la nuit suivante
        if (midnight.getTimeInMillis() < System.currentTimeMillis()) {
            midnight.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            	reinitialiserLeSeuilRestant();
            }
        }, midnight.getTime(), 24 * 60 * 60 * 1000);
    }
	
	
	// **********************************************************methode de retrait
	public boolean retrait(int montant) {
		boolean resultat = false; //resultat à retourner
		int soldeCpt; //solde du compte
		soldeCpt =getInfoById(); //recuperer le solde de la bdd
		int soldeInitial; 
		soldeInitial=soldeCpt; //enregister l'ancien solde avant d'effectuer le retrait
		int seuilRestant; //max à retirer
		seuilRestant=getSeuilRestantById(); //recuperer le montant max à retirer
		int nouveauSeuilRestant;
		
		if (montant>seuilRestant) {
			resultat = false;
		}else {
		if (soldeCpt >= montant & montant <= seuilRestant) { 
			soldeCpt = soldeCpt - montant;
			
			int numOp= getNumOp();
			numOp=numOp +1;
			mettreAJour(soldeCpt);
			remplirHistoriqueDesTransactions(numOp, "retrait", montant,getDateAsString(),getHourAsString(), id);
			nouveauSeuilRestant = seuilRestant-montant;
			actualiserLeSeuilRestant(nouveauSeuilRestant);
			resultat= true;
			
		} else {
			resultat = false;
			
		}}
		return resultat;
	}
// *******************************************************************methode de depot
	
	public void depot(int montant) {
		int numOp= getNumOp();
		numOp=numOp +1;
		int soldeCpt;
		soldeCpt =getInfoById();
		int soldeInitial;
		soldeInitial=soldeCpt;
		soldeCpt = soldeCpt + montant;
		mettreAJour(soldeCpt);
		remplirHistoriqueDesTransactions(numOp, "dépot", montant,getDateAsString(),getHourAsString(), id);
	}

	//***************************************************************************methode pour remplir la table transaction
	public void remplirHistoriqueDesTransactions(int numOp,String typeTransaction,int montant,String date,String heure,int numCompte) {
		Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(url,user, mdp);
            String sql = "INSERT INTO transaction (num_op, type_transaction, montant,date,heure, num_compte) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, numOp);
            stmt.setString(2, typeTransaction);
            stmt.setInt(3, montant);
            stmt.setString(4, date);
            stmt.setString(5, heure);
            stmt.setInt(6, numCompte);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}

	
	//***************************************************************************methode pour afficher l'historique des transactions 
	
	public String afficherHistorique() {
		StringBuffer chaine = new StringBuffer("");
        try (Connection conn = DriverManager.getConnection(url,user,mdp);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transaction WHERE num_compte = " + id)) {

            // Vérification s'il y'a bien un historique
            if (!rs.next()) {
      
            }
            ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			
			
			while (rs.next()) {
				chaine.append("Numéro : "+rs.getInt("num_op")+ " Opération : "+ rs.getString("type_transaction")+"  Montant : " + rs.getInt("montant")+ "  Heure : " + rs.getString("heure") + "   Date : "+ rs.getString("date") +"\n");
			}
			
			} catch (SQLException e) {
		}
		
		return chaine.toString();
	}

	//***************************************************************************methode qui execute les requetes select
	public  void executeQuery(String query) {
		try {
			// Connexion a la bdd
			Connection conn = DriverManager.getConnection(url,user, mdp);

			// Create a statement object
			Statement stmt = conn.createStatement();

			// Execution de la requete et recuperation des resultats
			ResultSet rs = stmt.executeQuery(query);

			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			// afficher le resultat
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					System.out.print(rs.getString(i) + "\t\t");
				}
				System.out.println();
			}
			// fermer la connexion
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Error executing query: " + e.getMessage());
		}
	}
	//********************************************************************************* méthode spécial num op à ne pas toucher !
	public  String executeQueryForString(String query) {
		String result=null;
		try {
			// connexion
			Connection conn = DriverManager.getConnection(url,user, mdp);

			// stmt
			Statement stmt = conn.createStatement();

			// Execution de la requete 
			ResultSet rs = stmt.executeQuery(query);

			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			
			// resultats
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					result = rs.getString(i);
				}
				System.out.println();
				
			}
			// fermer la connexion
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Erreur d'execution de la requete : " + e.getMessage());
		}
		return result;
		
	}

	
}