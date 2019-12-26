

	package tesseract;
	
	import java.awt.image.BufferedImage;
	import java.util.ArrayList;
	
	import gui.WindowManager;
	import net.sourceforge.tess4j.ITesseract;
	import net.sourceforge.tess4j.Tesseract;
	import net.sourceforge.tess4j.TesseractException;
	
	public class TesseractMethods {
	
		public static void runTesseract(String filePath) {
	
			Keywords keyWords = new Keywords();
			BufferedImage image = PublicArea.readImage(filePath);
	
			ITesseract instance = new Tesseract();
			instance.setLanguage("tur");
	
			try {
	
				String result = instance.doOCR(image);
	
				String[] lines = StringJobs.splitByRows(result);
	
	
				String isletmeAdi, tarih, toplam, fisNo, topkdv;
				isletmeAdi = tarih = toplam = fisNo = topkdv =  "";
	
				lines = StringJobs.cutUnnecessaryChar(lines);
				
				
				WindowManager.textAreaSetText(lines);
				
				isletmeAdi = StringJobs.findCompanyName(lines);
	
				if (isletmeAdi.equals("not found")) {
					for (int i = 0; i < lines.length; i++) {
						if ((!lines[i].contains("|||") || !lines[i].contains("III")) && lines[i].length() > 10) {
							isletmeAdi = lines[i];
							break;
						}
					}
				}
	
				System.out.println("\n iþletme adý : ***" + isletmeAdi + "***\n");
	
				int productLimit = -1;
				ArrayList<String> products = new ArrayList<String>();
				ArrayList<String> productPrices = new ArrayList<String>();
				ArrayList<String> productTaxes = new ArrayList<String>();
				String[] lineSplit;
				for (int i = 0; i < lines.length; i++) {
	
					lineSplit = lines[i].split(" ");
	
					for (int j = 0; j < lineSplit.length; j++) {
						String word = keyWords.search(lineSplit[j]);
						if (word != null) {
	
							String manipulated = stringManipulation(lines[i], word, j);
	
							if (word.equalsIgnoreCase("fiþ")) {
								if (fisNo.equals("") || fisNo.isEmpty()) {
									fisNo = manipulated;
								}
							} else if (word.equalsIgnoreCase("tarih")) {
								//System.out.println("tarih degiskeni : " + tarih);
								//System.out.println("manipulated : " + manipulated);
								if (tarih.equals("") || tarih.isEmpty()) {
									tarih = manipulated;
								}
							} else if (word.equalsIgnoreCase("toplam")) {
								productLimit = i;
								//System.out.println("toplam : " + lines[i]);
								if (toplam.equals("") || toplam.isEmpty()) {
									toplam = manipulated;
								}
							} else if(word.equalsIgnoreCase("topkdv")) {
								if (topkdv.equals("") || toplam.isEmpty()) {
									topkdv = manipulated;
								}
							}
						}
					}
	
				}
	
				/*
				 * 
				 * 
				 */
	
				if (tarih.isEmpty()) {
					for (int i = 0; i < lines.length; i++) {
						lineSplit = lines[i].split(" ");
						for (int j = 0; j < lineSplit.length; j++) {
							lineSplit[j] = lineSplit[j].trim();
							if ((lineSplit[j].contains("/") || lineSplit[j].contains(".")) && lineSplit[j].length() == 10) {
								tarih = lineSplit[j];
							}
						}
					}
				}
	
				// 10/10/2010
	
				if(fisNo.length() != 0 && !fisNo.equals("") && fisNo != null)
					fisNo = StringJobs.checkBeginEnd(fisNo);
				if (toplam.length() != 0 && !toplam.equals("") && toplam != null)
					toplam = StringJobs.checkBeginEnd(toplam);
				if (tarih.length() != 0 && !tarih.equals("") && tarih != null) 
					tarih = StringJobs.checkBeginEnd(tarih);
				if (topkdv.length() != 0 && !topkdv.equals("") && topkdv != null) 
					topkdv = StringJobs.checkBeginEnd(topkdv);
	
				if (tarih.length() == 10) {
					String temp = "";
					temp = tarih.substring(0, 2);
					temp += "/";
					temp += tarih.substring(3, 5);
					temp += "/";
					temp += tarih.substring(6);
	
					tarih = temp;
				}
	
				/*
				 * 
				 * 
				 */
				if (productLimit == -1) 
					productLimit = lines.length;
				
				for (int i = 0; i < productLimit; i++) {
					if (lines[i].contains("%")) {
						String[] productLine = lines[i].split("%");
						int pos = lines[i].indexOf("%");
						String urun = "";
						for (int j = 0; j < pos; j++) {
							urun += lines[i].charAt(j);
						}
	
						products.add(urun.trim());
						String urunFiyati = "";
						for (int j = pos; j < lines[i].length(); j++) {
							urunFiyati += lines[i].charAt(j);
						}
	
						String[] tax = urunFiyati.split(" ");
						for (int j = 0; j < tax.length; j++) {
							tax[j] = tax[j].trim();
							if (tax[j].contains("%")) {
								productTaxes.add(tax[j]);
							} else {
								System.out.println("geldi : " + tax[j]);
								tax[j] = tax[j].trim();
								if (!tax[j].isEmpty() && tax[j].length() != 1 && !tax[j].equals(" ")) {
									tax[j] = StringJobs.checkBeginEnd(tax[j].trim());
									productPrices.add(tax[j]);
								}
							}
						}
	
					}
				}
	
				System.out.println("\n\n--- urunler --- \n\n");
				System.out.println(products.toString());
				System.out.println(productPrices.toString());
				System.out.println(productTaxes.toString());
				System.out.println();
	
				System.out.println("\n\n");
	
				System.out.println("\n\n satirlar : \n\n");
	
				for (int i = 0; i < lines.length; i++) {
					System.out.println(lines[i]);
				}
	
				System.out.println(" isletme adi : " + isletmeAdi + " \ntoplam : " + toplam + " fisno : " + fisNo
						+ " tarih : " + tarih);
	
				Database.saveRecordToDb(isletmeAdi, tarih, fisNo, topkdv, toplam, products, productTaxes, productPrices);
	
			} catch (TesseractException ex) {
				System.out.println("run tess error : " + ex.getMessage());
			}
	
		}
	
		public static String stringManipulation(String str, String key, int pos) {
			String[] split = str.split(" ");
			String mString = "";
			
			if (key.equalsIgnoreCase("tarih")) {
				for (int i = 0; i < split.length; i++) {
					if ((split[i].matches(".*\\d.*")) && (split[i].contains("/") || (split[i].contains(".") && split[i].length() > 6))) {
						mString = split[i];
					}
				}
			} else {
				for (int i = split.length - 1; i >= pos; i--) {
					if ((split[i].matches(".*\\d.*"))) {
						mString = split[i];
						break;
					}
				}
			}
			
	
			if (!mString.isEmpty() && !mString.contains("")) {
				if (!Character.isDigit(mString.charAt(0))) {
					String mString2 = "";
	
					for (int i = 1; i < mString.length(); i++) {
						mString2 += mString.charAt(i);
					}
	
					mString = mString2;
				}
	
				if (!Character.isDigit(mString.charAt(mString.length() - 1))) {
	
					String mString2 = "";
	
					for (int i = 0; i < mString.length() - 1; i++) {
						mString2 += mString.charAt(i);
					}
	
					mString = mString2;
	
				}
			}
	
			return mString;
	
		}
	
	}


