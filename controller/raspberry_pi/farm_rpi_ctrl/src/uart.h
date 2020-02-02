/*
 * uart.h
 *
 *  Created on: 22 апр. 2019 г.
 *      Author: user
 */

#ifndef UART_H_
#define UART_H_

namespace uart {

	int init();
	void send(int uart, std::string msg);
	std::string sendReceive(int uart, std::string msg);
}


#endif /* UART_H_ */
