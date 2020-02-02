"use strict"
class PeriodicLoop {
	constructor(t) {
		this.t = t;
		this.timerId = null;
	}

	loop() {
	}

	start() {
		/*Когда функция передаётся в setInterval/setTimeout,
		 на неё создаётся внутренняя ссылка и сохраняется в планировщике.
	     Это предотвращает попадание функции в сборщик мусора,
		 даже если на неё нет других ссылок.
		*/
		/* 'this' is not related to the desired class anymore,
		 as soon as it gets triggered  - use bind*/
		this.timerId = setInterval(this.loop.bind(this), this.t);
	}

	stop() {
		clearInterval(this.timerId);
	}
}

module.exports = PeriodicLoop