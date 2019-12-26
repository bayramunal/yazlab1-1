
package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Dictionary;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import net.sourceforge.lept4j.*;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import tesseract.Database;
import tesseract.Detection;
import tesseract.Fis;
import tesseract.Keywords;
import tesseract.PublicArea;
import tesseract.StringJobs;
import tesseract.TesseractMethods;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.jbig2.Bitmap;
import org.opencv.core.*;
import org.opencv.imgcodecs.*; // imread, imwrite, etc
import org.opencv.videoio.*; // VideoCapture

public class WindowManager implements ActionListener {

	private JPanel jPanel;
	private JButton btnSelect, btnCompanyNameSearch, btnDateSearch, btnUrunleriListele, btnRefreshData, btnSirala;
	private JFrame frame;
	private Image selectedImage;
	private JLabel selectedImageView;
	private static JTextArea textArea;
	private JTextField txtIsletmeAdi, txtTarih;
	private JScrollPane scrollText, scrollDataTable;
	public static JTable dataTable;
	private String choosenFilePath = null;
	private String selectedRowId;
	private JComboBox comboCompanyNames;
	private JRadioButton rbFiyatArtan, rbFiyatAzalan, rbTarihArtan, rbTarihAzalan;
	private ButtonGroup buttonGroup;
	
	private static String fisNo, tarih, toplam;

	public WindowManager() {
		
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollText = new JScrollPane(textArea);
		scrollText.setBounds(225, 40, 200, 250);

		initComponents(scrollText);

		createLabel("Ýþletme adý : ", 500, 40, 150, 30, false);
		createLabel("Tarih : ", 500, 115, 150, 30, false);
		createLabel("", 450, 0, 1, 320, true); // ortadaki çizgi
		createLabel("", 0, 320, 800, 1, true); // alttaki çizgi
		createLabel("", 5, 40, 200, 250, true); // fotoðraf çerçevesi
		btnSelect = initButton(btnSelect, "Bir fotoðraf seç", 5, 5, 200, 30); // fotoðraf seçme butonu
		btnCompanyNameSearch = initButton(btnCompanyNameSearch, "Ara", 705, 75, 70, 30); // sorgu çalýþtýrma butonu
		btnUrunleriListele = initButton(btnUrunleriListele, "Seçili fiþe ait ürünler", 5, 330, 189, 25);
		btnRefreshData = initButton(btnRefreshData, "Yenile", 704, 330, 70, 25);
		btnSirala = initButton(btnSirala, "Sýrala", 704, 200, 70, 80);
		// txtIsletmeAdi = initTextField(txtIsletmeAdi, 500, 75, 200, 30);
		txtTarih = initTextField(txtTarih, 500, 150, 200, 30);
		btnDateSearch = initButton(btnDateSearch, "Ara", 705, 150, 70, 30); // sorgu çalýþtýrma butonu

		createDataTable(Database.getRecords(""));

		String[] companyNames = Database.getCompanyNames();
		comboCompanyNames = new JComboBox();
		
		comboCompanyNames.addItem("Hepsi");
		
		for (int i = 0; i < companyNames.length; i++) {
			comboCompanyNames.addItem(companyNames[i]);
		}
		
		comboCompanyNames.setBounds(500, 75, 200, 30);
		jPanel.add(comboCompanyNames);
		frame.revalidate();
		frame.repaint();

		
		
		
		
		rbFiyatArtan = new JRadioButton("Toplamý artan sýrada sýrala");
		rbFiyatAzalan = new JRadioButton("Toplamý azalan sýrada sýrala");
		
		rbTarihArtan = new JRadioButton("Tarihi artan sýrada sýrala");
		rbTarihAzalan = new JRadioButton("Tarihi azalan sýrada sýrala");
		
		buttonGroup = new ButtonGroup();
		
		rbFiyatArtan.setBounds(500, 200, 180, 20);
		rbFiyatAzalan.setBounds(500, 220, 185, 20);
		
		rbTarihArtan.setBounds(500, 240, 180, 20);
		rbTarihAzalan.setBounds(500, 260, 180, 20);
		
		buttonGroup.add(rbFiyatAzalan);
		buttonGroup.add(rbFiyatArtan);
		buttonGroup.add(rbTarihArtan);
		buttonGroup.add(rbTarihAzalan);
		
		addComponentOnJPanel(rbFiyatArtan, rbFiyatAzalan, rbTarihArtan, rbTarihAzalan);
		
		
		
		
		
		
		btnSelect.addActionListener(this);
		btnUrunleriListele.addActionListener(this);
		btnRefreshData.addActionListener(this);
		btnCompanyNameSearch.addActionListener(this);
		btnDateSearch.addActionListener(this);
		btnSirala.addActionListener(this);
		
		
	}
	
	
	public void refreshComboBox () {
		
		if (comboCompanyNames != null) {
			comboCompanyNames.removeAllItems();
			
			String[] companyNames = Database.getCompanyNames();
			
			comboCompanyNames.addItem("Hepsi");
			
			for (int i = 0; i < companyNames.length; i++) {
				comboCompanyNames.addItem(companyNames[i]);
			}
		}
			
	}
	
	
	public void clearDataTable() {
		DefaultTableModel tableModel = (DefaultTableModel) dataTable.getModel();
		//System.out.println("row count : " + tableModel.getRowCount());
		int rowCount = tableModel.getRowCount();
		for (int i = rowCount - 1; i >= 0; i--) {
			tableModel.removeRow(i);
		}
	}
	
	public void fillDataTableFromDB(String[][] list) {
		DefaultTableModel tableModel = (DefaultTableModel) dataTable.getModel();
		
		if (list != null && list.length > 0) {
			for (int i = 0; i < list.length; i++) {
				tableModel.addRow(list[i]);
			}
		}
		
		tableModel.fireTableDataChanged();
	}

	public void hideIdColumn () {
		dataTable.setFillsViewportHeight(true);
		dataTable.getColumnModel().getColumn(0).setMaxWidth(0);
		dataTable.getColumnModel().getColumn(0).setMinWidth(0);
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(0); // hide
		dataTable.getColumnModel().getColumn(0).setResizable(false);
		// dataTable.removeColumn(dataTable.getColumnModel().getColumn(0));
	}
	
	public void dataTableListener () {
		dataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub
				//System.out.println(dataTable.getValueAt(dataTable.getSelectedRow(), 0).toString());
				selectedRowId = dataTable.getValueAt(dataTable.getSelectedRow(), 0).toString();
			}
		});
	}
	
	public void createDataTable(String[][] fisler) {

		DefaultTableModel tableModel = setUpTableData(fisler);
		tableModel.fireTableDataChanged();
		dataTable = new JTable(tableModel);
		
		hideIdColumn();
		//dataTableListener();

		scrollDataTable = new JScrollPane(dataTable);
		scrollDataTable.setBounds(5, 360, 770, 190);
		addComponentOnJPanel(scrollDataTable);
	}
	
	public String[] getDataColumnNames() {
		return new String[] { "ID", "Ýþletme Adý", "Tarih", "Fiþ No", "TopKDV", "Toplam" };
	}
	
	public DefaultTableModel insertDataToTableModel (DefaultTableModel tableModel, String[][] list) {
		for (int i = 0; i < list.length; i++) {
			tableModel.addRow(list[i]);
			tableModel.fireTableDataChanged();
		}
		
		return tableModel;
	}
	
	public DefaultTableModel setUpTableData(String[][] list) {
		DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.setColumnIdentifiers(getDataColumnNames());

		return insertDataToTableModel(tableModel, list);
	}

	private void createLabel(String labelText, int x, int y, int width, int height, boolean hasBorder) {
		JLabel mlabel = new JLabel(labelText);
		mlabel.setBounds(x, y, width, height);

		if (hasBorder) {
			mlabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		}
		addComponentOnJPanel(mlabel);
	}

	private JButton initButton(JButton button, String buttonText, int x, int y, int width, int height) {
		button = new JButton(buttonText);
		button.setBounds(x, y, width, height);

		addComponentOnJPanel(button);

		return button;
	}

	private JTextField initTextField(JTextField textField, int x, int y, int width, int height) {
		textField = new JTextField();
		textField.setBounds(x, y, width, height);

		addComponentOnJPanel(textField);

		return textField;
	}

	private void initComponents(Component... components) {

		initJFrame();
		jPanel = new JPanel();

		addComponentOnJPanel(components);

		applyJFrame();
	}

	private void initJFrame() {

		frame = new JFrame("OCR Tabanlý Fiþ Tanýma");
		frame.setSize(800, 600);
	}

	private void addComponentOnJPanel(Component... components) {
		for (Component o : components) {
			jPanel.add(o);
		}

		jPanel.setLayout(null);
		frame.validate();
		frame.repaint();
	}

	private void applyJFrame() {
		frame.add(jPanel);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void selectImage() {
		textArea.setText("");
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(PublicArea.selectDirectoryPath)); // baþlangýç gözatma dosya yolu
		chooser.setDialogTitle("Bir fotoðraf seç");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			choosenFilePath = chooser.getSelectedFile().toString();
			// System.out.println("file path : " + filePath);
			putImageToImageView(choosenFilePath);
		} else {
			System.out.println("Dosya yolu seçilmedi");
		}

	}

	private void putImageToImageView(String directoryPath) {

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(directoryPath));

		} catch (IOException e) {
			System.out.println("put image to imageview error : " + e.getMessage());
		}

		if (img.getWidth() > img.getHeight()) { // görüntü ters gelirse 90 derece saða çevir
			img = PublicArea.createRotatedCopy(img);
		}

		selectedImage = img.getScaledInstance(200, 250, Image.SCALE_SMOOTH);

		if (selectedImageView == null) {
			selectedImageView = new JLabel(new ImageIcon(selectedImage));
			selectedImageView.setBounds(5, 40, 200, 250);

			addComponentOnJPanel(selectedImageView);
		}

		selectedImageView.setIcon(new ImageIcon(selectedImage));

		selectedImageView.repaint();
		selectedImageView.revalidate();

		frame.repaint();
		frame.revalidate();

		preProcessing(directoryPath);
		TesseractMethods.runTesseract(PublicArea.runTesseract);
		
		// tabloyu yenile
		clearDataTable();
		fillDataTableFromDB(Database.getRecords(""));
		refreshComboBox();

	}
	
	
	
	public static void preProcessing(String directoryPath) {

		System.load(PublicArea.openCvDLLPath);

		try {
			FileUtils.cleanDirectory(new File(PublicArea.textLines));
		} catch (IOException e) {
			System.out.println("clean directory error : " + e.getMessage());
		}
		BufferedImage bufferedImage = PublicArea.readImage(directoryPath);

		if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
			bufferedImage = PublicArea.createRotatedCopy(bufferedImage);
		}
		
		
		
		// erode/*
		Mat img = null;
		
		try {
			img = PublicArea.BufferedImage2Mat(bufferedImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 2), new Point(1, 1));
		Imgproc.erode(img, img, kernel);
		// Imgproc.dilate(img, img, kernel);

		try {
			bufferedImage = PublicArea.Mat2BufferedImage(img);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// PublicArea.writeFile("C:\\Users\\Bayram\\Desktop\\cropped2.png",
		// bufferedImage);

		BufferedImage thresholdImage = PublicArea.applyThreshold(bufferedImage);

		int[] y = PublicArea.searchWhiteY(thresholdImage);
		int[] x = PublicArea.searchWhiteX(thresholdImage);
		
		//System.out.println(x[0] + ", " + x[1] + "\n" + y[0] + ", " + y[1]);
		Detection.writeFile("C:\\Users\\Bayram\\Desktop\\kirpmadanOnce.png", thresholdImage);

		BufferedImage thresh2 = PublicArea.copyBufferedImage(thresholdImage);
		Detection.symbolDetect(thresholdImage, thresh2, TessPageIteratorLevel.RIL_TEXTLINE, PublicArea.textLines);

		BufferedImage cropImage = bufferedImage.getSubimage(x[0], 0, x[1] - x[0],
				(int) (bufferedImage.getHeight() * 0.8));
		BufferedImage cropImageThreshold = PublicArea.applyThreshold(cropImage);

		Detection.writeFile("C:\\Users\\Bayram\\Desktop\\kirptiktanSonra.png", cropImageThreshold);

		y = PublicArea.searchWhiteY(cropImageThreshold);
		x = PublicArea.searchWhiteX(cropImageThreshold);

		//System.out.println(" ---- " + x[0] + ", " + x[1] + "\n" + y[0] + ", " + y[1]);
		BufferedImage cropImageThreshold2;
		if ((x[1] - x[0]) > 0 && (y[1] - y[0]) > 0) {
			cropImageThreshold2 = cropImageThreshold.getSubimage(x[0], y[0], x[1] - x[0], y[1] - y[0]);
		} else {
			cropImageThreshold2 = cropImageThreshold;
		}

		Detection.writeFile("C:\\Users\\Bayram\\Desktop\\kirptiktanSonra2.png", cropImageThreshold);

		/*		*/
		BufferedImage original = PublicArea.copyBufferedImage(cropImageThreshold2);
		BufferedImage kirpilacak = Detection.symbolDetect(cropImageThreshold2, original,
				TessPageIteratorLevel.RIL_TEXTLINE, PublicArea.textLines);

		Detection.satirlariAl(kirpilacak, TessPageIteratorLevel.RIL_TEXTLINE, PublicArea.textLines);

	}
	
	public void radioButtonAction(String condition) {
		clearDataTable();
		fillDataTableFromDB(Database.getRecords(condition));
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		
		if (e.getSource() == btnSelect) {
			selectImage();
		} else if (e.getSource() == btnUrunleriListele) {
			
			selectedRowId = dataTable.getValueAt(dataTable.getSelectedRow(), 0).toString();
			
			if (selectedRowId == null || selectedRowId.isEmpty() || selectedRowId.equals("")) {
				JOptionPane.showMessageDialog(null, "Lütfen bir satýr seçiniz");
			} else {
				ProductWindow productWindow = new ProductWindow(selectedRowId);
			}
		} else if (e.getSource() == btnRefreshData) {
			dataTable.clearSelection();
			// dataTable.removeAll();
			clearDataTable();
			fillDataTableFromDB(Database.getRecords(""));
		} else if (e.getSource() == btnCompanyNameSearch) {
			dataTable.clearSelection();
			//System.out.println(String.valueOf(comboCompanyNames.getSelectedItem()));
			String comboCompanyName = String.valueOf(comboCompanyNames.getSelectedItem());
			
			comboCompanyName = StringJobs.checkString(comboCompanyName);
			
			if (!comboCompanyName.isEmpty() && !comboCompanyName.equals("") && comboCompanyName != null
					&& !comboCompanyName.equalsIgnoreCase("hepsi")) {
				clearDataTable();
				fillDataTableFromDB(Database.getSpecificRecords("isletmeAdi", comboCompanyName.trim()));
			} else if (comboCompanyName.equalsIgnoreCase("hepsi")) {
				clearDataTable();
				fillDataTableFromDB(Database.getRecords(""));
			}
			
		} else if (e.getSource() == btnDateSearch) {
			dataTable.clearSelection();
			String date = txtTarih.getText().trim();
			if (!date.isEmpty() && !date.equals("") && date != null) {
				clearDataTable();
				fillDataTableFromDB(Database.getSpecificRecords("tarih", date));
			} else if (date.isEmpty() && date.equals("")) {
				clearDataTable();
				fillDataTableFromDB(Database.getRecords(""));
			}
		} else if (e.getSource() == btnSirala) {
			if (rbFiyatArtan.isSelected()) {
				radioButtonAction("order by toplam asc");
			} else if (rbFiyatAzalan.isSelected()) {
				radioButtonAction("order by toplam desc");
			} else if (rbTarihArtan.isSelected()) {
				radioButtonAction("order by tarih asc");
			} else if (rbTarihAzalan.isSelected()) {
				radioButtonAction("order by tarih desc");
			}
		}

	}



	public static String getFisNo() {
		return fisNo;
	}

	public static String getTarih() {
		return tarih;
	}

	public static String getToplam() {
		return toplam;
	}

	public static void setFisNo(String var) {
		fisNo = var;
	}

	public static void setTarih(String var) {
		tarih = var;
	}

	public static void setToplam(String var) {
		toplam = var;
	}

	public static void textAreaSetText(String[] text) {
		
		textArea.setText("");
		
		for (int i = 0; i < text.length; i++) {
			textArea.setText(textArea.getText() + text[i] + "\n");
		}
		
	}


}
