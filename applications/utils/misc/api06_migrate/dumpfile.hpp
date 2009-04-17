#ifndef DUMPFILE_HPP
#define DUMPFILE_HPP

#include <fstream>
#include <string>
#include <boost/utility.hpp>

/**
 * creates a FIFO and dumps data to it in a format that mysql can
 * read as "load data infile".
 */
class dumpfile 
  : public boost::noncopyable {
public:

  dumpfile(const std::string &n);
  ~dumpfile();

  void close();
  void open();

  std::fstream fh;
  const std::string path;
};

#endif /* DUMPFILE_HPP */
