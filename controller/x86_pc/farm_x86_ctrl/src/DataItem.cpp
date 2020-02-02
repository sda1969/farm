/*
 * DataItem.cpp
 *
 *  Created on: 29 мар. 2019 г.
 *      Author: user
 */
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "DataItem.h"

namespace data {

std::string DataItem::marshall() {
	return itemName + "=" + std::to_string(value);
}

void DataItem::unmarshall(std::string str) {
	size_t posVal = str.find_first_of('=');
	if (posVal != std::string::npos) {
		std::string valStr = str.substr(++posVal);
		int valNew = std::stoi(valStr);
		if (valNew != -1){
			value = valNew;
		}
	}
}

} /* namespace data */
