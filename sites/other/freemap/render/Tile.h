#include <iostream>
#include <boost/tokenizer.hpp>
#include <sstream>

using std::cout;
using std::cerr;
using std::endl;

class tile 
{
public:
	int w, s, e, n; 
	int scale;
	int width, height;
	std::string error;

	tile() { }

	void parse_query_string(std::string query_string)
	{
		// Replace %2C for bbox separators if necessary
		int i;
		while((i=query_string.find("%2C")) != -1)
			query_string.replace(i,3,",");

		typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
		boost::char_separator<char> ampersand("&"), equals("="), comma(",");
		tokenizer t (query_string,ampersand);
		std::string key, value;

		for(tokenizer::iterator i=t.begin(); i!=t.end(); i++)
		{
			tokenizer t1 (*i, equals);

			int count=0;
			for(tokenizer::iterator j=t1.begin(); j!=t1.end(); j++)
			{
				if(count==0)
					key=*j;
				else if (count==1)
				{
					value=*j;
					if(key=="bbox" || key=="BBOX")
					{
						tokenizer t2(value,comma);
						int count2=0;
						for(tokenizer::iterator k=t2.begin(); k!=t2.end(); k++)
						{
							if(count2==0)
								w=atoi(k->c_str());
							else if(count2==1)
								s=atoi(k->c_str());
							else if(count2==2)
								e=atoi(k->c_str());
							else if(count2==3)
								n=atoi(k->c_str());
							count2++;
						}
					}
					else if(key=="width" || key=="WIDTH")
					{
						width=atoi(value.c_str());
					}
					else if(key=="height" || key=="HEIGHT")
					{
						height=atoi(value.c_str());
					}
				}
				count++;
    		}
		}
	}

	bool chk_input_sensible();

	std::string get_error() { return error; }
	std::string get_filename(const std::string& root);

	void print()
	{
		cout << "w=" << w << " s=" << s << " e=" << e <<  " n=" << n
				<<" width=" << width << " height=" << height << 
				" scale=" << scale << endl;
	}
};

