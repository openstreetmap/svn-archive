#ifndef ELEMSTYLES_H
#define ELEMSTYLES_H

#include <vector>
#include <map>
#include <string>
#include "ElemStyle.h"
#include "Object.h"

namespace OSM
{

struct Rule
{
	std::string key, value;
	Rule() { }
	Rule(std::string k,std::string v) { key=k; value=v; }
};

		
class ElemStyles
{
private:

	struct _Style 
	{
		std::vector<Rule> rules;
		ElemStyle* style;

		_Style() { }
		_Style(const std::vector<Rule>& rules, ElemStyle* s)
		{
			this->rules=rules;
			this->style=s;
		}
	};

	std::vector<_Style> styles;

public:
	ElemStyles()
	{
	}

	~ElemStyles()
	{
		for(int count=0; count<styles.size(); count++)
			delete styles[count].style;
	}

	void add (const std::vector<Rule>& rules, ElemStyle* style)
	{
		styles.push_back(_Style(rules,style));
	}

	ElemStyle* getStyle (Object *object)
	{
		int hit=0, maxHit=0;
		ElemStyle* style=NULL;

		for(int count=0; count<styles.size(); count++)
		{
			for(std::map<std::string,std::string>::iterator i=
					object->tags.begin(); i!=object->tags.end(); i++)
			{

				for(int count2=0; count2<styles[count].rules.size(); count2++)
				{
					if(styles[count].rules[count2].key==i->first)
					{
						if(styles[count].rules[count2].value==i->second)
							hit++;
						else
						{
							hit=0;
							break;
						}
					}
				}
			}

			if(hit>maxHit)
			{
				maxHit=hit;
				style = styles[count].style;
			}
		}

		return style;
	}

	std::string getFeatureClass(Object *object)
	{
		ElemStyle* style = getStyle(object);
		return (style) ? style->getFeatureClass(): "";
	}
};

}

#endif
