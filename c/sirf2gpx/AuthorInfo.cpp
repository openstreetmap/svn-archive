#include "AuthorInfo.hpp"

static const char *copyright_urls[] = {
  "http://www.creativecommons.org/blah",
  "public domain url",
  "artistic license"
};

/*
AuthorInfo AuthorInfo::importFromDotFile(const std::string &path) {
}
*/

AuthorInfo::AuthorInfo(std::string _fullname, std::string email, 
		       CopyrightType cp) {
  copyright = cp;
  fullname = _fullname;
  int at_pos = email.find("@");
  if (at_pos == std::string::npos) {
    // throw an error or something
  } else {
    email_id = email.substr(0, at_pos);
    email_domain = email.substr(at_pos+1);
  }
}

AuthorInfo::AuthorInfo(std::string _fullname, std::string _email_id, 
		       std::string _email_domain, CopyrightType cp) {
  copyright = cp;
  fullname = _fullname;
  email_id = _email_id;
  email_domain = _email_domain;
}

std::string AuthorInfo::getFullName() const {
  return fullname;
}

std::string AuthorInfo::getEmailID() const {
  return email_id;
}

std::string AuthorInfo::getEmailDomain() const {
  return email_domain;
}

std::string AuthorInfo::getCopyrightURL() const {
  return copyright_urls[copyright];
}

