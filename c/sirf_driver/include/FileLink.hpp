#ifndef FILE_LINK_H
#define FILE_LINK_H

#include "XML.hpp"

namespace XML {

  class FileLink : public XMLElement {
  public:
    FileLink(const string &_href, const string &_text, const string &_type) :
      XMLElement(("link")), text(_text), type(_type) {
      addProp(("href"), _href);
    }
    xmlNodePtr write(xmlNodePtr parent);
  private:
    XMLElement text, type;
  };
  
}

#endif /* FILE_LINK_H */
