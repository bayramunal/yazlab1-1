
package tesseract;

public class StringJobs {


	static Keywords keyWords = new Keywords();
	
	public static String mergeSplitList (String[] split, int startPos, String var) {
		
		var = "";
		
		for (int j = startPos +1; j < split.length; j++) {
			var += split[j];
		}
		
		return var;
	}
	
	// satýrlara göre ayýr
	public static String[] splitByRows (String str) {
		return str.split("\\r?\\n");
	}
	
	// baþtaki 1 karakterlik hatayý temizle
	public static String[] cutUnnecessaryChar (String[] list) {
		
		for (int i = 0; i < list.length; i++) {
			if (list[i].length() > 3) {
				if ((list[i].substring(1,2)).equals(" ")) {
					
					list[i] = list[i].substring(2);
					
				}
			}
		}
		
		return list;
		
	}
	
	
	public static String findCompanyName(String[] lines) {
		
		String isletmeAdi = "not found";
		boolean bulunduMu = false;
		for (int i = 0; i < lines.length; i++) {
			String[] split = lines[i].split(" ");
			for (int j = 0; j < split.length; j++) {
				String key = keyWords.search(split[j]);
				if (key != null) {
					if (key.equalsIgnoreCase("maðazacýlýk")) {
						isletmeAdi = lines[i];
						System.out.println("iþletme adý : " + isletmeAdi);
						bulunduMu = true;
						break;
					}
				}
				
			}

			if (bulunduMu) {
				break;
			}
		}
		
		return isletmeAdi;
	}
	
	public static String checkString(String comboCompanyName) {
		if (comboCompanyName.charAt(0) == '"' || comboCompanyName.charAt(0) == '\'') {
			String temp = "";
			for (int i = 1; i < comboCompanyName.length(); i++) {
				temp += comboCompanyName.charAt(i);
			}
			comboCompanyName = temp;
		}
		
		if (comboCompanyName.charAt(comboCompanyName.length() - 1) == '"' || comboCompanyName.charAt(comboCompanyName.length() - 1) == '\'') {
			String temp = "";
			for (int i = 0; i < comboCompanyName.length() - 1; i++) {
				temp += comboCompanyName.charAt(i);
			}
			comboCompanyName = temp;
		}
		return comboCompanyName;
	}
	
	public static String checkBeginEnd (String str) {
		if (!Character.isDigit(str.charAt(0))) {
			str = str.substring(1);
		}
		if (!Character.isDigit(str.charAt(str.length()-1))) {
			str = str.substring(0, str.length() - 1);
		}
		
		return str;
	}

	
}

