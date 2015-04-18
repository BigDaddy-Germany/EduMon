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

	function send(data) {
		try {
			target.postMessage(data, '*')
		} catch (e) {
			console.error(e);
			console.error(data);
		}
	}

	function sendResult(id, result) {
		send({
			type: TYPE_RESULT,
			id: id,
			result: result
		});
	}

	function sendError(id, message) {
		send({
			type: TYPE_ERROR,
			id: id,
			message: message
		})
	}

	function getVerifyArgs(args) {
		if (args.length === 0) {
			throw "No name given!";
		}
		if (args.length > 1) {
			return Array.prototype.slice.call(args, 1);
		}
		return [];
	}

	this.invoke = function(name, args) {
		args = getVerifyArgs(arguments);
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

	this.invokeLocal = function(name, args) {
		args = getVerifyArgs(arguments);
		var func = functions[name];
		if (!func) {
			throw "Function not found!";
		}
		return func.apply(functions, args);
	};

	this.shutdown = function() {
		window.removeEventListener('message', handleMessage);
	}
};
