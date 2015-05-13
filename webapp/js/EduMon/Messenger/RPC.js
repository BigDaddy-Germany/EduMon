/**
 * This class implements an RPC service to communicate between two entities.
 *
 * @author Phillip
 *
 * @param {Function} registerReceiver the function to send a packet of data to the remote node
 * @param {Function} send the function to send a packet of data to the remote node
 * @param {Object} procedures a map of name -> function pairs.
 * @constructor
 */
function RPC(registerReceiver, send, procedures) {

	if (!(registerReceiver instanceof Function)) {
		throw "registerReceiver must be a function!";
	}

	if (!(send instanceof Function)) {
		throw "send must be a function!";
	}

	var nextId = 1;
	var outstandingResponses = {};

	var TYPE_INVOKE = 1;
	var TYPE_RESULT = 2;
	var TYPE_ERROR = 3;

	var removeReceiver = registerReceiver(handleMessage);

	if (!(removeReceiver instanceof Function)) {
		throw "registerReceiver must return a function that removes the receiver";
	}

	/**
	 * This function handles incoming messages.
	 * It is intentionally declared as a var to have a different instance per XWindowRPC instance to safely remove the
	 * listener from the current window.
	 *
	 * @param e the message event
	 */
	function handleMessage(e) {
		var data = e.data;

		var p;
		if (data.type === TYPE_INVOKE) {
			var func = procedures[data.name];
			if (!func) {
				sendError(data.id, "Function not found!");
			}
			var result;
			try {
				result = func.apply(procedures, data.args);
			} catch (e) {
				sendError(data.id, e);
			}
			sendResult(data.id, result);
		} else if (data.type === TYPE_RESULT) {
			p = outstandingResponses[data.id];
			if (!p) {
				return;
			}
			var fulfill = p[0];
			fulfill(data.result)
		} else if (data.type === TYPE_ERROR) {
			p = outstandingResponses[data.id];
			if (!p) {
				return;
			}
			var reject = p[1];
			reject(data.message);
		}
	}

	/**
	 * Sends a result packet to the target window with the given invocation ID and the result.
	 *
	 * @param id the id of the procedure call
	 * @param result the result of the procedure
	 */
	function sendResult(id, result) {
		send({
			type: TYPE_RESULT,
			id: id,
			result: result
		});
	}

	/**
	 * Sends an error packet to the target window with the given invocation ID and the error message.
	 *
	 * @param id the id of the procedure call
	 * @param message the error message
	 */
	function sendError(id, message) {
		send({
			type: TYPE_ERROR,
			id: id,
			message: message
		})
	}

	/**
	 * Verifies the arguments of the invoke call and returns the parameters for the
	 *
	 * @param {Array} args the invoke(Local) arguments.
	 * @returns {Array} the procedure arguments
	 */
	function getVerifiedArgs(args) {
		if (args.length === 0) {
			throw "No name given!";
		}
		if (args.length > 1) {
			return Array.prototype.slice.call(args, 1);
		}
		return [];
	}

	/**
	 * Calls a remote procedure.
	 *
	 * @param {string} name the name of the remote procedure to call
	 * @param {... *} args the arguments for the remote procedure
	 * @returns {Promise} a promise of the RPC result
	 */
	this.invoke = function(name, args) {
		args = getVerifiedArgs(arguments);
		return new Promise(function(fulfill, reject) {
			var id = nextId++;
			outstandingResponses[id] = [fulfill, reject];
			send({
				type: TYPE_INVOKE,
				id: id,
				name: name,
				args: args
			});
		});
	};

	/**
	 * Calls a local procedure.
	 *
	 * @param {string} name the name of the local procedure to call
	 * @param {... *} args the arguments for the local procedure
	 * @returns {*} The result of the procedure
	 */
	this.invokeLocal = function(name, args) {
		args = getVerifiedArgs(arguments);
		var func = procedures[name];
		if (!func) {
			throw "Function not found!";
		}
		return func.apply(procedures, args);
	};

	/**
	 * Shuts down this RPC service.
	 */
	this.shutdown = function() {
		removeReceiver();
	}
}

/**
 * Implements RPC for cross window communitcation.
 *
 * @param {Window} target the window that the data is send to
 * @param {Object} procedures the API
 * @returns {RPC} a new RPC instance configured for cross window communication
 * @constructor
 */
RPC.xWindow = function(target, procedures) {

	if (!(target && "onmessage" in target && "addEventListener" in target && typeof target.postMessage == "function")) {
		throw "target must be a window!";
	}

	function registerMessageListener(func) {
		window.addEventListener('message', func);
		return function() {
			window.removeEventListener('message', func);
		}
	}

	function sender(data) {
		try {
			target.postMessage(data, '*')
		} catch (e) {
			console.error(e);
			console.error(data);
		}
	}

	return new RPC(registerMessageListener, sender, procedures);
};

/**
 * Implements RPC to communicate with a Web Worker
 *
 * @param {Worker} target the worker to communicate with
 * @param {Object} procedures the API
 * @returns {RPC} a new RPC instance
 * @constructor
 */
RPC.WorkerRPC = function(target, procedures) {
	// verify a Worker-like object
	if (!(target && "onmessage" in target && typeof target.postMessage == "function")) {
		throw "worker must be a Web Worker " + Date.now();
	}

	function registerMessageListener(func) {
		target.onmessage = func;
		return function() {
			target.onmessage = null;
		}
	}

	function sender(data) {
		try {
			target.postMessage(data)
		} catch (e) {
			console.error(e);
			console.error(data);
		}
	}

	return new RPC(registerMessageListener, sender, procedures);
};
