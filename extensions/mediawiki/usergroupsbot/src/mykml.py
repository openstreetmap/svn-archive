'''
Created on 21.11.2010

a simple KML 2.2 lib providing Placemark and styling support using DOM

@author: Matthias Meisser
'''

import xml.dom.minidom
import codecs
import pprint
from StringIO import StringIO


class kml:
    def __init__(self,title,description):
        """Add KML preamble"""
        title=unicode(title)
        description=unicode(description)
        self.__doc = xml.dom.minidom.Document()
        kml = self.__doc.createElement('kml')
        kml.setAttribute('xmlns', 'http://www.opengis.net/kml/2.2')
        self.__doc.appendChild(kml)
        document = self.__doc.createElement('Document')
        kml.appendChild(document)
        #TODO only if provided
        docName = self.__doc.createElement('name')
        document.appendChild(docName)
        docName_text = self.__doc.createTextNode(title)
        docName.appendChild(docName_text)
        docDesc = self.__doc.createElement('description')
        document.appendChild(docDesc)
        docDesc_text = self.__doc.createTextNode(description)
        docDesc.appendChild(docDesc_text)
        self.__idcounter=0
        self.__kml=document
        
    def add_style(self,name, icon):
        name=unicode(name)
        icon=unicode(icon)
        """Add a simple Style block with a icon"""
        style = self.__doc.createElement('Style')
        style.setAttribute('id', name)
        self.__kml.appendChild(style)
        icon_style = self.__doc.createElement('IconStyle')
        style.appendChild(icon_style)
        sIcon = self.__doc.createElement('Icon')
        icon_style.appendChild(sIcon)
        href = self.__doc.createElement('href')
        sIcon.appendChild(href)
        scale = self.__doc.createElement('scale')
        sIcon.appendChild(scale)
        scale_text = self.__doc.createTextNode("0.5")
        scale.appendChild(scale_text)
        iconurl=self.__doc.createTextNode(icon)
        href.appendChild(iconurl)
        
    def add_placemark(self, name, (lon,lat), style,attributes):
        """Generate the KML Placemark for a given address."""
        name=name
        style=style
        pm = self.__doc.createElement("Placemark")
        pm.setAttribute("id",str(self.__idcounter))
        self.__idcounter+=1
        self.__kml.appendChild(pm)
        pname = self.__doc.createElement("name")
        pm.appendChild(pname)
        #TODO a seperated attributes subnode?
        name_text = self.__doc.createTextNode(name)
        pname.appendChild(name_text)
        pt = self.__doc.createElement("Point")
        pm.appendChild(pt)
        coords = self.__doc.createElement("coordinates")
        pt.appendChild(coords)
        coords_text = self.__doc.createTextNode(lon+","+lat)
        coords.appendChild(coords_text)
        style_url = self.__doc.createElement("styleUrl")
        pm.appendChild(style_url)
        style_url_text = self.__doc.createTextNode("#"+style)
        style_url.appendChild(style_url_text)
        #now all attributes
        for a in attributes:
            desc = self.__doc.createElement(a)
            pm.appendChild(desc)
            desc_text = self.__doc.createTextNode(attributes[a])
            desc.appendChild(desc_text)


    def save(self,filename):
        out = codecs.open(filename,'w+',"utf-8")
        self.__doc.writexml(out,encoding="utf-8")
        out.close()

    
    