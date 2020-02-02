/**
 * 
 */
"use strict"
const PeriodicLoop = require('./PeriodicLoop.js');
class TimeoutManager extends PeriodicLoop {
	constructor(t, callback) {
		super(t);
		this.kicked = false;
		this.callback = callback;
	}

	kick() {
		this.kicked = true;
	}

	loop(){
		if (!this.kicked){
				console.log("timeout callback triggered");
				this.callback();
		}
		else{
			this.kicked = false;			
		}
	}
	
}

module.exports = TimeoutManager