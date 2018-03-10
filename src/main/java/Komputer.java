

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Komputer {
	
	// tutaj definiujemy adres logowania się do bazy danych, 
	protected final String dbServer = "jdbc:mysql://000.000.0.000/XXXYYY";
	private String sql = null; //zmmienna przetrzymująca zapytania SQL
	private String nazwaKomputera = "pc.name";
	private String nazwaUzytkownika = "brak";
	private String os = "os.name";
	private String ip = "000.000.000.000";
	private String cpu = null;
	private String gpu = null;
	private String ram = null;
	private String plytaGlowna = null;
	private String dysk = null;

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	LocalDateTime now = LocalDateTime.now();
	String dataUruchomienia = (dtf.format(now).toString()); // 2016/11/16 12:08:43

	public void pobierzDane() {
		try {

			// pobiera nazwę użytkownika i host;
			InetAddress localHost = InetAddress.getLocalHost(); //
			this.nazwaKomputera = localHost.getHostName().toString();
			this.nazwaUzytkownika = System.getProperty("user.name");
			this.ip = localHost.getHostAddress();
			this.os = System.getProperty(this.os);

		} catch (UnknownHostException ex) {
			System.out.println("Error" + ex);
		}
	}

	public void spiszKomputer() {

		
			// łączymy się z bazą danych dbServer definiujemy powyżej. 
		try (Connection conn = DriverManager.getConnection(dbServer)) {

			Statement stmt = conn.createStatement();
			// tablica wynikowa wyświetla wszystkie wyniki gdzie komórka "komputer" zawiera nazwe komputera
			sql = String.format("SELECT * FROM lista_komputerow WHERE komputer = \'%s\'", getNazwaKomputera()); 
			ResultSet rs = stmt.executeQuery(sql);

			// Jeżeli istnieje wpis w bazie danych o nazwie komputera, poprawia wpis. o nowe pobrane dane 
			if (rs.next() == true) {
				

				sql = String.format(
						"UPDATE lista_komputerow set komputer = \'%s\', uzytkownik = \'%s\', data = \'%s\' WHERE komputer = \'%s\' ",
						getNazwaKomputera(), getNazwaUzytkownika(), dataUruchomienia, getNazwaKomputera());
				stmt.execute(sql);
				System.out.println("UPDATE");
			} 
			// jeżeli tablica wynikowa nie zwróci żadnego wyniku towerzy nowy wpis gdzie podaje nazwe komputera, nazwę użytkownika i date dodania 
			else {

				sql = String.format(
						"INSERT INTO lista_komputerow (komputer, uzytkownik, data) VALUES (\'%s\', \'%s\', \'%s\')",
						getNazwaKomputera(), getNazwaUzytkownika(), dataUruchomienia);
				stmt.execute(sql);
				System.out.println("Doda�em");

			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}

	}

	public String getNazwaKomputera() {
		return nazwaKomputera;
	}

	public String getNazwaUzytkownika() {
		return nazwaUzytkownika;
	}

	public void generujXMLSprzetu() {
		
		/*
		 * Tutaj trochę magi, metoda ta odwołuje się do wiersza poleceń w systemie Windows, tworzy XMLa na podstawie 
		 * danych z DirectX
		 */
		
		//Definujemy miejsce zapisu XML'a w tym przypadku zapisujemy bezpośrednio w Tempie - nie chcemy bardzo zaśmiecać kompa 
		String pathXML = String.format("C:\\Users\\%s\\AppData\\Local\\Temp\\inwentaryzacja.xml",
				getNazwaUzytkownika()); 
		
		// dofiniujemy co ma zostać wspinae w CMD czyli komedna "dxdiag /x" - generuje plik XML z DirectX o scieżka powyżej
		String cmd = "dxdiag /x " + pathXML; 
		// Definiujemy plik XML i gdzie sie znajduje
		File plikXML = new File(pathXML);

		System.out.println(cmd);
		System.out.println(pathXML);
		
		// Jeżeli plik XML nie jestenieje to wykonuje instrukcje
		if (!plikXML.exists()) {
			try {

				
				//JVM uruchamia proces tworzenie XML poprzez CMD 
				Process process = Runtime.getRuntime().exec(cmd);
				
				//pętla sprawdza czy plikXML istnieje, jeżeli plik się pojawi w tempie. to zmienia czekajNaPlik = false i zamyka działąnie pętli.
				Boolean czekajNaPlik = true;
				System.out.println("Tworze plik XML...");
				while (czekajNaPlik) {

					if (plikXML.exists()) {
						czekajNaPlik = false;
						System.out.println(czekajNaPlik);
					}
					
				}
				System.out.println("Koniec tworzenia pliku XML");
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			
			//jeżęli plik istnieje instrukcja się zamyka
			System.out.println("plik istnieje");
		}
	}

	public void pobierzSprzet() {

		
		/*
		 * Tutaj przechodzimy do czytania XMLi 
		 * 
		 * 
		 */
		String path = String.format("C:\\Users\\%s\\AppData\\Local\\Temp\\inwentaryzacja.xml",
				getNazwaUzytkownika()); 
		DocumentBuilderFactory dbf; // budujemy obiekt poprzez DocumentBuilderFactory
		DocumentBuilder db; // następnie obiekt dbf przerabiamy na db
		Document d; // nastepnie db prasujemy plik na document
		XPath xp;
		NodeList nl;
		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			d = db.parse(path);
			xp = XPathFactory.newInstance().newXPath();
			
			//Pobiera pamięć ram 
			String expression = "//Memory";
			nl = (NodeList) xp.compile(expression).evaluate(d, XPathConstants.NODESET);
			String memory = nl.item(0).getTextContent();
			System.out.println(memory);
			
			//Pobiera pamięć procesor 
			expression = "//Processor";
			nl = (NodeList) xp.compile(expression).evaluate(d, XPathConstants.NODESET);
			String processor = nl.item(0).getTextContent();
			System.out.println(processor);
			
			//Pobiera OS 
			expression = "//OperatingSystem";
			nl = (NodeList) xp.compile(expression).evaluate(d, XPathConstants.NODESET);
			String os = nl.item(0).getTextContent();
			System.out.println(os);
			
			
			//Pobiera Karte graficzną 
			expression = "//CardName";
			nl = (NodeList) xp.compile(expression).evaluate(d, XPathConstants.NODESET);
			String firstGPU = nl.item(0).getTextContent();
			String secondGPU = null;
			
			/*
			 * iteruje po NodeList i sprawdza czy jest wiecej wyników, potrzebne przy komputerach z dwoma grafikami 
			 * jak np. Intel HD + nVidia itp.
			 */
		
			if(!nl.item(1).equals(null)) {
				secondGPU = nl.item(1).getTextContent();
			}
			System.out.println(firstGPU);
			System.out.println(secondGPU);
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
		
		
	}



}
