

package tesseract;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class PublicArea {

	// threshold için gerekli deðerler
	//private static final double CANNY_THRESHOLD_RATIO = .2; // Suggested range .2 - .4
	//private static final int CANNY_STD_DEV = 1; // Range 1-3

	//tesseract infos
	public static final String textLines = "C:\\Users\\Bayram\\Desktop\\symbol\\";
	public static final String words = "C:\\Users\\Bayram\\Desktop\\crop\\";
	public static final String openCvDLLPath = "C:\\Users\\Bayram\\Desktop\\opencvfolder\\opencv\\build\\java\\x64\\opencv_java411.dll";
	public static final String selectDirectoryPath = "C:\\Users\\Bayram\\Desktop";
	public static final String runTesseract = "C:\\Users\\Bayram\\Desktop\\cropped.png";
	
	//database infos
	public static final String dbName = "tesseract";
	public static final String connectionString = "jdbc:mysql://localhost:8889/"+dbName+"?useSSL=false";
	public static final String dbUserName = "root";
	public static final String dbPassword = "root";
	
	
	// fotoðraf yan bir þekilde kayýt ediliyorsa, saat yönünde 90 derece çevirmek
	// için
	public static BufferedImage createRotatedCopy(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();

		BufferedImage rot = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);

		double theta;
		theta = Math.PI / 2;

		AffineTransform xform = new AffineTransform();
		xform.translate(0.5 * h, 0.5 * w);
		xform.rotate(theta);
		xform.translate(-0.5 * w, -0.5 * h);
		Graphics2D g = (Graphics2D) rot.createGraphics();
		g.drawImage(img, xform, null);
		g.dispose();

		return rot;
	}


	// dosya yazma iþlemini yapan metod
	public static void writeFile(String path, BufferedImage subImage) throws IOException {

		File outputfile = new File(path);

		if (subImage.getWidth() > subImage.getHeight()) {
			int w = subImage.getWidth();
			int h = subImage.getHeight();
			BufferedImage newImage = new BufferedImage(subImage.getWidth(), subImage.getHeight(), subImage.getType());
			Graphics2D g2 = newImage.createGraphics();
			g2.rotate(Math.toRadians(90), w / 2, h / 2);
			g2.drawImage(subImage, null, 0, 0);
		}

		ImageIO.write(subImage, "png", outputfile);

	}

	// verilen dosya yolundan görüntüyü okur
	public static BufferedImage readImage(String path) {
		File imageFile = new File(path);
		System.out.println("path : " + path);
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(imageFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bufferedImage;
	}

	// görüntüye threshold uygular
	public static BufferedImage applyThreshold(BufferedImage bufferedImage) {
		Mat srcMat = null;
		try {
			srcMat = BufferedImage2Mat(bufferedImage);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("threshold error 1 : " + e1.getMessage());
		}
		Mat grayMat = new Mat();
		Imgproc.blur(srcMat, srcMat, new Size(3.0, 3.0));
		Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY);

		Imgproc.threshold(grayMat, srcMat, 0, 255, Imgproc.THRESH_OTSU);
		BufferedImage print = null;
		try {
			print = Mat2BufferedImage(srcMat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("threshold error 2 : " + e.getMessage());
		}

		return print;
	}

	// threshold uygulanmýþ görüntüdeki y eksenindeki en uzun beyaz çizgiyi bulur
	public static int[] searchWhiteY(BufferedImage print) {

		Graphics2D g2d = print.createGraphics();
		g2d.setColor(Color.BLUE);

		ArrayList<MyPoint> list = new ArrayList<MyPoint>();

		int baslangicX = -1, baslangicY = -1, bitisX = -1, bitisY = -1;

		for (int i = 0; i < print.getWidth(); i++) {
			for (int j = 0; j < print.getHeight(); j++) {

				Color c = new Color(print.getRGB(i, j));

				if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255) {
					if (baslangicX == -1)
						baslangicX = i;
					if (baslangicY == -1)
						baslangicY = j;

					bitisY = j;
				} else {
					list.add(new MyPoint(baslangicX, i, baslangicY, bitisY, (bitisY - baslangicY), false)); // y için
																											// false
					baslangicX = -1;
					bitisX = -1;
					baslangicY = -1;
					bitisY = -1;
				}

			}
		}

		int enBuyuk = 0;
		int y1 = -1, y2 = -1;
		int x = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).uzunlukY > enBuyuk) {

				enBuyuk = list.get(i).uzunlukY;
				y1 = list.get(i).baslangicY;
				y2 = list.get(i).bitisY;
				x = list.get(i).baslangicX;
			}
		}

		// System.out.println("uzunluk Y : " + enBuyuk + " " + x + " " + y1 + ", " +
		// y2);

		// System.out.println("\n\n");

		return new int[] { y1, y2 };
	}

	// threshold uygulanmýþ görüntüdeki en uzun x çizgisini bulur
	public static int[] searchWhiteX(BufferedImage print) {

		Graphics2D g2d = print.createGraphics();
		g2d.setColor(Color.BLUE);

		ArrayList<MyPoint> list = new ArrayList<MyPoint>();

		int baslangicX = -1, baslangicY = -1, bitisX = -1, bitisY = -1;

		for (int i = 0; i < print.getHeight(); i++) {
			for (int j = 0; j < print.getWidth(); j++) {

				Color c = new Color(print.getRGB(j, i));

				if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255) {
					if (baslangicX == -1)
						baslangicX = j;
					if (baslangicY == -1)
						baslangicY = i;

					bitisX = j;
				} else {
					list.add(new MyPoint(baslangicX, j, baslangicY, bitisX, (bitisX - baslangicX), true)); // x için
																											// true
					baslangicX = -1;
					bitisX = -1;
					baslangicY = -1;
					bitisY = -1;
				}

			}
		}

		int enBuyuk = 0;
		int y1 = -1, y2 = -1;
		int x = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).uzunlukX > enBuyuk) {

				enBuyuk = list.get(i).uzunlukX;
				y1 = list.get(i).baslangicX;
				y2 = list.get(i).bitisX;
				x = list.get(i).baslangicY;
			}
		}

		 //System.out.println("uzunluk X : " + enBuyuk + " " + x + " " + y1 + ", " +
		 //y2);

		// System.out.println("bitti");

		return new int[] { y1, y2 };

	}
	
	public static int countColor(BufferedImage print, boolean state) {
		
		int whiteCount = 0;
		int blackCount = 0;
		
		for (int i = 0; i < print.getHeight(); i++) {
			for (int j = 0; j < print.getWidth(); j++) {

				Color c = new Color(print.getRGB(j, i));

				if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255) {
					whiteCount++;
				} else {
					blackCount++;
				}

			}
		}
		
		if (state) {
			return whiteCount;
		} else {
			return blackCount;
		}
		
	}

	// BufferedImage görüntüsünü 'Mat' nesnesine çevirir
	public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		byteArrayOutputStream.flush();
		return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
	}

	// Mat nesnesini BufferedImage nesnesine çevirir
	public static BufferedImage Mat2BufferedImage(Mat matrix) throws IOException {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}


	public static String getTesseractString (String path) {
		
		BufferedImage image = readImage(path);
		
		ITesseract instance = new Tesseract();
		instance.setLanguage("tur");
		
		String result = null;

		try 
		{
			result = instance.doOCR(image);	
		}
		catch(TesseractException ex)
		{
			System.out.println("run tess error : " + ex.getMessage());
		}
		
		return result;
	}
	
	
	public static BufferedImage copyBufferedImage(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.getRaster();
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	

}
