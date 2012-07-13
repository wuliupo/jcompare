package tim.jarcomp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.tools.crypt.Md5;

/**
 * Class to do the actual comparison of jar files,
 * populating a list of EntryDetails objects
 */
public class Comparer
 {

	private List<EntryDetails> entryList = new ArrayList<EntryDetails>();
	private File inFile1;
	private File inFile2;
	private boolean inMd5 = false;
	private String namePrefix = "";
	
	public Comparer(File inFile1, File inFile2) {
		this.inFile1 = inFile1;
		this.inFile2 = inFile2;
	}
	
	public Comparer(File inFile1, File inFile2, boolean inMd5) {
		this.inFile1 = inFile1;
		this.inFile2 = inFile2;
		this.inMd5 = inMd5;
	}

	public Comparer(File inFile1, File inFile2, boolean inMd5, String namePrefix) {
		this.inFile1 = inFile1;
		this.inFile2 = inFile2;
		this.inMd5 = inMd5;
		this.namePrefix = namePrefix;
	}
	/**
	 * Compare the two given files and return the results
	 * @param inFile1 first file
	 * @param inFile2 second file
	 * @param inMd5 true to also check md5 sums
	 * @return results of comparison
	 */
	public CompareResults getResults()
	{
		// Make results object and compare file sizes
		CompareResults results = new CompareResults();
		results.setSize(0, inFile1.length());
		results.setSize(1, inFile2.length());
		// Make empty list
		// load first file, make entrydetails object for each one
		final int numFiles1 = makeEntries(inFile1, 0);
		results.setNumFiles(0, numFiles1);
		// load second file, try to find entrydetails for each file or make new one
		final int numFiles2 = makeEntries(inFile2, 1);
		results.setNumFiles(1, numFiles2);

		Collections.sort(entryList, new Comparator<EntryDetails>() {
			@Override
			public int compare(EntryDetails o1, EntryDetails o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
				//TODO
			}
		});
		
		results.setEntryList(entryList);

		// Check md5 sums if necessary
		if (inMd5) {
			calculateMd5(inFile1, 0);
			calculateMd5(inFile2, 1);
		}
		return results;
	}

	// TODO: Maybe we need to add an option to ignore path, just look at filenames?

	/**
	 * Make entrydetails objects for each entry in the given file and put in list
	 * @param inList list of entries so far
	 * @param inFile zip/jar file to search through
	 * @param inIndex 0 for first file, 1 for second
	 * @return number of files found
	 */
	private int makeEntries(File inFile, int inIndex)
	{
		boolean checkList = (entryList.size() > 0);
		int numFiles = 0;
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(inFile);
			Enumeration<?> zipEntries = zip.entries();
			while (zipEntries.hasMoreElements())
			{
				ZipEntry ze = (ZipEntry) zipEntries.nextElement();
				numFiles++;
				String name = ze.getName();
				EntryDetails details = null;
				if (checkList) {details = getEntryFromList(name);}
				// Construct new details object if necessary
				if (details == null)
				{
					details = new EntryDetails();
					details.setName(name);
					details.setNamePrefix(namePrefix);
					entryList.add(details);
				}
				// set size
				details.setSize(inIndex, ze.getSize());
			}
		} catch (IOException ioe) {
			System.err.println("Ouch: " + ioe.getMessage());
		} finally {
			try {
				zip.close();
			} catch (Exception e) {
			}
		}
		return numFiles;
	}

	/**
	 * Look up the given name in the list
	 * @param inList list of EntryDetails objects
	 * @param inName name to look up
	 */
	private EntryDetails getEntryFromList(String inName)
	{
		EntryDetails details = null;
		for (int i=0; i<entryList.size(); i++)
		{
			details = entryList.get(i);
			//TODO, name ignore
			if (details.getName() != null && details.getName().equals(inName)) {
				return details;
			}
		}
		return null;
	}

	/**
	 * Calculate the md5 sums of all relevant entries
	 * @param inResults results from preliminary check
	 * @param inFile file to read
	 * @param inIndex 0 or 1
	 */
	private void calculateMd5(File inFile, int inIndex)
	{
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(inFile);
			for (int i=0; i<entryList.size(); i++)
			{
				EntryDetails entry = entryList.get(i);
				if (entry.getStatus() == EntryDetails.EntryStatus.SAME_SIZE)
				{
					// Must be present in both archives if size is the same
					ZipEntry zipEntry = zip.getEntry(entry.getName());
					if (zipEntry == null) {
						System.err.println("zipEntry for " + entry.getName() + " shouldn't be null!");
					}
					Md5 hasher = new Md5(zip.getInputStream(zipEntry));
					byte[] digest = hasher.getDigest();
					if (digest != null) {
						String hash = hasher.getStringDigest();
						// System.out.println("Calculated md5 sum for " + entry.getName() + " - '" + hash + "'");
						entry.setMd5Sum(inIndex, hash);
					}
				}
			}
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
		finally {
			try {zip.close();} catch (Exception e) {}
		}
	}
}
