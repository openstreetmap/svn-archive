/*
    This file is part of extract-polygons.

    extract-polygons is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    extract-polygons is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with extract-polygons.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <stdio.h>
#include <string.h>
#include <libxml/xmlreader.h>
#include <libxml/xmlwriter.h>
#include <bzlib.h>

#include "hashtable.h"
#include "table.h"
#include "polygon.h"
#include "extract-polygon.h"

#ifdef LIBXML_READER_ENABLED

#define OSM_VERSION	"0.6"

void die(char *message) {
    fprintf(stderr, "%s\n", message);
    exit(-1);
}

int bz2InputReadCallback(void *context, char *buffer, int len) {
    int bzerr;
    
    return BZ2_bzRead (&bzerr, (BZFILE *)context, buffer, len);
}

int bz2InputCloseCallback(void *context) {
    int bzerr;
    
    BZ2_bzReadClose(&bzerr, (BZFILE *)context);
    return bzerr == BZ_OK;
}

xmlTextReaderPtr myXmlReaderForFile(const char *filename) {
    xmlTextReaderPtr reader = NULL;
    
    // Try BZ2
    size_t len = strlen(filename);
    if (len > 4) {
	if (strcmp(filename+len-4, ".bz2") == 0) {
	    int nUnused = 0;
	    char unused[BZ_MAX_UNUSED];
	    int bzerr;
	    
	    FILE *f = fopen(filename, "r");
	    BZFILE* bzf = BZ2_bzReadOpen(&bzerr, f, 0, 0, unused, nUnused);
	    if (bzf == NULL || bzerr != BZ_OK) {
		die("Failed to uncompress bz2 file");
	    }
	    reader = xmlReaderForIO(bz2InputReadCallback, bz2InputCloseCallback, bzf, "", NULL, 0);
	}
    }
    
    // Try normal file
    if (reader == NULL) {
	reader = xmlReaderForFile(filename, NULL, 0);
    }
    
    if (reader == NULL) {
	die("Unable to open XML file");
    }
    
    return reader;
}

void copyAttributes(xmlTextReaderPtr reader, xmlTextWriterPtr writer) {
    const xmlChar *name, *value;

    xmlTextReaderMoveToFirstAttribute(reader);
    do {
	name = xmlTextReaderConstName(reader);
	value = xmlTextReaderConstValue(reader);

	xmlTextWriterWriteAttribute(writer, name, value);
    } while (xmlTextReaderMoveToNextAttribute(reader));
    xmlTextReaderMoveToElement(reader);
}

static void cutFile(const char *inFilename, const char *outFilename, struct hashtable *includeNodes, struct hashtable *includeWays, struct hashtable *includeRelations) {
    xmlTextReaderPtr reader;
    xmlTextWriterPtr writer;
    int ret, rc;

    reader = myXmlReaderForFile(inFilename);

    writer = xmlNewTextWriterFilename(outFilename, 0);
    if (writer == NULL) {
	die("Error creating XML writer");
    }
    
    xmlTextWriterSetIndent(writer, 1);

    rc = xmlTextWriterStartDocument(writer, NULL, "UTF-8", NULL);
    if (rc < 0) {
	die("Error starting XML document");
    }

    rc = xmlTextWriterStartElement(writer, BAD_CAST "osm");
    if (rc < 0) {
	die("Error creating XML element");
    }

    rc = xmlTextWriterWriteAttribute(writer, BAD_CAST "version",
                                 BAD_CAST OSM_VERSION);
    if (rc < 0) {
	die("Error creating XML attribute");
    }
    
    rc = xmlTextWriterWriteAttribute(writer, BAD_CAST "generator",
                                 BAD_CAST "extract-polygons.c");
    if (rc < 0) {
	die("Error creating XML attribute");
    }
    
    const xmlChar *name;
    int copy = 1;

    do {
	ret = xmlTextReaderRead(reader);
	if (ret != 1) {
	    break;
	}
	name = xmlTextReaderConstName(reader);

	if (copy && xmlTextReaderNodeType(reader) == XML_READER_TYPE_END_ELEMENT) {
	    rc = xmlTextWriterEndElement(writer);
	    if (rc < 0) {
		die("Error creating XML element");
	    }
	    continue;
	}

	if (strcmp((char *)name, "node") == 0) {
	    xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"id");
	    if (table_get(includeNodes, atoi((char *)att))) {
	        copy = 1;
	        rc = xmlTextWriterStartElement(writer, BAD_CAST "node");
	        if (rc < 0) {
		    die("Error creating XML element");
	    	}

		copyAttributes(reader, writer);
	    } else {
		copy = 0;
	    }
	    free(att);
	} else if (strcmp((char *)name, "way") == 0) {
	    xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"id");
	    if (table_get(includeWays, atoi((char *)att))) {
		copy = 1;
		rc = xmlTextWriterStartElement(writer, BAD_CAST "way");
		if (rc < 0) {
		    die("Error creating XML element");
	    	}

		copyAttributes(reader, writer);
	    } else {
		copy = 0;
	    }
	    free(att);
	} else if (strcmp((char *)name, "relation") == 0) {
	    xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"id");
	    if (table_get(includeRelations, atoi((char *)att))) {
	        copy = 1;
	        rc = xmlTextWriterStartElement(writer, BAD_CAST "relation");
	        if (rc < 0) {
		    die("Error creating XML element");
	    	}

		copyAttributes(reader, writer);
	    } else {
	        copy = 0;
	    }
	    free(att);
	} else if (strcmp((char *)name, "bound") == 0) {
	    rc = xmlTextWriterStartElement(writer, BAD_CAST "bound");
	    if (rc < 0) {
	        die("Error creating XML element");
	    }
	    copyAttributes(reader, writer);
	} else if (copy) {
	    if (strcmp((char *)name, "nd") == 0 ||
	    	    strcmp((char *)name, "tag") == 0 ||
		    strcmp((char *)name, "member") == 0) {
		rc = xmlTextWriterStartElement(writer, BAD_CAST xmlTextReaderConstName(reader));
		if (rc < 0) {
		    die("Error creating XML element");
	    	}
		
		copyAttributes(reader, writer);
		
		rc = xmlTextWriterEndElement(writer);
		if (rc < 0) {
		    die("Error ending XML element");
		}
		continue;
	    } else if (xmlTextReaderIsEmptyElement(reader)) {
		rc = xmlTextWriterEndElement(writer);
		if (rc < 0) {
		    die("Error ending XML element");
		}
		continue;
	    }
	}
	
	if (copy) {
	    if (xmlTextReaderIsEmptyElement(reader)) {
		rc = xmlTextWriterEndElement(writer);
		if (rc < 0) {
		    die("Error ending XML element");
		}
	    }
	}
    } while (ret == 1);

    rc = xmlTextWriterEndDocument(writer);
    if (rc < 0) {
	die("Error ending XML document");
    }

    xmlFreeTextReader(reader);
    xmlFreeTextWriter(writer);
	    
    if (ret != 0) {
	die("XML parse error");
    }
}

static void fillFromUp(struct hashtable *down, struct hashtable *up, int *nodes, int nodesPos, int id) {
    for (int i = 0 ; i < nodesPos ; i++) {
    	if (table_get(down, nodes[i])) {
    	    table_set(up, id);
	    for (int j = 0 ; j < nodesPos ; j++) {
		table_set(down, nodes[j]);
	    }
	    break;
	}
    }
}

static int fillIncludes(const char *inFilename, struct hashtable *includeNodes, struct hashtable *includeWays, struct hashtable *includeRelations, Tpolygon *polygon) {
    xmlTextReaderPtr reader;
    int ret;
    int restart = 0;
    int status = 0;

    reader = myXmlReaderForFile(inFilename);
    
    const xmlChar *name;
    int type;
    int* nodes;
    int nodesPos = 0;
    int nodesSize = 64;
    nodes = malloc(nodesSize * sizeof(int));
    if (nodes == NULL) {
        die("Can't allocated memory!");
    }

    do {
	ret = xmlTextReaderRead(reader);
	if (ret != 1) {
	    break;
	}
	    
	type = xmlTextReaderNodeType(reader);
	if (type != XML_READER_TYPE_ELEMENT && type != XML_READER_TYPE_END_ELEMENT) {
	    continue;
	}

	name = xmlTextReaderConstName(reader);
	
	if (strcmp((char *)name, "tag") == 0) {
	} else if (strcmp((char *)name, "node") == 0) {
	    if (status > 1) {
		restart = 1;
		printf("Non linear OSM file, two phase reading\n");
	    } else if (status != 1) {
		status = 1;
		printf("Loading nodes...\n");
	    }
	    if (type == XML_READER_TYPE_ELEMENT) {
		xmlChar *att;
		att = xmlTextReaderGetAttribute(reader, (xmlChar *)"lat");
		float lat = atof((char *)att);
		free(att);
		att = xmlTextReaderGetAttribute(reader, (xmlChar *)"lon");
		float lon = atof((char *)att);
		free(att);
		if (polygon_pointInside(polygon, lat, lon)) {
		    att = xmlTextReaderGetAttribute(reader, (xmlChar *)"id");
	    	    table_set(includeNodes, atoi((char *)att));
		    free(att);
		}
	    }
	} else if (strcmp((char *)name, "nd") == 0) {
	    if (type == XML_READER_TYPE_ELEMENT) {
		xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"ref");
	        nodes[nodesPos++] = atoi((char *)att);
		free(att);
		if (nodesPos == nodesSize) {
	    	    nodesSize *= 2;
		    nodes = realloc(nodes, nodesSize * sizeof(int));
		    if (nodes == NULL) {
			die("Can't allocated memory!");
		    }
		}
	    }
	} else if (strcmp((char *)name, "way") == 0) {
	    if (status > 2) {
		restart = 1;
		printf("Non linear OSM file, two phase reading\n");
	    } else if (status != 2) {
		status = 2;
		printf("Loading ways...\n");
	    }
	    if (type == XML_READER_TYPE_ELEMENT) {
		nodesPos = 0;
	    } else {
		xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"id");
	        fillFromUp(includeNodes, includeWays, nodes, nodesPos, atoi((char *)att));
		free(att);
	    }
	} else if (strcmp((char *)name, "relation") == 0) {
	    if (status > 3) {
		restart = 1;
		printf("Non linear OSM file, two phase reading\n");
	    } else if (status != 3) {
		status = 3;
		printf("Loading relations...\n");
	    }
	    if (type == XML_READER_TYPE_ELEMENT) {
		nodesPos = 0;
	    } else {
		xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"id");
	        fillFromUp(includeWays, includeRelations, nodes, nodesPos, atoi((char *)att));
		free(att);
	    }
	} else if (strcmp((char *)name, "member") == 0) {
	    if (type == XML_READER_TYPE_ELEMENT) {
		xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"ref");
		nodes[nodesPos++] = atoi((char *)att);
		free(att);
		if (nodesPos == nodesSize) {
		    nodesSize *= 2;
		    nodes = realloc(nodes, nodesSize * sizeof(int));
		    if (nodes == NULL) {
		        die("Can't allocated memory!");
		    }
		}
	    }
	} else if (strcmp((char *)name, "bound") == 0) {
	} else if (strcmp((char *)name, "osm") == 0) {
	    xmlChar *att = xmlTextReaderGetAttribute(reader, (xmlChar *)"version");
	    if (strcmp((char *)att, OSM_VERSION) != 0) {
		free(att);
		die("OSM version mismatch");
	    }
	    free(att);
	} else {
	    die("Unknown XML element name");
	}
    } while (ret == 1);
	
    xmlFreeTextReader(reader);
    if (ret != 0) {
        die("XML parse error");
    }
    
    return restart;
}

int main(int argc, char **argv) {
    if (argc != 4) {
	printf("Usage: extract-polygon <inputFile> <outputFile> <polygonFile>\n");
        return(1);
    }

    LIBXML_TEST_VERSION

    struct hashtable *includeNodes;
    struct hashtable *includeWays;
    struct hashtable *includeRelations;
    Tpolygon polygon;

    includeNodes = table_init();
    includeWays = table_init();
    includeRelations = table_init();

    printf("Loading polygon...\n");
    polygon_load(&polygon, argv[3]);
    if (fillIncludes(argv[1], includeNodes, includeWays, includeRelations, &polygon)) {
	// Non linear OSM file, do it again
	fillIncludes(argv[1], includeNodes, includeWays, includeRelations, &polygon);
    }
    printf("Cutting file...\n");
    cutFile(argv[1], argv[2], includeNodes, includeWays, includeRelations);

    table_destruct(includeNodes);
    table_destruct(includeWays);
    table_destruct(includeRelations);

    xmlCleanupParser();

    xmlMemoryDump();
    return(0);
}

#else
int main(void) {
    fprintf(stderr, "XInclude support not compiled in\n");
    exit(1);
}
#endif
