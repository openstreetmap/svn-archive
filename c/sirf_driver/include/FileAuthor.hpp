#ifndef FILE_AUTHOR_H
#define FILE_AUTHOR_H

#include "XML.hpp"

namespace XML {

  class FileAuthor : public XMLElement {
  public:
    FileAuthor(const string &_author, const string &_id, 
	       const string &_domain) : 
      XMLElement(("author")), author(("name"), _author), 
      email(("email")),
      link(("link")) {
      email.addProp(("id"), _id);
      email.addProp(("domain"), _domain);
    }
    xmlNodePtr write(xmlNodePtr parent);
  private:
    XMLElement author, email, link;
  };
  
}

#endif /* FILE_AUTHOR_H */
