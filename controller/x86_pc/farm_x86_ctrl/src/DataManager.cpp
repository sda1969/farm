/*
 * DataManager.cpp
 *
 *  Created on: 29 мар. 2019 г.
 *      Author: user
 */

#include <iostream>
#include "DataManager.h"

namespace data {

DataManager::DataManager(const std::string& prf, const std::list<std::string>& listOfItems) {
	prefix = prf;
	for (const std::string& itemName : listOfItems) {
		ret_map_insert_t res = items.insert( { itemName, DataItem(itemName) });
		if (!res.second) {
			printf("protocol design error - fieds must be unique\n");
			exit(1);
		}
	}
}

std::string DataManager::marshall() {
	std::string res = prefix;
	for (auto& item : items) {
		res += item.second.marshall() + ",";
	}
	// удалить последнюю ","
	res.pop_back();
	res += ";\n";

	return res;
}

void DataManager::unmarshall(const std::string& str) {
	int safetyCounter = 0;

	std::string remainStr = str;

	// сравниваем и удаляем префикс
	size_t posPrefixEnd = remainStr.find_first_of(':');
	if (posPrefixEnd == std::string::npos){
		std::cerr << "Prefix not found" << '\n';
	}
	else {
		std::string r_prefix = remainStr.substr(0, posPrefixEnd + 1);;
		remainStr = remainStr.substr(++posPrefixEnd);
		if(r_prefix != prefix){
			std::cerr << "Wrong prefix" << '\n';
		}
	}

	while (remainStr.length() > 2) {
		size_t posNewItem = remainStr.find_first_of(',');
		std::string item;
		if (posNewItem == std::string::npos){
			item = remainStr;
			posNewItem = remainStr.length()- 1;
		}
		else {
			item = remainStr.substr(0, posNewItem);
		}

		size_t posKey = item.find_first_of('=');
		if (posKey == std::string::npos){
			break;
		}
		std::string key = item.substr(0, posKey);
//		std::cout << item << "\n";
//		std::cout << key << "\n";
		try {
			items.at(key).unmarshall(item);
		}
		catch (const std::out_of_range& oor){
			std::cerr << "Out of Range error: " << oor.what() << '\n';
		}

		remainStr = remainStr.substr(++posNewItem);

		if (++ safetyCounter > 100)
			break;
	}

}

} /* namespace data */
