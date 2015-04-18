/**
 * This class implements an RPC service to communicate between windows.
 *
 * @param {Window} target the window to send messages to
 * @param {Object} functions a map of name -> function pairs.
 * @constructor
 */
EduMon.XWindowRPC = function (target, functions) {

	var nextId = 1;
	var outstandingResponses = {};

	var TYPE_INVOKE = 1;
	var TYPE_RESULT = 2;
	var TYPE_ERROR = 3;

	/**
	 * This function handles incoming messages.
	 * It is intentionally declared as a var to have a different instance per XWindowRPC instance to safely remove the
	 * listener from the current window.
	 *
	 * @param e the message event
	 */
	var handleMessage = function (e) {
		var data = e.data;

		var p;
		if (data.type === TYPE_INVOKE) {
			var func = functions[data.name];
			if (!func) {
				sendError(data.id, "Function not found!");
			}
			var result;
			try {
				result = func.apply(functions, data.args);
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
	};

	window.addEventListener('message', handleMessage);

	/**
	 * Sends a data packet to the target window
	 *
	 * @param data the data to send
	 */
	function send(data) {
		try {
			target.postMessage(data, '*')
		} catch (e) {
			console.error(e);
			console.error(data);
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
		return new Promise(function (fulfill, reject) {
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
		var func = functions[name];
		if (!func) {
			throw "Function not found!";
		}
		return func.apply(functions, args);
	};

	/**
	 * Shuts down this RPC service.
	 */
	this.shutdown = function() {
		window.removeEventListener('message', handleMessage);
	}
};
