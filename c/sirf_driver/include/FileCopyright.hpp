#ifndef FILE_COPYRIGHT_H
#define FILE_COPYRIGHT_H

#include "XML.hpp"

namespace XML {

  class FileCopyright : public XMLElement {
  public:
    FileCopyright(const string &_year, const string &_uri, 
		  const string &_author) :
      XMLElement(("copyright")), year(("year"), _year), 
      license(("license"), _uri) {
      addProp(("author"), _author);
    }
    xmlNodePtr write(xmlNodePtr parent);
  private:
    XMLElement year, license;
  };

}

#endif /* FILE_COPYRIGHT_H */
