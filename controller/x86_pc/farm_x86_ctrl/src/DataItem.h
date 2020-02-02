/*
 * DataItem.h
 *
 *  Created on: 29 мар. 2019 г.
 *      Author: user
 */

#ifndef DATAITEM_H_
#define DATAITEM_H_

#include <string>
#include <functional>

namespace data {

class DataItem {
public:
	DataItem(const std::string& name) : itemName(name) {}
	virtual ~DataItem() {}
	std::string marshall();
	void unmarshall(std::string str);
	void setvalue(int v) {value = v;}
	int getvalue() {return value;}
	void setCallback(std::function<void(int)> f) {onUpdate = f;}

private:
	const std::string itemName;
	int value = -1;
	std::function<void(int)> onUpdate = nullptr;
};

} /* namespace data */

#endif /* DATAITEM_H_ */
