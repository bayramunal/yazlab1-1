

package gui;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tesseract.Database;

public class ProductWindow {
	
	private JPanel jPanel;
	private JFrame frame;
	private JTable dataTable;
	private JScrollPane scrollDataTable;
	
	
	public ProductWindow (String fisNo) {
		jPanel = new JPanel();
		frame = new JFrame(fisNo + " numaralý fiþe ait ürünler");
		frame.setSize(500, 200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(jPanel);
		frame.setVisible(true);
		
		createDataTable(fisNo);
		
	}
	
public void createDataTable (String fisNo) {
		
		String[][] urunler = Database.getProducts(fisNo);
		String[] columnNames = {"Ürün adý", "Ürün KDV", "Ürün Fiyatý" };
		
		dataTable = new JTable(urunler, columnNames);
		
		dataTable.setFillsViewportHeight(true);
		
		scrollDataTable = new JScrollPane(dataTable);
		
		scrollDataTable.setBounds(5, 5, 475, 150);
		addComponentOnJPanel(scrollDataTable);
	}

	private void addComponentOnJPanel(Component... components) {
		for (Component o : components) {
			jPanel.add(o);
		}
	
		jPanel.setLayout(null);
		frame.validate();
		frame.repaint();
	}
	
	
}


