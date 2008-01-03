/*
#-----------------------------------------------------------------------------
# planetdiff - Calculates the differences between 2 planet.osm
# Roughly equivalent to 'diff' but optimised for planet.osm files
#
# Use:
#      planetdiff planetA.osm planetB.osm > delta-AB.xml
#
#      planetpatch planetA.osm delta-AB.xml > planetB.osm
#
# One of the input files may come from STDIN by specifying - as the filename
# Files can be gzip or bzip2 compressed.
#
#-----------------------------------------------------------------------------
# Written by Jon Burgess, Copyright 2007
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------
*/

/* TODO:
 * - Empty!
 *
 * DONE:
 * Escape key/values on output (&"'<>)
 * write patch tool
 * Error if file not in required sequence (node/way/relation with increasing ID)
 * Warn if generator is not planet.rb since others tend to violate ordering above
 * Generate run-time stats on stderr
 * Allow direct reading of .gz and .bz2 files
 * Perform UTF8sanitize on input
 */

// define VERBOSE if you want run time stats
#undef VERBOSE

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include <libxml/xmlstring.h>
#include <libxml/xmlreader.h>

#include "keyvals.h"
#include "input.h"
#include "sanitizer.h"

/* Since {node,way,relation} elements are not nested we can
 * accumulates the attributes, tags and nodes in global data.
 * This is maintained independently for planetA and planetB
 */
enum type { invalid, node, way, relation, eof } current[2];
static int osm_id[2];
static struct keyval tags[2], nds[2], attrs[2];
static struct keyval member_nodes[2], member_ways[2], member_rels[2];
static xmlTextReaderPtr reader[2];
static const char *files[2];
// Strict mode is considers timestamp and key ordering changes as significant, otherwise they are ignored
static const int strict = 0;
// Do we run UTF8sanitizer on input, this should only be necessary if you are using very old planet dumps.
static const int sanitize = 0;

static int compareTypeID() {
    // Compare object types
    if (current[0] < current[1])
        return -1;
    if (current[0] > current[1])
        return +1;

    if (osm_id[0] < osm_id[1])
        return -1;
    if (osm_id[0] > osm_id[1])
        return +1;

    return 0;
}

static int compareKeyval(struct keyval kv[2])
{
    struct keyval *p[2];
    int r;

    p[0] = kv[0].next;
    p[1] = kv[1].next;
#define END(x) (p[x] == &kv[x])

    while(1) {
        if (END(0) && END(1))
            return 0;
        if (END(0))
            return -1;
        if (END(1))
            return +1;

        r = strcmp(p[0]->key, p[1]->key);
        if (r) return r;

        // Ignore the value of any timstemap= for comparison
        // This skips formatting and other uninteresting timestamp-only updates
        // Note: this triggers on both k="timestamp" as well as timestamp=...
        if (strict || strcmp(p[0]->key, "timestamp")) {
            r = strcmp(p[0]->value, p[1]->value);
            if (r) return r;
        }

        p[0] = p[0]->next;
        p[1] = p[1]->next;
    }
    return 0;
}

int compareKV(const void *hA, const void *hB)
{
    struct keyval * const *a = hA;
    struct keyval * const *b = hB;
    int c;

    c = strcmp((*a)->key, (*b)->key);
    if (c) return c;

    return strcmp((*a)->value, (*b)->value);
}

void sortList(struct keyval *in, struct keyval *out)
{
    struct keyval **t, *p;
    int len = countList(in);
    int i;

    initList(out);
    if (!len)
        return;

    t = calloc(len, sizeof(*in));
    assert(t);

    for (i=0, p = in->next; i<len; i++, p = p->next)
        t[i] = p;

    qsort(t, len, sizeof(*t), compareKV);

    for (i=0; i<len; i++)
        addItem(out, t[i]->key, t[i]->value, 0);

    free(t);
}

static int compareLists(struct keyval kv[2])
{
    // Strict mode requires all lists to be in a consistent order
    // non-strict treats the 2 sets of tags below as equivalent
    //
    // <k='A' v='1' />
    // <k='B' v='2' />
    //
    // <k='B' v='2' />
    // <k='A' v='1' />

    struct keyval clone[2];
    int r;

    if (strict)
        return compareKeyval(kv);

    if (!listHasData(&kv[0]) && !listHasData(&kv[1]))
        return 0;

    sortList(&kv[0], &clone[0]);
    sortList(&kv[1], &clone[1]);
    r = compareKeyval(clone);
    resetList(&clone[0]);
    resetList(&clone[1]);

    return r;
}


static int compareOther(void)
{
    int c;

    c = compareKeyval(attrs);
    if (c) return c;

    c = compareLists(tags);
    if (c) return c;

    c = compareKeyval(nds);
    if (c) return c;

    c = compareLists(member_nodes);
    if (c) return c;

    c = compareLists(member_ways);
    if (c) return c;

    c = compareLists(member_rels);
    if (c) return c;

    return 0;
}

static void collectAttributes(int i)
{
    int ret;
    const xmlChar *name, *value;
    while ((ret = xmlTextReaderMoveToNextAttribute(reader[i])) == 1) {
        name  = xmlTextReaderConstName(reader[i]);
        value = xmlTextReaderConstValue(reader[i]);
        addItem(&attrs[i], (char *)name, (char *)value, 0);
        //fprintf(stderr, "%s = %s\n", (char *)name, (char *)value);
    }
    if (ret != 0) {
        fprintf(stderr, "%s : failed to parse attributes\n", files[i]);
        exit(1);
    }
}

static void StartElement(int i, const xmlChar *name)
{
    xmlChar *xk, *xv, *xid, *xgen, *xrole, *xtype;
    static int warn = 1;

    if (xmlStrEqual(name, BAD_CAST "node")) {
        current[i] = node;
        collectAttributes(i);
    } else if (xmlStrEqual(name, BAD_CAST "tag")) {
        xk = xmlTextReaderGetAttribute(reader[i], BAD_CAST "k");
        xv = xmlTextReaderGetAttribute(reader[i], BAD_CAST "v");
        addItem(&tags[i], (char *)xk, (char *)xv, 0);
        xmlFree(xk);
        xmlFree(xv);
    } else if (xmlStrEqual(name, BAD_CAST "member")) {
        xtype = xmlTextReaderGetAttribute(reader[i], BAD_CAST "type");
        xid   = xmlTextReaderGetAttribute(reader[i], BAD_CAST "ref");
        xrole = xmlTextReaderGetAttribute(reader[i], BAD_CAST "role");
        if (xmlStrEqual(xtype, BAD_CAST "node"))
            addItem(&member_nodes[i], (char *)xid, (char *)xrole, 0);
        else if (xmlStrEqual(xtype, BAD_CAST "way"))
            addItem(&member_ways[i], (char *)xid, (char *)xrole, 0);
        else if (xmlStrEqual(xtype, BAD_CAST "relation"))
            addItem(&member_rels[i], (char *)xid, (char *)xrole, 0);
        else
            fprintf(stderr, "Unknown type: <member type='%s' ...\n", (char *)xtype);
        xmlFree(xtype);
        xmlFree(xid);
        xmlFree(xrole);
    } else if (xmlStrEqual(name, BAD_CAST "way")) {
        current[i] = way;
        collectAttributes(i);
    } else if (xmlStrEqual(name, BAD_CAST "nd")) {
        xid  = xmlTextReaderGetAttribute(reader[i], BAD_CAST "ref");
        addItem(&nds[i], "ref", (char *)xid, 0);
        xmlFree(xid);
    } else if (xmlStrEqual(name, BAD_CAST "relation")) {
        current[i] = relation;
        collectAttributes(i);
    } else if (xmlStrEqual(name, BAD_CAST "osm")) {
        if (warn) {
            xgen = xmlTextReaderGetAttribute(reader[i], BAD_CAST "generator");
            if (!xmlStrEqual(xgen, BAD_CAST "OpenStreetMap planet.rb") && !xmlStrEqual(xgen, BAD_CAST "OpenStreetMap planet.c")) {
                fprintf(stderr, "Warning: The input file %s was generated by: %s\n", files[i], xgen ? (char *)xgen : "unknown");
                fprintf(stderr, "this tool relies on the planet.osm format of the format of the planet.osm file\n");
                fprintf(stderr, "generated by planet.rb of planet.openstreetmap.org and may not work with other osm files.\n\n");
                warn = 0;
            }
            xmlFree(xgen);
        }
    } else if (xmlStrEqual(name, BAD_CAST "bound")) {
        // Ignored
    } else {
        fprintf(stderr, "%s: Unknown element name: %s\n", __FUNCTION__, name);
    }
}

static int EndElement(int i, const xmlChar *name)
{
    // Return 0 for closing tag of node/way/relation
    int ret;
    if (xmlStrEqual(name, BAD_CAST "node")) {
        ret = 0;
    } else if (xmlStrEqual(name, BAD_CAST "way")) {
        ret = 0;
    } else if (xmlStrEqual(name, BAD_CAST "tag")) {
        ret = 1;
    } else if (xmlStrEqual(name, BAD_CAST "member")) {
        ret = 1;
    } else if (xmlStrEqual(name, BAD_CAST "nd")) {
        ret = 1;
    } else if (xmlStrEqual(name, BAD_CAST "relation")) {
        ret = 0;
    } else if (xmlStrEqual(name, BAD_CAST "osm")) {
        ret = 1; // maybe should be 0
    } else if (xmlStrEqual(name, BAD_CAST "bound")) {
        ret = 1;
    } else {
        fprintf(stderr, "%s: Unknown element name: %s\n", __FUNCTION__, name);
        ret = 1;
    }
    return ret;
}

// Process an XML node, returns 0 for the closing tag for a node/segment/way
static int processNode(int i)
{
    int ret = 1;
    int empty;
    xmlChar *name = xmlTextReaderName(reader[i]);
    if (name == NULL)
        name = xmlStrdup(BAD_CAST "--");
	
    switch(xmlTextReaderNodeType(reader[i])) {
        case XML_READER_TYPE_ELEMENT:
            empty = xmlTextReaderIsEmptyElement(reader[i]);
            StartElement(i, name);	
            if (!empty)
                break;
            /* Drop through for self closing tags since these generate no end_element */
        case XML_READER_TYPE_END_ELEMENT:
            ret = EndElement(i, name);
            break;
        case XML_READER_TYPE_SIGNIFICANT_WHITESPACE:
            /* Ignore */
            break;
        default:
            fprintf(stderr, "Unknown node type %d\n", xmlTextReaderNodeType(reader[i]));
            break;
    }
    xmlFree(name);
    return ret;
}

static const char *getName(int i)
{
    switch (current[i]) {
        case node: return "node";
        case way: return "way";
        case relation: return "relation";
        case invalid: return "invalid";
        default:
            break;
    }
    fprintf(stderr, "Unhandled type %d\n", current[i]);
    exit(5);
}

// Reads in a complete OSM object, e.g. <node> to <node/>
static void getobject(int i)
{
    int ret = 1;
    const char *id;
    enum type last_type = current[i];
    const char *last_name = getName(i);
    int last_id = osm_id[i];
    //static int count;

    // Delete data for previous object
    resetList(&attrs[i]);
    resetList(&nds[i]);
    resetList(&tags[i]);
    resetList(&member_nodes[i]);
    resetList(&member_ways[i]);
    resetList(&member_rels[i]);

    current[i] = eof;
    if (!reader[i])
        return; // EOF

    while(ret == 1) {
        ret = xmlTextReaderRead(reader[i]);
        if (ret == 0) {
            //fprintf(stderr, "EOF %s\n", files[i]);
            xmlFreeTextReader(reader[i]);
            reader[i] = NULL;
            return;
        }
        if (ret != 1) {
            fprintf(stderr, "Error parsing file %s\n", files[i]);
            exit(3);
        }

        ret = processNode(i);
    }
    // Retrieve osm_id for node/way/relation
    id = getItem(&attrs[i], "id");
    osm_id[i] = id ? atoi(id) : 0;
    //fprintf(stderr, "%d: object %d, id=%d\n", i, current[i], osm_id[i]);

    // Perform sanity checking on element sequence. The output of planet.rb conforms to this and
    // the current diff/patch algorithm relies on these properties.
    if (current[i] < last_type) {
        fprintf(stderr, "Error: <%s> seen after <%s>. The file must be strictly ordered node/way/relation.\n", getName(i), last_name);
        fprintf(stderr, "The planet.osm generated by the planet export (planet.rb) is consistent with this.\n");
        exit(8);
    }
    if ((current[i] == last_type) && (osm_id[i] < last_id)) {
        fprintf(stderr, "Error: <%s id=%d> seen after <%s id=%d>. The IDs must be in increasing order.\n", getName(i), osm_id[i], getName(i), last_id);
        fprintf(stderr, "The planet.osm generated by the planet export (planet.rb) is consistent with this.\n");
        exit(9);
    }
#ifdef VERBOSE
    // Some run-time stats, only count first stream
    if (i == 0) {
        count++;
        if (current[i] != last_type) {
            count = 0;
            fprintf(stderr, "\n");
        }
        if ((count % 10000) == 0)
            fprintf(stderr, "\rProcessing: %s(%dk)", getName(i), count/1000);
    }
#endif
}


static void displayNode(int i, const char *indent)
{
    struct keyval *p;

    printf("%s<%s", indent,getName(i));

    while ((p = popItem(&attrs[i])) != NULL) {
        printf(" %s=\"%s\"", p->key, p->value);
        freeItem(p);
    }

    if (listHasData(&tags[i]) || listHasData(&nds[i]) || listHasData(&member_nodes[i]) ||
        listHasData(&member_ways[i]) || listHasData(&member_rels[i])) {
        printf(">\n");

        while ((p = popItem(&nds[i])) != NULL) {
            printf("%s  <nd ref=\"%s\" />\n", indent, p->value);
            freeItem(p);
        }
        while ((p = popItem(&member_nodes[i])) != NULL) {
            printf("%s  <member type=\"node\" ref=\"%s\" role=\"%s\" />\n", indent, p->key, p->value);
            freeItem(p);
        }
        while ((p = popItem(&member_ways[i])) != NULL) {
            printf("%s  <member type=\"way\" ref=\"%s\" role=\"%s\" />\n", indent, p->key, p->value);
            freeItem(p);
        }
        while ((p = popItem(&member_rels[i])) != NULL) {
            printf("%s  <member type=\"relation\" ref=\"%s\" role=\"%s\" />\n", indent, p->key, p->value);
            freeItem(p);
        }
        while ((p = popItem(&tags[i])) != NULL) {
            printf("%s  <tag k=\"%s\" v=\"%s\" />\n", indent, p->key, p->value);
            freeItem(p);
        }
        printf("%s</%s>\n", indent,getName(i));
    } else {
        printf("/>\n");
    }
}

static void displayDiff(int i)
{
    const char *operation = i ? "add" : "delete";
    printf("  <%s>\n", operation);
    displayNode(i, "    ");
    printf("  </%s>\n", operation);
}

static void displayPatch(int i)
{
    displayNode(i, "  ");
}


static void process(void)
{
    int c;

    getobject(0);
    getobject(1);

    while (reader[0] || reader[1]) {
        c = compareTypeID();
        if (c == 0) {
            // Matching object type and ID, generate diff if content differs
            if (compareOther()) {
                displayDiff(0);
                displayDiff(1);
            }
            getobject(0);
            getobject(1);
        } else if (c < 0) {
            // Object in stream 0 is missing in 1, generate diff to remove old content 0
            displayDiff(0);
            getobject(0);
        } else {
            // Object in stream 0 is ahead of 1, generate diff to add content in 1
            displayDiff(1);
            getobject(1);
        }
    }
}



static int diffFiles(void)
{
    int i;

    for(i=0; i<2; i++) {
        if (sanitize)
            reader[i] = sanitizerOpen(files[i]);
        else
            reader[i] = inputUTF8(files[i]);
        if (!reader[i]) {
            fprintf(stderr, "Unable to open %s\n", files[i]);
            return 1;
        }
    }

    printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    printf("<planetdiff version=\"0.5\" generator=\"OpenStreetMap planetdiff\" from=\"%s\" to=\"%s\">\n", files[0], files[1]);
    process();
    printf("</planetdiff>\n");

    return 0;
}


static void applyPatch(int mode)
{
    int c;
    //fprintf(stdout,"%s: %d id=%d\n", (mode > 0)? "add" : "delete", current[1], osm_id[1]);

    // Stream input until we get to or pass the matching type+id
    while ((c = compareTypeID()) < 0) {
        if (current[0] != invalid)
            displayPatch(0);
        getobject(0);
    }
    if (mode == -1) {
        if ((c == 0) && (compareOther() == 0)) {
            // Perfect match, remove this from input by fetching next object
            getobject(0);
            return;
        }
        fprintf(stderr, "Error remove for <%s id=%d> does not match input file. %s\n",
                getName(1), osm_id[1], (c==0)?"Object content differs.":"Object ID missing.");
        exit(6);
    } else if (mode == +1) {
        if (c > 0) {
            // Found next input following the insert, emit the new content but not input (yet)
            displayPatch(1);
            return;
        }
        fprintf(stderr, "Error adding <%s id=%d> input file already contains this object\n", getName(1), osm_id[1]);
        exit(6);
    }
}

static void processPatchNode(void)
{
    int i = 1; // Patch file is always stream 1
    int empty;
    int mode = 0; // -1 = remove, +1 = add;
    xmlChar *name = xmlTextReaderName(reader[i]);
    if (name == NULL)
        name = xmlStrdup(BAD_CAST "--");
	
    switch(xmlTextReaderNodeType(reader[i])) {
        case XML_READER_TYPE_ELEMENT:
            empty = xmlTextReaderIsEmptyElement(reader[i]);
            if (xmlStrEqual(name, BAD_CAST "add"))
                mode = +1;
            else if (xmlStrEqual(name, BAD_CAST "delete"))
                mode = -1;
            else if (xmlStrEqual(name, BAD_CAST "planetdiff")) {
                mode = 0;
                break;
            } else {
                fprintf(stderr, "Unexpected node: <%s>\n", (char *)name);
                if (xmlStrEqual(name, BAD_CAST "osm"))
                    fprintf(stderr, "It looks like %s is an osm file not a planetdiff\n", files[i]);
                exit(1);
            }
            // Retrieve the object to add/remove from the patch file and apply
            if (!empty) {
                getobject(1);
                applyPatch(mode);
                break;
            }
            /* Drop through for self closing tags since these generate no end_element */
        case XML_READER_TYPE_END_ELEMENT:
            mode = 0;
            break;
        case XML_READER_TYPE_SIGNIFICANT_WHITESPACE:
            /* Ignore */
            break;
        default:
            fprintf(stderr, "Unknown node type %d\n", xmlTextReaderNodeType(reader[i]));
            break;
    }
    xmlFree(name);
}


static int patchFiles(void)
{
    int ret = 0;
    int i;
    // files[0] = planet.osm (input)
    // files[1] = delta.xml (patch)
    for(i=0; i<2; i++) {
        if (sanitize)
            reader[i] = sanitizerOpen(files[i]);
        else
            reader[i] = inputUTF8(files[i]);
        if (!reader[i]) {
            fprintf(stderr, "Unable to open %s\n", files[i]);
            return 1;
        }
    }

    printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    printf("<osm version=\"0.5\" generator=\"OpenStreetMap planet.rb\">\n");
    // Ensure generator string length matches original planet.rb
    // to allow 'cmp -l' to be run on output to verify correctness 
    //printf("<osm version=\"0.5\" generator=\"OSM planetpatch tool---\">\n");

    ret = xmlTextReaderRead(reader[1]);

    while (ret == 1) {
        processPatchNode();
        ret = xmlTextReaderRead(reader[1]);
    }

    if (ret != 0) {
        fprintf(stderr, "%s : failed to parse patch\n", files[1]);
        return ret;
    }

    // Displaying any trailer from input
    while ((current[0] != eof) && reader[0]) {
        displayPatch(0);
        getobject(0);
    }

    printf("</osm>\n");

    xmlFreeTextReader(reader[0]);
    xmlFreeTextReader(reader[1]);
    return 0;
}


static void usageDiff(const char *arg0)
{
    fprintf(stderr, "Usage:\n\t%s planet1.osm planet2.osm > planet.diff\n\nGenerates a difference file between the two input planet.osm files.\nThis binary is based on SVN revision: $Rev$.\n", arg0);
    exit(1);
}

static int mainDiff(int argc, char *argv[], const char *name)
{
    int i;

    if (argc != 3) {
        usageDiff(name);
        exit(1);
    }

    for (i=0; i<2; i++) {
        files[i] = argv[i+1];
        initList(&tags[i]);
        initList(&nds[i]);
        initList(&attrs[i]);
        initList(&member_nodes[i]);
        initList(&member_ways[i]);
        initList(&member_rels[i]);
    }

    if (diffFiles() != 0)
        exit(2);

    return 0;
}

static void usagePatch(const char *arg0)
{
    fprintf(stderr, "Usage:\n\t%s planet.osm delta.xml > out.osm\n\n", arg0);
    fprintf(stderr, "Applies the differences listed in delta.xml to the input planet.osm\n");
    exit(1);
}

static int mainPatch(int argc, char *argv[], const char *name)
{
    int i;

    if (argc != 3) {
        usagePatch(name);
        exit(1);
    }

    for (i=0; i<2; i++) {
        files[i] = argv[i+1];
        initList(&tags[i]);
        initList(&nds[i]);
        initList(&attrs[i]);
        initList(&member_nodes[i]);
        initList(&member_ways[i]);
        initList(&member_rels[i]);
    }

    if (patchFiles() != 0)
        exit(2);

    return 0;
}

int main(int argc, char *argv[])
{
    int r;
    const char *name;
    LIBXML_TEST_VERSION

    name = strrchr(argv[0], '/');

    if (!name)
        name = strrchr(argv[0], '\\');

    if (name)
        name++;
    else
        name = argv[0];

    if (!strcmp(name, "planetdiff"))
            r = mainDiff(argc, argv, name);
    else if (!strcmp(name, "planetpatch"))
            r = mainPatch(argc, argv, name);
    else {
        fprintf(stderr, "Usage error - should be called as planetdiff or planetpatch (not '%s')\n", name);
        exit(1);
    }

    xmlCleanupParser();
    xmlMemoryDump();
#ifdef VERBOSE
    fprintf(stderr, "\n");
#endif
    return r;
}
