The script bulk_upload_sax.py is derived from bulk_upload.py by Yann Coupin <yann@coupin.net>
in July 2009 and has been used for the Corine Land Cover France import.

The main change is about the XML parsing done by SAX and not by DOM parsing. The DOM method loads 
the whole XML file in memory which is not possible for big files (e.g. 1.4 GB uncompressed 
XML file for Corine). 
The second change is the storage of the old_id's, new_id's table in the *.osm.db file with the
Python "shelve" persistence library, also for performance reasons.

Pieren3@gmail.com on behalf of Yann Coupin