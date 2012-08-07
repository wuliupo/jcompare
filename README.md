JCompare Introductions
=============

This is Compare Tool.
It is a compare tool, compare folders, jars, wars, zips and their inner-inner files. 

Use scenario
-------
If we have two zipped files, it will show what the differences are in the contents. It shows which files have been added, which have been removed, and which are present in both archives. If a file is found in both, it will tell you whether the file has grown bigger or been reduced in size, or if it's the same size in both. If it's the same size, an md5 checksum can be made for both files to see whether they're really the same contents or just the same size.

1.  Find out customized pages, portlets and all modified files. They can upgrade to new version more smoothly.
2.  Backup instances more quickly, only the differences.
3.  Statics user usage, which pages/portlets are modified frequently, then we can add more public APIs.
4.  Refer to the compare result, we will release new product bundles: the clean zip + fewer configuration files. Then product will be portable.
5.  The different between 9140001, 9140002, 9140003 and other versions
6.  …

Usage
------------
You can also run it from the command line, just by giving the path to the jarcomp.jar file:
java -jar jarcomp_01.jar

Or you can also give it the two files to compare straight away:
java -jar jarcomp_01.jar file1.jar file2.jar


Reference
------------

1.  [activityworkshop jarcomp][jarcomp]
2.  [extradata jarc][jarc]
3.  [Java API Compliance Checker][ispras]
4.  [github pkgdiff][pkgdiff]
5.  [sourceforge clirr][clirr]

[jarcomp]: http://activityworkshop.net/software/jarcomp/index.html
[jarc]: http://extradata.com/products/jarc/
[ispras]: http://ispras.linuxbase.org/index.php/Java_API_Compliance_Checker
[pkgdiff]: http://pkgdiff.github.com/pkgdiff/
[clirr]: http://clirr.sourceforge.net/