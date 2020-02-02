/*
 * DataManager.h
 *
 *  Created on: 29 мар. 2019 г.
 *      Author: user
 */

#ifndef DATAMANAGER_H_
#define DATAMANAGER_H_
#include <string>
#include <list>
#include <map>
#include <functional>
#include "DataItem.h"

namespace data {

	typedef std::pair<std::map<std::string, DataItem>::iterator,bool> ret_map_insert_t;

class  DataManager {
public:
	DataManager(const std::string& prf, const std::list<std::string>& listOfItems);
	std::string marshall();
	void unmarshall(const std::string& str);
	void setvalue(const std::string& name, int v) {items.at(name).setvalue(v);}
	int getvalue(const std::string& name) {return items.at(name).getvalue();}
	void setCallback(const std::string& name, const std::function<void(int)>& f);

private:
	std::map<std::string, DataItem> items;
	std::string prefix;
};

} /* namespace data */

#endif /* DATAMANAGER_H_ */
