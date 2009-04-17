#include "split_tags.hpp"
#include <iostream>

using namespace std;

void
print_vec(const string &n, const vector<string> &v) {
  cout << n << ":\n";
  for (vector<string>::const_iterator itr = v.begin();
       itr != v.end(); ++itr) {
    cout << "\"" << *itr << "\"" << endl;
  }
}

int
main() {
  print_vec("easy", tags_split("foo=bar;bax=bat"));
  print_vec("empty", tags_split(""));
  print_vec("missing val", tags_split("foo=;bax=bat"));
  print_vec("missing val 2", tags_split("foo="));
  print_vec("real 1", tags_split("highway=track;highway =cycleway;created_by=JOSM"));

  return 0;
}
