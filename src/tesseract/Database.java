
package tesseract;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.PreparedStatement;

public class Database {
	public static void saveRecordToDb(String isletmeAdi, String tarih, String fisNo, String topkdv, String toplam,
			ArrayList<String> products, ArrayList<String> productTax, ArrayList<String> productPrice) {

		// System.out.println("product.get(0) : " + products.get(0));

		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = DriverManager.getConnection(PublicArea.connectionString, PublicArea.dbUserName,
					PublicArea.dbPassword);

			Statement stmt = con.createStatement();

			// the mysql insert statement
			String query = "insert into fisler (isletmeAdi, tarih, fisNo, topkdv, toplam) values (?, ?, ?, ?, ?)";

			PreparedStatement preparedStmt = (PreparedStatement) con.prepareStatement(query);
			preparedStmt.setString(1, isletmeAdi);
			preparedStmt.setString(2, tarih);
			preparedStmt.setString(3, fisNo);
			preparedStmt.setString(4, topkdv);
			preparedStmt.setString(5, toplam);

			preparedStmt.execute();

			query = "select id from fisler order by id DESC limit 1";
			ResultSet result = stmt.executeQuery(query);

			int fisId = -1;
			if (result.next()) {
				// System.out.println("gelen deger : " + result.getString(1).trim());
				fisId = Integer.parseInt(result.getString(1).trim());
			}

			for (int i = 0; i < products.size(); i++) {
				query = "insert into urunler (urunAdi, fisId, urunKdv, urunFiyat) values (?, ?, ?, ?)";
				preparedStmt = (PreparedStatement) con.prepareStatement(query);
				preparedStmt.setString(1, products.get(i));
				preparedStmt.setInt(2, fisId);
				preparedStmt.setString(3, productTax.get(i));
				preparedStmt.setString(4, productPrice.get(i));
				preparedStmt.execute();
			}

			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static String[][] getRecords(String condition) {
		ArrayList<Fis> fisler = new ArrayList<Fis>();
		String[][] fisBilgileri = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = DriverManager.getConnection(PublicArea.connectionString, PublicArea.dbUserName,
					PublicArea.dbPassword);

			Statement stmt = con.createStatement();

			// the mysql insert statement
			String query = "select id, isletmeAdi, fisNo, tarih, topkdv, toplam from fisler "+condition+"";

			ResultSet result = stmt.executeQuery(query);

			while (result.next()) {
				Fis f = new Fis(result.getString(1), result.getString(2), result.getString(3), result.getString(4),
						result.getString(5),
						result.getString(6));
				fisler.add(f);
				// System.out.println("result id : " + f.isletmeAdi);
			}

			fisBilgileri = new String[fisler.size()][6];

			for (int i = 0; i < fisBilgileri.length; i++) {
				fisBilgileri[i][0] = fisler.get(i).id;
				fisBilgileri[i][1] = fisler.get(i).isletmeAdi;
				fisBilgileri[i][2] = fisler.get(i).tarih;
				fisBilgileri[i][3] = fisler.get(i).fisNo;
				fisBilgileri[i][4] = fisler.get(i).topkdv;
				fisBilgileri[i][5] = fisler.get(i).toplam;
			}

			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return fisBilgileri;

	}

	public static String[][] getProducts(String id) {
		ArrayList<Urun> urunler = new ArrayList<Urun>();
		String[][] urunListesi = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = DriverManager.getConnection(PublicArea.connectionString, PublicArea.dbUserName,
					PublicArea.dbPassword);

			Statement stmt = con.createStatement();

			String query = "select urunAdi, urunKdv, urunFiyat from urunler where fisId like " + id + "";
			ResultSet result = stmt.executeQuery(query);

			while (result.next()) {
				Urun u = new Urun(result.getString(1), result.getString(2), result.getString(3));
				urunler.add(u);
			}

			urunListesi = new String[urunler.size()][3];
			for (int i = 0; i < urunler.size(); i++) {
				urunListesi[i][0] = urunler.get(i).urunAdi;
				urunListesi[i][1] = urunler.get(i).urunKdv;
				urunListesi[i][2] = urunler.get(i).urunFiyat;
			}

			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return urunListesi;
	}

	public static String[] getCompanyNames() {
		ArrayList<String> companyNames = new ArrayList<String>();

		String[] returnCompanyNames = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = DriverManager.getConnection(PublicArea.connectionString, PublicArea.dbUserName,
					PublicArea.dbPassword);

			Statement stmt = con.createStatement();

			String query = "select distinct isletmeAdi from fisler";
			ResultSet result = stmt.executeQuery(query);

			while (result.next()) {
				companyNames.add(result.getString(1));
			}

			returnCompanyNames = new String[companyNames.size()];

			for (int i = 0; i < companyNames.size(); i++) {
				returnCompanyNames[i] = companyNames.get(i);
			}

			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return returnCompanyNames;
	}

	public static String[][] getSpecificRecords(String var, String value) {
		ArrayList<Fis> fisler = new ArrayList<Fis>();
		String[][] fisBilgileri = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = DriverManager.getConnection(PublicArea.connectionString, PublicArea.dbUserName,
					PublicArea.dbPassword);

			Statement stmt = con.createStatement();

			// the mysql insert statement
			String query = "select id, isletmeAdi, fisNo, tarih, topkdv, toplam from fisler where "+var+" like '%"+value+"%'";

			ResultSet result = stmt.executeQuery(query);

			while (result.next()) {
				Fis f = new Fis(result.getString(1), result.getString(2), result.getString(3), result.getString(4),
						result.getString(5),
						result.getString(6));
				fisler.add(f);
				// System.out.println("result id : " + f.isletmeAdi);
			}

			fisBilgileri = new String[fisler.size()][5];

			for (int i = 0; i < fisBilgileri.length; i++) {
				fisBilgileri[i][0] = fisler.get(i).id;
				fisBilgileri[i][1] = fisler.get(i).isletmeAdi;
				fisBilgileri[i][2] = fisler.get(i).tarih;
				fisBilgileri[i][3] = fisler.get(i).fisNo;
				fisBilgileri[i][4] = fisler.get(i).topkdv;
				fisBilgileri[i][5] = fisler.get(i).toplam;
			}

			con.close();
		} catch (Exception e) {
			System.out.println("db error : " + e);
		}

		return fisBilgileri;
	}

}

