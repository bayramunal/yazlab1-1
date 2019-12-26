

package tesseract;

public class MyPoint {
	int baslangicX = -1, baslangicY = -1, bitisX = -1, bitisY = -1;
	int uzunlukX = 0, uzunlukY;
	public MyPoint(int baslangicX, int bitisX, int baslangicY, int bitisY, int uzunluk, boolean durum) {
		this.baslangicX = baslangicX;
		this.bitisX = bitisX;
		this.baslangicY = baslangicY;
		this.bitisY = bitisY;
		
		if (durum) {
			this.uzunlukX = uzunluk;
		} else {
			this.uzunlukY = uzunluk;
		}
		
	}
	
}

