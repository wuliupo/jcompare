package tim.jarcomp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Class to manage the main compare window
 */
public class CompareWindow
{
	/** Main window object */
	private JFrame _mainWindow = null;
	/** Two files to compare */
	private File[] _files = new File[2];
	/** Displays for jar file details */
	private JarDetailsDisplay[] _detailsDisplays = null;
	/** Label for compare status */
	private JLabel _statusLabel = null;
	/** Second label for contents status */
	private JLabel _statusLabel2 = null;
	/** Table model */
	private EntryTableModel _tableModel = null;
	private JTable table;
	/** File chooser */
	private JFileChooser _fileChooser = null;
	/** Button to check md5 sums */
	private JButton _md5Button = null;
	private JButton _loadButton = null;
	/** Refresh button to repeat comparison */
	private JButton _refreshButton = null;
	private JButton _exchangeButton = null;
	private JButton _cacheButton = null;
	private JButton _exportButton;
	private JComboBox<String> _filterBox = null;
	/** Flag to process md5 sums */
	private boolean _checkMd5 = false;

	private JButton _oldFileChoose = null;
	private JButton _newFileChoose = null;

	/**
	 * Constructor
	 */
	public CompareWindow()
	{
		_mainWindow = new JFrame("JCompare - Compressed file Comparer");
		_mainWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		_mainWindow.getContentPane().add(makeComponents());
		_mainWindow.pack();
		_mainWindow.setVisible(true);
	}

	/**
	 * Make the GUI components for the main dialog
	 * @return JPanel containing GUI components
	 */
	private JPanel makeComponents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// Top panel
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		// Button panel
		JPanel buttonPanel = new JPanel();
		JButton compareButton = new JButton("Compare ...");
		compareButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startCompare(_files[0], _files[1], false);
			}
		});
		buttonPanel.add(compareButton);

		_oldFileChoose = new JButton("choose old file");
		_oldFileChoose.setEnabled(true);
		_oldFileChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseFile(_files[0], _files[1], true);
			}
		});
		//buttonPanel.add(_oldFileChoose);
		
		_newFileChoose = new JButton("choose new file");
		_newFileChoose.setEnabled(true);
		_newFileChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseFile(_files[0], _files[1], false);
			}
		});
		//buttonPanel.add(_newFileChoose);
		
		_refreshButton = new JButton("Refresh");
		_refreshButton.setEnabled(false);
		_refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startCompare(_files[0], _files[1], false);
			}
		});
		buttonPanel.add(_refreshButton);
		
		_exchangeButton = new JButton("Exchange");
		_exchangeButton.setEnabled(false);
		_exchangeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startCompare(_files[1], _files[0], false);	//exchange
			}
		});
		buttonPanel.add(_exchangeButton);
		
		_cacheButton = new JButton("Delete Cache File");
		_cacheButton.setEnabled(false);
		_cacheButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteFile(new File(_files[0].getAbsolutePath() + "_tmp"));
				deleteFile(new File(_files[1].getAbsolutePath() + "_tmp"));
				JOptionPane.showMessageDialog(_mainWindow, "Delete Cache File successed: " + _files[0].getParentFile().getAbsolutePath(),
						"Delete Success", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		buttonPanel.add(_cacheButton);
		
		_filterBox = new JComboBox<String>();
		//"show All", "only Add", "only Delete", "checksum change", "size change", "only equal", "same size"
		_filterBox.addItem("show All");
		_filterBox.addItem("only Add");
		_filterBox.addItem("only Delete");
		_filterBox.addItem("Only folder");
		_filterBox.addItem("checksum");
		_filterBox.addItem("size change");
		_filterBox.addItem("only equal");
		_filterBox.addItem("same size");
		
		_filterBox.setEnabled(false);
		_filterBox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				EntryTableModel.filter = (String) e.getItem();
				if ("checksum".equals(EntryTableModel.filter) || "only equal".equals(EntryTableModel.filter)){
					startCompare(_files[0], _files[1], true);
					_md5Button.setEnabled(false);
				} else {
					startCompare(_files[0], _files[1], false);
					_md5Button.setEnabled(true);
				}
			}});
		buttonPanel.add(_filterBox);
		
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(buttonPanel);

		//file details panel
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridLayout(1, 2, 5, 5));
		_detailsDisplays = new JarDetailsDisplay[2];
		_detailsDisplays[0] = new JarDetailsDisplay("", _oldFileChoose);
		detailsPanel.add(_detailsDisplays[0], BorderLayout.WEST);
		_detailsDisplays[1] = new JarDetailsDisplay("", _newFileChoose);
		detailsPanel.add(_detailsDisplays[1], BorderLayout.EAST);
		topPanel.add(detailsPanel);
		detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		//status button
		_statusLabel = new JLabel("");
		_statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(_statusLabel);
		_statusLabel2 = new JLabel("");
		//_statusLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(_statusLabel2);
		mainPanel.add(topPanel, BorderLayout.NORTH);

		// main table panel
		_tableModel = new EntryTableModel();
		table = new JTable(_tableModel);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(40);
		table.getColumnModel().getColumn(2).setPreferredWidth(40);
		table.getColumnModel().getColumn(3).setPreferredWidth(40);
		// Table sorting by clicking on column headings
		table.setAutoCreateRowSorter(true);
		// File size rendering
		//table.getColumnModel().getColumn(3).setCellRenderer(new SizeChangeRenderer());
		//table.setDefaultRenderer(JButton.class, new DefaultTableCellRenderer(){
		table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
			private static final long serialVersionUID = -3538491678170025699L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
//				if (value == null) {
//					return new JLabel();
//				} else if (value instanceof String) {	//column == 0 || column == 1 || column == 2
//					JLabel jLabel = new JLabel((String)value);
//					jLabel.setFont(new java.awt.Font(null, Font.PLAIN, 12));
//					return jLabel;
//				} else 
				if (value instanceof JButton) {
					JButton button = (JButton) value;
					if (isSelected) {
						button.setForeground(table.getSelectionForeground());
						button.setBackground(table.getSelectionBackground());
					} else {
						button.setForeground(table.getForeground());
						button.setBackground(table.getBackground());
					}
					//button.setFont(table.getFont());
					if (hasFocus) {
						button.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
						if (!isSelected && table.isCellEditable(row, column)) {
							Color col = UIManager.getColor("Table.focusCellForeground");
							if (col != null) {
								button.setForeground(col);
							}
							col = UIManager.getColor("Table.focusCellBackground");
							if (col != null) {
								button.setBackground(col);
							}
						}
					} else {
						button.setBorder(new EmptyBorder(1, 1, 1, 1));
					}
					
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// TODO
							System.out.println("bababab");
						}
					});

//					MouseEvent buttonEvent = (MouseEvent) SwingUtilities.convertMouseEvent(table, e, button);
//					button.dispatchEvent(buttonEvent);

					return button;
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = table.getSelectedRow();
				int column = table.getSelectedColumn();
				if (column == 3) {
					// TODO
					showInnerArchive(row);	//(String) table.getValueAt(row, 0);
				}
				
			}
		});
		
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

		// button panel at bottom
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		_loadButton = new JButton("load all inner archives");
		_loadButton.setEnabled(false);
		_loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO
				int len = _tableModel.getRowCount();
				for (int i = len - 1; i >= 0; i--) {	//operate from last row
					showInnerArchive(i);
					_loadButton.setEnabled(false);
				}
			}
		});
		bottomPanel.add(_loadButton);
		
		_md5Button = new JButton("Check Md5 sums");
		_md5Button.setEnabled(false);
		_md5Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startCompare(_files[0], _files[1], true);
			}
		});
		bottomPanel.add(_md5Button);
		
		_exportButton = new JButton("Export");
		_exportButton.setEnabled(false);
		_exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportTable(_tableModel);
			}
		});
		bottomPanel.add(_exportButton);
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		bottomPanel.add(closeButton);
		
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		return mainPanel;
	}

	/**
	 * Start the comparison process by prompting for two files
	 */
	public void startCompare()
	{
		startCompare(null, null, false);
	}
	
	/**
	 * Start the comparison using the two specified files
	 * @param inFile1 first file
	 * @param inFile2 second file
	 * @param inMd5 true to check Md5 sums as well
	 */
	public void startCompare(File inFile1, File inFile2, boolean inMd5)
	{
		// Clear table model
		_tableModel.reset();

		File file1 = inFile1;
		File file2 = inFile2;
		if (file1==null || !file1.exists() || !file1.canRead()) {
			JOptionPane.showMessageDialog(_mainWindow, "The first file is not corect!\n"
					+ "Please choose the old file '",
					"Please choose file", JOptionPane.ERROR_MESSAGE);

		}
		// Bail if cancel pressed
		if (file1 == null) {return;}
		// Select second file if necessary
		if (file2 == null || !file2.exists() || !file2.canRead()) {
			JOptionPane.showMessageDialog(_mainWindow, "The second file is not corect!\n"
					+ "Please choose the new file '",
					"Please choose file", JOptionPane.ERROR_MESSAGE);

		}
		// Bail if cancel pressed
		if (file2 == null) {return;}
		_files[0] = file1;
		_files[1] = file2;

		// Clear displays
//		_detailsDisplays[0].clear();
//		_detailsDisplays[1].clear();
		_statusLabel.setText(" comparing...");

		// Start separate thread to compare files
		_checkMd5 = inMd5;
		new Thread(new Runnable() {
			public void run() {
				doCompare();
			}
		}).start();
	}

	/**
	 * Start the comparison using the two specified files
	 * @param inFile1 first file
	 * @param inFile2 second file
	 * @param inMd5 true to check Md5 sums as well
	 */
	public void chooseFile(File inFile1, File inFile2, boolean isOldFile)
	{
		// Clear table model
		_tableModel.reset();
		File file1 = inFile1;
		File file2 = inFile2;
		if (isOldFile) {
			file1 = selectFile("Select first file", null);
//			if (file1 == null || !file1.exists() || !file1.canRead()) {
//			}
			// Bail if cancel pressed
			if (file1 == null) {
				return;
			}
			_files[0] = file1;
			_detailsDisplays[0].setContents(file1, null, 0);
		} else {
			// Select second file if necessary
			file2 = selectFile("Select second file", file1);
//			if (file2 == null || !file2.exists() || !file2.canRead()) {
//			}
			// Bail if cancel pressed
			if (file2 == null) {
				return;
			}
			_files[1] = file2;
			_detailsDisplays[1].setContents(file2, null, 0);
		}
		// Clear displays
//		_statusLabel.setText(" comparing...");

		// Start separate thread to compare files
//		_checkMd5 = inMd5;
//		new Thread(new Runnable() {
//			public void run() {
//				doCompare();
//			}
//		}).start();
	}

	/**
	 * Compare method, to be done in separate thread
	 */
	private void doCompare()
	{
		CompareResults results = new Comparer(_files[0], _files[1], _checkMd5).getResults();
		_tableModel.setEntryList(results.getEntryList());
		final boolean archivesDifferent = (results.getStatus() == EntryDetails.EntryStatus.CHANGED_SIZE);
		if (archivesDifferent) {
			_statusLabel.setText(" Archives have different size (" + results.getSize(0) + ", " + results.getSize(1) + ")");
		}
		else {
			_statusLabel.setText(" Archives have the same size (" + results.getSize(0)+ ")");
		}
		_detailsDisplays[0].setContents(_files[0], results, 0);
		_detailsDisplays[1].setContents(_files[1], results, 1);
		// System.out.println(_files[0].getName() + " has " + results.getNumFiles(0) + " files, "
		//	+ _files[1].getName() + " has " + results.getNumFiles(1));
		if (results.getEntriesDifferent()) {
			_statusLabel2.setText(" " + (archivesDifferent?"and":"but") + " the files have different contents");
		}
		else {
			if (results.getEntriesMd5Checked()) {
				_statusLabel2.setText(" " + (archivesDifferent?"but":"and") + " the files have exactly the same contents");
			}
			else {
				_statusLabel2.setText(" " + (archivesDifferent?"but":"and") + " the files appear to have the same contents");
			}
		}
		_md5Button.setEnabled(!results.getEntriesMd5Checked());
		_checkMd5 = false;
		_refreshButton.setEnabled(true);
		_exchangeButton.setEnabled(true);
		_cacheButton.setEnabled(true);
		_filterBox.setEnabled(true);
		_exportButton.setEnabled(true);
		_loadButton.setEnabled(true);
		_oldFileChoose.setEnabled(true);
		_newFileChoose.setEnabled(true);
		// Possibilities:
		//      Jars have same size, same md5 sum, same contents
		//      Jars have same size but different md5 sum, different contents
		//      Jars have different size, different md5 sum, but same contents
		//      Individual files have same size but different md5 sum
		//      Jars have absolutely nothing in common

		// Maybe poll each minute to check if last modified has changed, then prompt to refresh?
	}


	/**
	 * Select a file for the comparison
	 * @param inTitle title of dialog
	 * @param inFirstFile File to compare selected file with (or null)
	 * @return selected File, or null if cancelled
	 */
	private File selectFile(String inTitle, File inFirstFile)
	{
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setFileFilter(new GenericFileFilter("Jar files, Zip files and War files", new String[] {"jar", "zip", "war"}));
		}
		_fileChooser.setDialogTitle(inTitle);	//already reset title
		File file = null;
		boolean rechoose = true;
		while (rechoose)
		{
			file = null;
			rechoose = false;
			int result = _fileChooser.showOpenDialog(_mainWindow);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				file = _fileChooser.getSelectedFile();
				rechoose = (!file.exists() || !file.canRead());
			}
			// Check it's not the same as the first file, if any
			if (inFirstFile != null && file != null && file.equals(inFirstFile))
			{
				JOptionPane.showMessageDialog(_mainWindow, "The second file is the same as the first file!\n"
					+ "Please select another file to compare with '" + inFirstFile.getName() + "'",
					"Two files equal", JOptionPane.ERROR_MESSAGE);
				rechoose = true;
			}
		}
		return file;
	}
	
	private void exportTable(TableModel model) {
		File file = new File("diff.xls");
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String extj[] = { "xls" };
			FileFilter filter = new FileNameExtensionFilter("MicroSoft Excel (*.xls)", extj);
			chooser.setFileFilter(filter);

			chooser.setSelectedFile(file);
			int retval = chooser.showSaveDialog(_mainWindow);
			if (retval == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			}

			FileWriter out;
			out = new FileWriter(file);
			int columns = model.getColumnCount() - 1;
			for (int i = 0; i < columns; i++) {
				out.write(model.getColumnName(i) + "\t");
			}
			out.write("\n");
			for (int i = 0; i < model.getRowCount(); i++) {
				for (int j = 0; j < columns; j++) {
					out.write(model.getValueAt(i, j).toString() + "\t");
				}
				out.write("\n");
			}
			out.close();
			JOptionPane.showMessageDialog(_mainWindow, "Export the different result successed: " + file.getAbsolutePath(),
					"Export Success", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(_mainWindow, "Export the different result failed: " + file.getAbsolutePath(),
					"Export Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void deleteFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					this.deleteFile(files[i]);
				}
			}
			if (file.delete()) {
				System.out.println("Delete Cache File: " + file.getAbsolutePath());
			}
		} else {
			System.out.println("File does not exist: " + file.getAbsolutePath());
		}
	}

	private void showInnerArchive(final int row) {
		final EntryDetails entry = _tableModel.getEntry(row);
		if (!entry.showInnerArchive()) {
			return;
		}
		final String name = entry.getName();
		entry.setName(name + " [Loading]");
		table.updateUI();
		new Thread(){
			@Override
			public void run() {
				try {
					CompareResults results = new Comparer(
							extractInnerArchive(_files[0], name),
							extractInnerArchive(_files[1], name),
							_checkMd5,
							" [" + name + "] "
							).getResults();
					
					_tableModel.addRow(row + 1, results.getEntryList());
					
				} catch (ZipException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
//				JButton button = (JButton) table.getValueAt(row, 3);
//				button.setText("Loaded");
//				button.setEnabled(false);
//				table.setValueAt("loaded", row, 3);
//				_tableModel.setValueAt("loaded", row, 3);
//				
//				table.setValueAt(table.getValueAt(row, 0) + " [loaded]", row, 0);
//				_tableModel.setValueAt(table.getValueAt(row, 0) + " [loaded]", row, 0);
//				
//				entry.setName(table.getValueAt(row, 0) + " [loaded]");
				
				entry.setName(name + " [loaded]");
				table.updateUI();
				super.run();
			}
		}.start();
	}
	
	private File extractInnerArchive(File archiveFile, String innerEntryName) throws IOException {
		ZipFile zipFile = new ZipFile(archiveFile);
		ZipEntry entry = zipFile.getEntry(innerEntryName);
		
		BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
		File innerEntryFile = new File(archiveFile.getCanonicalPath() + "_tmp" + File.separatorChar
				+ innerEntryName.replace('/', File.separatorChar));
		if (innerEntryFile.exists()) {
			System.out.println("Temporary file already exists: "
					+ innerEntryFile.getAbsolutePath());
			zipFile.close();
			return innerEntryFile;
		}
		
		File parent = innerEntryFile.getParentFile();
		if (parent != null && !parent.exists()) {
			if (parent.mkdirs()){
				System.out.println("Create temporary file: " + parent.getAbsolutePath());
			}
		}

		FileOutputStream fos = new FileOutputStream(innerEntryFile);
		int BUFFER = 2048;
		BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
		int count;
		byte data[] = new byte[BUFFER];
		while ((count = bis.read(data, 0, BUFFER)) != -1) {
			bos.write(data, 0, count);
		}
		bos.flush();
		bos.close();
		bis.close();
		
		zipFile.close();
		return innerEntryFile;
	}
}
