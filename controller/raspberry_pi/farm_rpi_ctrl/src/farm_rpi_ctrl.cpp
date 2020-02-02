//============================================================================
// Name        : farm_server_x86.cpp
// Author      : D.Stepanov
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <fstream>
#include <string>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include "uart.h"

using namespace std;

namespace {

constexpr int BUFFER_SIZE = 65536;
constexpr int PORT = 5025;

string requestAllSensorsData = "get_sensors;"; 	// запрос  к Ардуино на отправку данных датчиков
string requestAllSetupData = "get_setup;"; 		// запрос  к Ардуино на отправку уставок
string msgSetSetup = "setup:";					// запрос на установку уставок

}

int main() {

	//инициализация UART
	int uart = uart::init();

	//инициализация сокета
	int sockfd, newsockfd;
	socklen_t clilen;
	char buffer[BUFFER_SIZE];
	struct sockaddr_in serv_addr, cli_addr;
	int n = -1;

	// используем TCP --> SOCK_STREAM
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		printf("ERROR opening socket\n");
		exit(1);
	}

	// привязываем сокет к заданному порту
	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(PORT);

	if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
		printf("ERROR on binding\n");
		exit(1);
	}

	// ------   Главный бесконечный цикл программы
	while (true) {
		// ждем подключения клиента - обрабатываем специально только одно входящее соединение
		listen(sockfd, 5);

		// клиент подключился - для общения с ним новый сокет newsockfd
		clilen = sizeof(cli_addr);
		newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		if (newsockfd < 0) {
			printf("ERROR on accept\n");
			exit(1);
		}

		//установка таймаут нового сокета в 2 секунды
		struct timeval t;
		t.tv_sec = 2;
		t.tv_usec = 0;

		setsockopt(newsockfd,     // Socket descriptor
				SOL_SOCKET, // To manipulate options at the sockets API level
				SO_RCVTIMEO, // Specify the receiving or sending timeouts
				&t, // option values
				sizeof(t));

		// бесконечный цикл общения с подключенным клиентом
		// при разрыве соединения выход в Главный цикл и ожидание нового подключения

		while (true) {
			//соединение уже установлено, слушаем  новый сокет
			bzero(buffer, BUFFER_SIZE);
			n = read(newsockfd, buffer, BUFFER_SIZE);
			if (n < 0) {
				printf("ERROR reading from socket=%d\n", errno);
				break;
			}
			if (n == 0) {
				// это происходит при отключение клиента
				printf("n == 0 client closed connection\n");
				break;
			}

			// разбираем комманды - нужно знать на какую ожидать ответ
			string received = string(buffer, n);
			if (	(received == requestAllSensorsData) ||
					(received == requestAllSetupData) ){
				printf("from client: %s\n", received.c_str());
				//на эти комманды надо отослать ответ
				std::string reply = uart::sendReceive(uart, received);
				//printf("from arduino: %s\n", reply.c_str());
				write(newsockfd, reply.c_str(), reply.length());

			} else if (received.substr(0, msgSetSetup.length()) == msgSetSetup.substr(0, msgSetSetup.length())) {
				//установка новых уставок - ответ не требуется
				printf("from client: %s\n", received.c_str());
				uart::send(uart, received);
			} else {
				//принято что-то странное - игнорируем
				printf("trash received: %s\n", buffer);
			}

		}
		close(newsockfd);
	}
	close(sockfd);

	return 0;
}
