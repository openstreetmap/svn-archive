#ifndef AUTHOR_INFO_H
#define AUTHOR_INFO_H

#include <string>

class AuthorInfo {
public:
  enum CopyrightType {
    CreativeCommons = 0,
    PublicDomain = 1,
    Artistic = 2
  };
  /*
  static AuthorInfo importFromDotFile(const std::string &path);
  */

  AuthorInfo(std::string _fullname, std::string email, 
	     CopyrightType cp = CreativeCommons);

  AuthorInfo(std::string _fullname, std::string _email_id, 
	     std::string _email_domain, CopyrightType cp = CreativeCommons);

  std::string getFullName() const;
  std::string getEmailID() const;
  std::string getEmailDomain() const;
  std::string getCopyrightURL() const;

private:
  std::string fullname, email_id, email_domain;
  CopyrightType copyright;
};

#endif /* AUTHOR_INFO_H */
