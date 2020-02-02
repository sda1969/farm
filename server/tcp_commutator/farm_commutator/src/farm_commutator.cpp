//============================================================================
// Name        : farm_commutator.cpp
// Author      : 
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
#include <arpa/inet.h>
#include <thread>
#include <atomic>

using namespace std;

namespace {

constexpr int BUFFER_SIZE = 1024;
constexpr int PORT1 = 5025;
constexpr int PORT2 = 8080;
atomic_int num_connections  {0};
atomic_int fd8080 {-1};
atomic_int fd5025 {-1};

void serv_connection(int port) {

	int sockfd, newsockfd;
	socklen_t socklen;
	char buffer[BUFFER_SIZE];

	struct sockaddr_in sock_addr;
	int n = -1;

	//инициализация сокета 1
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		printf("ERROR opening socket\n");
		exit(1);
	}

	bzero((char *) &sock_addr, sizeof(sock_addr));
	sock_addr.sin_family = AF_INET;
	sock_addr.sin_addr.s_addr = INADDR_ANY;
	sock_addr.sin_port = htons(port);

	if (bind(sockfd, (struct sockaddr *) &sock_addr, sizeof(sock_addr))
			< 0) {
		printf("ERROR on binding\n");
		exit(1);
	}

	while (true) {
		// ждем подключения клиента - обрабатываем только одно входящее соединение
		printf("Waiting for connection\n");
		listen(sockfd, 5);

		socklen = sizeof(sock_addr);
		newsockfd = accept(sockfd, (struct sockaddr *) &sock_addr,
				&socklen);
		if (newsockfd < 0) {
			printf("ERROR on accept\n");
			exit(1);
		}

		if(port == 5025){
			fd5025.store(newsockfd);
		}
		else {
			fd8080.store(newsockfd);
		}

		num_connections++;
		printf("Accepted from %08X\n", htonl(sock_addr.sin_addr.s_addr));

		while (true) {
			//соединение установлено слушаем  новый сокет
			bzero(buffer, BUFFER_SIZE);
			n = read(newsockfd, buffer, BUFFER_SIZE);
			if (n < 0) {
				printf("ERROR reading from socket\n");
				num_connections--;
				close(newsockfd);
				break;
			}
			if (n == 0) {
				printf("n == 0 client closed connection\n");
				num_connections--;
				close(newsockfd);
				break;
			}
			// разбираем комманды
			printf("%s\n", buffer);
			//если GUI клиенту ничего не ответить он разорвет соединение со своей стороны

			if(2 == num_connections.load()){
				int soc = (port == 5025) ? fd8080.load() : fd5025.load();
				write(soc, buffer, n);
			}

		}
	}
}
} //namespace

int main(int argc, char ** argv) {

	thread conn5025(serv_connection, 5025);
	thread conn5026(serv_connection, 5026);
	thread conn_monitor([](){while(1){sleep(1); printf("connections total = %d\n", num_connections.load());}});

	conn5025.join();
	conn5026.join();
	conn_monitor.join();

}
