EduMon.XWindowRPC = function (target, functions) {

	var nextId = 1;
	var outstandingResponses = {};

	var TYPE_INVOKE = 1;
	var TYPE_RESULT = 2;
	var TYPE_ERROR = 3;

	window.addEventListener('message', function (e) {
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
	});

	function send(data) {
		target.postMessage(data, '*')
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

	this.invoke = function(name, args) {
		args = Array.prototype.slice.call(arguments, 1);

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
		var func = functions[name];
		if (!func) {
			throw "Function not found!";
		}
		return func.apply(functions, args);
	};
};
