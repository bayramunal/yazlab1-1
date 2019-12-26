
package tesseract;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Detection {

	public static void writeFile(String path, BufferedImage subImage) {

		File outputfile = new File(path);

		if (subImage.getWidth() > subImage.getHeight()) {
			int w = subImage.getWidth();
			int h = subImage.getHeight();
			BufferedImage newImage = new BufferedImage(subImage.getWidth(), subImage.getHeight(), subImage.getType());
			Graphics2D g2 = newImage.createGraphics();
			g2.rotate(Math.toRadians(90), w / 2, h / 2);
			g2.drawImage(subImage, null, 0, 0);
		}

		try {
			ImageIO.write(subImage, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("write image error : " + e.getMessage());
		}

	}

	public static int getSubImageTextLength(BufferedImage bufferedImage) {

		ITesseract instance = new Tesseract();
		instance.setLanguage("tur");
		int uzunluk = -1;
		try {
			String result = instance.doOCR(bufferedImage);

			// textAreaSetText(result);

			// test(result);

			// System.out.println(result + " uzunluk : " + result.length());
			uzunluk = result.length();
		} catch (TesseractException ex) {
			System.out.println(ex.getMessage());
		}
		return uzunluk;
	}

	public static void wordDetect(BufferedImage bufferedImage, BufferedImage img2, int tessPageIteratorLevel,
			String path) {

		Tesseract instance = new Tesseract();

		int level = tessPageIteratorLevel;

		List<Rectangle> result = null;
		double uzaklik = -1;
		try {
			//System.out.println(bufferedImage == null ? "true" : "false");
			//System.out.println("iterator level : " + tessPageIteratorLevel);
			result = instance.getSegmentedRegions(bufferedImage, level);
		} catch (TesseractException e) {
			System.out.println("Symbol detect error : " + e.getMessage());
		}

		if (result != null) {
			Rectangle rect = result.get(0);

			for (int i = 0; i < result.size(); i++) {
				rect = result.get(i);

				uzaklik = Math.sqrt(Math.pow((rect.x + rect.height) - rect.x, 2)
						+ Math.pow((rect.y + rect.height + rect.width) - (rect.y + rect.width), 2));
				if (uzaklik >= 30 && rect.width > 15) {
					BufferedImage croppedImage = img2.getSubimage(rect.x, rect.y, rect.width, rect.height);
					writeFile(path + i + ".png", croppedImage);

				}
			}
		}

	}

	public static BufferedImage symbolDetect(BufferedImage bufferedImage, BufferedImage img2, int tessPageIteratorLevel,
			String path) {

		// sonucu görebilmek için kopyalandý

		double minX = 9999, minY = 9999;
		double maxX = 0, maxY = 0;

		Tesseract instance = new Tesseract();

		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.RED);

		int level = tessPageIteratorLevel;

		List<Rectangle> result = null;
		try {
			result = instance.getSegmentedRegions(bufferedImage, level);
		} catch (TesseractException e) {
			// TODO Auto-generated catch block
			System.out.println("Symbol detect error : " + e.getMessage());
		}
		Rectangle rect = result.get(0);
		minX = rect.getX() + rect.getWidth();
		minY = rect.getY() + rect.getHeight();
		maxX = rect.getX() + rect.getWidth();
		maxY = rect.getY() + rect.getHeight();
		ArrayList<Rectangle> rects = new ArrayList<Rectangle>();

		double uzaklik = -1;
		for (int i = 0; i < result.size(); i++) {
			rect = result.get(i);

			uzaklik = Math.sqrt(Math.pow((rect.x + rect.height) - rect.x, 2)
					+ Math.pow((rect.y + rect.height + rect.width) - (rect.y + rect.width), 2));
			if (uzaklik >= 30 && rect.width > 15) {

				BufferedImage croppedImage = bufferedImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
				// writeFile(PublicArea.textLines + i + ".png", croppedImage);

				if (getSubImageTextLength(croppedImage) > 2) {
					if ((rect.getX() + rect.getWidth()) > maxX) {
						maxX = rect.getX() + rect.getWidth();
					}
					if (rect.getX() < minX) {
						minX = rect.getX();
					}

					if ((rect.getY() + rect.getHeight()) > maxY) {
						maxY = rect.getY() + rect.getHeight();
					}
					if (rect.getY() < minY) {
						minY = rect.getY();
					}

					rects.add(rect);
					// g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
				}

			}
		}

		// writeFile("C:\\Users\\Bayram\\Desktop\\rectangles.png", bufferedImage);
		bufferedImage = bufferedImage.getSubimage((int) minX, (int) minY, (int) (maxX - minX),
				(int) ((maxY - minY) * 0.8));
		writeFile("C:\\Users\\Bayram\\Desktop\\cropped.png", bufferedImage);
		BufferedImage cropped2 = img2;
		//System.out.println("minY : " + minY + " minX : " + minX + " maxX : " + maxX + " maxY : " + maxY);
		if ((maxX - minX) >= 0 && (maxY - minY) >= 0) {
			cropped2 = img2.getSubimage((int) minX, (int) minY, (int) (maxX - minX), (int) ((maxY - minY)));
		}
		// BufferedImage cropped2 = img2.getSubimage((int) minX, (int) (maxY -
		// maxY*0.96), (int) (maxX - minX), (int) ((maxY - minY) * 0.8));

		return cropped2;

	}

	public static void satirlariAl(BufferedImage img, int level, String path) {

		try {
			FileUtils.cleanDirectory(new File(PublicArea.textLines));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Tesseract instance = new Tesseract();
		List<Rectangle> result = null;
		try {
			result = instance.getSegmentedRegions(img, level);
		} catch (TesseractException e) {
			// TODO Auto-generated catch block
			System.out.println("Symbol detect error : " + e.getMessage());
		}

		Rectangle rect = result.get(0);
		double uzaklik = -1;
		int cropY = 9999;
		
		Rectangle widthRect = null;
		int width = 0;

		Rectangle first = null;

		for (int i = 0; i < result.size(); i++) {
			rect = result.get(i);

			uzaklik = Math.sqrt(Math.pow((rect.x + rect.height) - rect.x, 2)
					+ Math.pow((rect.y + rect.height + rect.width) - (rect.y + rect.width), 2));
			if (uzaklik >= 30 && rect.width > 15) {
				BufferedImage croppedImage = img.getSubimage(rect.x, rect.y, rect.width, rect.height);

				int blackCount = PublicArea.countColor(croppedImage, false);
				int whiteCount = PublicArea.countColor(croppedImage, true);

				if (whiteCount > blackCount) {
					//System.out.println("cropped X : " + rect.x + " Y : " + rect.y);
					writeFile(path + i + ".png", croppedImage);
					if (cropY > rect.y) {
						cropY = rect.y;
						first = rect;
					}
					if (width < rect.width) {
						width = rect.width;
						widthRect = rect;
					}
				}
			}
		}
		
		//System.out.println("width : " + widthRect.width + " x: " + widthRect.x + " y: " + widthRect.y);

		//System.out.println("first X : " + first.x + " Y : " + first.y);

		// BufferedImage img2 = PublicArea.copyBufferedImage(img);
		//
		//System.out.println("cropY : " + cropY);
		//System.out.println(
		//		"image.getHeight() " + img.getHeight() + " - " + (img.getHeight() - rect.y) + " rect.y : " + rect.y);
		img = img.getSubimage(0, first.y, img.getWidth(), img.getHeight() - first.y);
		// usttenkirpilmis
		writeFile("C:\\Users\\Bayram\\Desktop\\cropped.png", img);
		

	}

}
