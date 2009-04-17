#ifndef BLOCK_VECTOR_HPP
#define BLOCK_VECTOR_HPP

#include <vector>
#include <iostream>

/**
 * A vector which appears to be of length n, but only allocates
 * blocks in chunks. Chunk size is controlled by the block_bits
 * template parameter. In this respect it could be described as
 * a depth-2 trie.
 *
 * Obviously this isn't as efficient as allocating one big block,
 * but it could save some memory if the access pattern is sparse.
 */
template <typename T, size_t block_bits = 20>
class block_vector {
public:

  static const size_t block_size = 1 << block_bits;
  static const size_t block_mask = block_size - 1;

  block_vector(size_t max) 
    : data(((max - 1) >> block_bits) + 1) {
    print_status();
  }

  ~block_vector() {
  }

  T operator[](size_t i) const {
    std::vector<T> &block = data[i >> block_bits];
    if (block.size() == 0) {
      block.resize(block_size);
      print_status();
    }
    return block[i & block_mask];
  }

  T &operator[](size_t i) {
    std::vector<T> &block = data[i >> block_bits];
    if (block.size() == 0) {
      block.resize(block_size);
      print_status();
    }
    return block[i & block_mask];
  }    

private:

  typedef std::vector<std::vector<T> > data_t;

  void print_status() const {
    size_t root = sizeof(data);
    size_t first = sizeof(std::vector<T>*) * data.size();
    size_t second = 0;
    for (typename data_t::const_iterator itr = data.begin();
	 itr != data.end(); ++itr) {
      second += sizeof(std::vector<T>);
      second += sizeof(T) * itr->size();
    }

    std::cerr << "Allocated: "
	      << root << " "
	      << first << " "
	      << second << std::endl;
  }
  
  // double lookup - should be enough to save some space.
  // dunno about runtime, probably will be OK.
  mutable data_t data;

};

#endif /* BLOCK_VECTOR_HPP */
