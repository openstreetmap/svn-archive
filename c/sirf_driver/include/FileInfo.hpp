#ifndef FILE_INFO_H
#define FILE_INFO_H

#include "XML.hpp"

namespace XML {

  class FileInfo {
  public:
    FileInfo(const string &_author, const string &_id, const string &_domain,
	     const string &_year, const string &_uri, const string &_name,
	     const string &_desc) :
      XMLElement(("metadata")), author(_author, _id, _domain),
      copyright(_year, _uri, _author), name(("name"), _name), 
      description(("desc"),_desc) {
    }
    xmlNodePtr write(xmlNodePtr parent);
  private:
    XMLElement name, description;
    FileAuthor author;
    FileCopyright copyright;
    vector<FileLink> links;
    //??? time;
    vector<string> keywords;
    // this is really annoying - we won't know it until 
    // we've built the file
    //Bounds bounds;
    bool extended;
  };

}

#endif /* FILE_INFO_H */
