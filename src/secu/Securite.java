package secu;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class Securite {
	//méthode pour vérifier si le code entrée est égal au code associé à la carte
	public static boolean verifierCodeSecret(String code, String codeCorrect) {
	    return code.equals(codeCorrect);
	}
	
	//méthode pour crypter le code avec le MD5
	public static String MD5(String codeSecret) {
	    try {
	      MessageDigest md = MessageDigest.getInstance("MD5");
	      byte[] hash = md.digest(codeSecret.getBytes());
	      StringBuilder sb = new StringBuilder();
	      for (byte b : hash) {
	        sb.append(String.format("%02x", b));
	      }
	      return sb.toString();
	    } catch (NoSuchAlgorithmException e) {
	      System.err.println("ERREUR : L'algorithme MD5 n'est pas disponible.");
	      e.printStackTrace();
	      return null;
	    }
	  }
	//méthode pour crypter le code avec le SHA-256
	  public static String SHA256(String codeSecret) {
	    try {
	      MessageDigest md = MessageDigest.getInstance("SHA-256");
	      byte[] hash = md.digest(codeSecret.getBytes());
	      StringBuilder sb = new StringBuilder();
	      for (byte b : hash) {
	        sb.append(String.format("%02x", b));
	      }
	      return sb.toString();
	    } catch (NoSuchAlgorithmException e) {
	      System.err.println("L'algorithme SHA-256 n'est pas disponible.");
	      e.printStackTrace();
	      return null;
	    }
	  }
	  
	  //méthode pour crypter le code + la date du jour(key) avec le SHA-256
	  public static String sha256WithKey(String codeSecret, String key) throws NoSuchAlgorithmException {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        String donnée = codeSecret + key;
	        byte[] hash = md.digest(donnée.getBytes());
	        StringBuilder sb = new StringBuilder();	  	
	        for(byte b: hash){
	        	sb.append(String.format("%02x", b));
	        }
	        return sb.toString();
	  }
}  