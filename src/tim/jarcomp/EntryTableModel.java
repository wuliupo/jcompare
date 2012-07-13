package tim.jarcomp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import tim.jarcomp.EntryDetails.EntryStatus;

/**
 * Class to hold the table model for the comparison table
 */
public class EntryTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = -6859610286826628702L;
	/** list of entries */
	private List<EntryDetails> _entries = null;
	
	/**
	 * filter, "show All", "only Add", "only Delete", "checksum", "size change", "only equal", "same size"
	 */
	protected static String filter = "show All";
	protected static String[] columnNames = new String[]{"Filename", "Status", "Size Change", "Operation"};

	/**
	 * Clear list to start a new comparison
	 */
	public void reset() {
		_entries = new ArrayList<EntryDetails>();
	}

	/**
	 * Reset the table with the given list
	 * @param inList list of EntryDetails objects
	 */
	public void setEntryList(List<EntryDetails> inList)
	{
		//,"show All", "only Add", "only Delete", "checksum", "size change", "only equal", "same size"
		if (filter == null || "".equals(filter) || "show All".equals(filter)) {
			_entries = inList;
		} else if ("only Add".equals(filter)){
			filterList(inList, EntryDetails.EntryStatus.ADDED);
		} else if ("only Delete".equals(filter)){
			filterList(inList, EntryDetails.EntryStatus.REMOVED);
		} else if ("checksum".equals(filter)){
			filterList(inList, EntryDetails.EntryStatus.CHANGED_SUM);
		} else if ("size change".equals(filter)){
			filterList(inList, EntryDetails.EntryStatus.CHANGED_SIZE);
		} else if ("only equal".equals(filter)){
			filterList(inList, EntryDetails.EntryStatus.EQUAL);
		} else if ("same size".equals(filter)){
			filterList(inList, EntryDetails.EntryStatus.SAME_SIZE);
		} else if ("Only folder".equals(filter)){
			for (EntryDetails e : inList) {
				if(e.getName().endsWith("/")) {
					_entries.add(e);
				}
			}
		} else {
			_entries = inList;
		}
		fireTableDataChanged();
	}

	private void filterList(List<EntryDetails> inList, EntryDetails.EntryStatus status) {
		_entries = new ArrayList<EntryDetails>();
		for (EntryDetails e : inList) {
			if(e.getStatus() == status) {
				_entries.add(e);
			}
		}
	}

	/**
	 * @return number of columns in table
	 */
	public int getColumnCount() {
		return columnNames.length;
		// TODO: Columns for size1, size2, status (as icon), size difference
	}

	/**
	 * @return column name
	 */
	public String getColumnName(int inColNum) {
		return columnNames[inColNum];
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
		case 1:
		case 2:
		case 3:
			return JButton.class;
		}
		return String.class;
	}

	/**
	 * @return number of rows in the table
	 */
	public int getRowCount()
	{
		if (_entries == null) {return 0;}
		return _entries.size();
	}

	public EntryDetails getEntry(int i)
	{
		if (_entries == null) {return null;}
		return _entries.get(i);
	}

	/**
	 * @return object at specified row and column
	 */
	public Object getValueAt(int inRowNum, int inColNum) {
		if (inRowNum >= 0 && inRowNum < getRowCount()) {
			EntryDetails entry = _entries.get(inRowNum);
			if (inColNum == 0) {
				return getRichName(entry.getNamePrefix() + entry.getName(), entry.getStatus());
			} else if (inColNum == 1) {
				return getText(entry.getStatus());
			} else if (inColNum == 2) {
				return new Long(entry.getSizeDifference()).toString();
			} else if (inColNum == 3) {
				if (entry.showInnerArchive()) {
					JButton loadButton = new JButton("load");
					loadButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// TODO
							System.out.println("hahah");
						}
					});
					return loadButton;
				}
			}
		}
		return null;
	}

	private String getRichName(String name, EntryStatus inStatus){
		String subfix = " ? ";
		switch (inStatus) {
			case ADDED: subfix = " + "; break;
			case CHANGED_SIZE: subfix = " ~ "; break;
			case CHANGED_SUM: subfix = " ~ "; break;
			case EQUAL: subfix = name.endsWith("/") ? " > " : " = "; break;
			case REMOVED: subfix = " - "; break;
			case SAME_SIZE: subfix = name.endsWith("/") ? " > " : " ? "; break;
		}
		
		int len = name.split("/").length;
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i < len; i++){
			sb.append(subfix);
		}
		return sb.append(name).toString();
	}

	/**
	 * Convert an entry status into text
	 * @param inStatus entry status
	 * @return displayable text
	 */
	private static String getText(EntryStatus inStatus)
	{
		switch (inStatus) {
			case ADDED: return "Added";
			case CHANGED_SIZE: return "Changed size";
			case CHANGED_SUM: return "Changed sum";
			case EQUAL: return "=";
			case REMOVED: return "Removed";
			case SAME_SIZE: return "Same size";
		}
		return inStatus.toString();
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		//TODO
		//System.out.println("addTableModelListener " + l);
		super.addTableModelListener(l);
	}
	
	protected void addRow(int index, EntryDetails ed){
		_entries.add(index, ed);
	}
	
	protected void addRow(int index, List<EntryDetails> list){
		_entries.addAll(index, list);
	}
}
