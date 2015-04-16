<?php
	/**
	 *
	 * This is the mailbox of edumon. Please access it as follows and remeber to hold the session!
	 *
	 * Your request needs to contain the following GET data:
	 *
	 * - room [String] : The room, inside which you are at the moment
	 * - moderatorPassphrase [String] : If you want to authenticate as a moderator, you have to send this passphase (needed only once in a session)
	 *
	 * All packages you want to send should be in the BODY as a JSON Array:
	 *
	 * - each element of this array has to contain the following fields:
	 * - type [int] : the package's type
	 * - id [int] : the package's id
	 * - time [int] : the packages timestamp
	 * - from [String] : the sender (your SESSION_ID)
	 * - to [String] : the receiver's SESSION_ID or "MODERATOR" or "BROADCAST"
	 * - room [String] : the room you are in
	 * - body [Array] : the packages body
	 *
	 *
	 * An object containing the following fields will be returned as JSON:
	 *
	 * - inbox [Array] : All Packages which are stored for you. They contain the following fields:
	 * - type [int] : the package's type
	 * - id [int] : the package's id
	 * - time [int] : the packages timestamp
	 * - from [String] : the sender (your SESSION_ID)
	 * - to [String] : the receiver's SESSION_ID or "MODERATOR" or "BROADCAST"
	 * - room [String] : the room you are in
	 * - body [Array] : the packages body
	 * - errorMessages [Array (String)] : All error messages
	 * - clientId [String] : Your clientId
	 *
	 *
	 * Unless you are the moderator, your client id is your SESSION_ID.
	 *
	 *
	 * If you want to set up the system or reset it, please delete the files .htedumondatabase and .htedumonpassword
	 * from your server (located in the same folder as this script) and access mailbox.php?setup. Afterwards, follow
	 * the given instructions.
	 */

	error_reporting(-1);
	ini_set("display_errors", true);
	define('PW_FILE', '.htedumonpassword');
	define('DB_FILE', '.htedumondatabase');

	// if the .htedumon* files don't exist and the user accesses mailbox.php?setup, we will start the system's setup
	if (isset($_GET['setup']) and !file_exists(dirname(__FILE__).'/'.DB_FILE) and !file_exists(dirname(__FILE__).'/'.PW_FILE)) {

		?>
		<!DOCTYPE HTML>
		<html>
		<head>
			<meta charset="utf-8">
			<title>Set up your EduMon Message Server</title>
		</head>
		<body>
		<?php

		if (!is_writable(dirname(__FILE__))) {

			// no access to write folder
			?>
			<h1>Error</h1>
			<p>It seems, as if EduMon cannot write to the folder, it is located in...</p>
			<?php

		} elseif (!isset($_POST['passphrase'])) {

			// initial page
			?>
			<h1>Setup</h1>
			<p>Welcome to EduMon's Setup page. Please enter the passphrase, you want to use: </p>

			<form action="" method="post">
				<input type="password" name="passphrase">

				<h2>Start Setup</h2>

				<p>In order to create the database and to start the system, please cklick on the following button:</p>
				<input type="submit" name="submit" value="Start Setup">
			</form>
			<?php

		} else {

			// Create the two needed files

			// password file at first
			$handle = fopen(dirname(__FILE__).'/'.PW_FILE, 'w+');
			fwrite($handle, trim($_POST['passphrase']));
			fclose($handle);

			// now, the database
			$db = new SQLite3(dirname(__FILE__) . '/'.DB_FILE, SQLITE3_OPEN_CREATE | SQLITE3_OPEN_READWRITE);

			$db->exec("CREATE TABLE 'packages' (
					'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
					'room' TEXT NOT NULL,
					'to_client' TEXT NOT NULL,
					'time' INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
					'data' TEXT NOT NULL
				)
			");

			?>
			<h1>Finished</h1>
			<p>Congratulations! Your setup was successful. You are now able to use EduMon.</p>
			<p>Please notice the following points:</p>
			<ul>
				<li>
					To re-enter this setup and set a new master passphrase, you can just delete the files
					<i><?php echo DB_FILE.'</i> and <i>'.PW_FILE; ?></i>
				</li>
				<li>
					To change your passphrase, you can just edit the file <i><?php echo PW_FILE; ?></i>
				</li>
			</ul>
			<?php

		}

		?>
		</body>
		</html>
		<?php


		die();
	}




	// if no setup is performed, we will just do the normal mailbox stuff
	if (isset($_SERVER['HTTP_ORIGIN'])) {
		$url = parse_url($_SERVER['HTTP_ORIGIN']);
		$portExists = !empty($url['port']) && $url['port'] != 80 && $url['port'] != 443;

		header('Access-Control-Allow-Origin: ' . $url['scheme'] . '://' . $url['host'] . ($portExists ? ':' . $url['port'] : ''));
		header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
		header('Access-Control-Allow-Credentials: true');
	}
	header('Content-type: application/json; charset=utf-8');

	session_name('EDUMONSESSID');
	$sessionState = @session_start();
	if (!$sessionState) {
		session_regenerate_id(true);
		session_start();
	}

	if (@$_GET['logout']) {
		session_regenerate_id(true);
	}

	// For debug purposes -> get random demo package from server
	if (isset($_GET['demoPackages'])) {
		$demoFiles = glob(dirname(__FILE__) . '/demoData/*.json');

		die (file_get_contents($demoFiles[rand(0, count($demoFiles) - 1)]));
	}

	$errorMessages = array();

	// try to get access to the database
	try {
		$db = new SQLite3(dirname(__FILE__) . '/'.DB_FILE, SQLITE3_OPEN_READWRITE);

		// check table packages to be available in the right structure
		if ($stmt = @$db->prepare('PRAGMA table_info(packages)')) {
			$result = $stmt->execute();

			$givenStructure = array();

			while ($row = $result->fetchArray(SQLITE3_ASSOC)) {
				$givenStructure[$row['name']] = $row['type'];
			}

			$neededStructure = array(
				'id' => 'INTEGER',
				'room' => 'TEXT',
				'to_client' => 'TEXT',
				'time' => 'INTEGER',
				'data' => 'TEXT'
			);

			if (count($neededStructure) == count($givenStructure)) {

				// check all fields
				foreach ($neededStructure as $name => $type) {

					// check whether col exists
					if (!isset($givenStructure[$name])) {
						$errorMessages[] = 'Database table \'packages\' does not match the needed scheme. [Column \'' . $name . '\' is missing]';
						http_response_code(500);
					} elseif ($givenStructure[$name] != $type) {
						$errorMessages[] = 'Column \'' . $name . '\' of table \'packages\' does not match the needed type. [IS: ' . $givenStructure[$name] . ', SHOULD: ' . $type . ']';
						http_response_code(500);
					}
				}

			} else {
				$errorMessages[] = 'Database table \'packages\' does not match the needed scheme. [Col count doesn\'t match]';
				http_response_code(500);
			}

		} else {
			$errorMessages[] = 'Error while accessing the database.';
			http_response_code(500);
		}

	} catch (Exception $e) {
		$errorMessages[] = 'No database available. Did you set up the system correctly?';
		http_response_code(500);
	}


// check whether this client is the moderator
	if (isset($_GET['moderatorPassphrase'])) {
		if (!($passphrase = @file_get_contents(dirname(__FILE__) . '/'.PW_FILE))) {
			$errorMessages[] = 'No password file available. Did you set up the system correctly?';
			http_response_code(500);
		} else {
			$passphrase = trim($passphrase);

			$_SESSION['isModerator'] = ($passphrase == $_GET['moderatorPassphrase']);

			if (!$_SESSION['isModerator']) {
				$errorMessages[] = 'Incorrect passphrase.';
				http_response_code(401);
			}
		}

	} elseif (!isset($_SESSION['isModerator'])) {
		$_SESSION['isModerator'] = false;
	}


	//provide an easy way to check if up and running
	if (isset($_GET['ping'])) {
		if (count($errorMessages)>0) {
			http_response_code(500);
			die(implode(" ",$errorMessages));
		} else {
			http_response_code(202);
			die("EduMon");
		}
	}


	if (!isset($_GET['room']) or !is_string($_GET['room'])) {
		$errorMessages[] = 'Please select a valid room.';
		http_response_code(400);
	}


// if everything is okay, we can start
	if (http_response_code() == 200) {

		$logging = '';

		// delete old database entries
		$stmt = $db->prepare("DELETE FROM packages WHERE time < :timestamp");
		$stmt->bindValue(':timestamp', time() - 24 * 3600, SQLITE3_INTEGER);
		$stmt->execute();


		// No one should be able to see the moderator's session id
		if ($_SESSION['isModerator']) {
			$clientId = 'MODERATOR';
		} else {
			$clientId = session_id();
		}

		// check, whether there are packages to send

		$outbox = json_decode(file_get_contents('php://input'), true);

		// at least one entry? we have to send some packages
		$logging .= "Counted packages: ".count($outbox)."\n";
		if (is_array($outbox) and count($outbox) > 0) {
			$logging .= "Line ".(__LINE__)."\n";
			foreach ($outbox as $key => $package) {
				$logging .= "Line ".(__LINE__)."\n";
				// check package header on a basic level

				// if there is no array, we are wrong -> continue with next package
				if (!is_array($package)) {
					$errorMessages[] = 'Package ' . $key . ' is no array. Ignored package.';
					continue;
				}

				// needed field types
				$packageValidation = array(
					'integer' => array('type', 'time'),
					'string' => array('from', 'to', 'room'),
					'array' => array('body')
				);

				$packageError = false;

				// save all validated data into new array, so we now there are no additional fields
				$validatedPackage = array();

				foreach ($packageValidation as $type => $fields) {
					foreach ($fields as $field) {
						// If field doesn't exist, save error and continue with next one
						if (!isset($package[$field])) {
							$packageError = true;
							$errorMessages[] = 'Field "' . $field . '" in package ' . $key . ' is missing.';
							continue;
						}
						// check type of field
						if (gettype($package[$field]) != $type) {
							$packageError = true;
							$errorMessages[] = 'Type of field "' . $field . '" in package ' . $key . ' is invalid.'
								. '(IS ' . gettype($package[$field]) . ', SHOULD: ' . $type . ')';
							continue;
						}
						$validatedPackage[$field] = $package[$field];
					}
				}

				// if an error is registered, continue with next package
				if ($packageError) {
					$errorMessages[] = 'Ignored package ' . $key . ' because of errors.';
					continue;
				}

				// Moderator's session id will be replaced
				if ($_SESSION['isModerator']) {
					$package['from'] = 'MODERATOR';
				}

				// Don't send from other clients...
				if ($package['from'] != $clientId) {
					$errorMessages[] = 'You are not client "' . $package['from'] . '" (Package ' . $key . ').';
					continue;
				}

				// Don't send packages to yourself...
				if ($package['to'] == $clientId) {
					$errorMessages[] = 'You cannot send a package to yourself (Package ' . $key . ').';
					continue;
				}

				// Only the moderator can broadcast and send to clients. Hence, clients can only send to MODERATOR
				if ($package['to'] != 'MODERATOR' and !$_SESSION['isModerator']) {
					$errorMessages[] = 'Participants can only send packages to MODERATOR (Package ' . $key . ').';
					continue;
				}

				// send packages only to own room
				if ($package['room'] != $_GET['room']) {
					$errorMessages[] = 'Only packages inside the room are allowed [You are in room "' . $_GET['room'] . '" and wanted to send something to "' . $package['room'] . '"] (Package ' . $key . ').';
					continue;
				}

				// otherwise, we can continue
				$package = $validatedPackage;
				unset($validatedPackage);

				/*
					If we are here, the package has the right rough structure
					We will NOT check the body's structure now. This work should be done by the receiver
				*/

				// if new lecture starts (message type 1), delete all packages inside this room now
				if ($package['type'] == 1) {
					$stmt = $db->prepare("DELETE FROM packages WHERE room = :room");
					$stmt->bindValue(':room', $package['room'], SQLITE3_TEXT);
					$stmt->execute();
				}

				// insert new package into database

				$stmt = $db->prepare("INSERT INTO packages (room, to_client, data) VALUES (:room, :toClient, :data)");

				$stmt->bindValue(':room', $package['room'], SQLITE3_TEXT) or die($db->lastErrorMsg());
				$stmt->bindValue(':toClient', $package['to'], SQLITE3_TEXT);
				$stmt->bindValue(':data', json_encode($package, JSON_UNESCAPED_UNICODE), SQLITE3_TEXT);

				if (!$stmt->execute()) {
					$errorMessages[] = 'Could not save package ' . $key . ' to database. [Error: "' . $db->lastErrorMsg() . '"]';
				}
			}
		}


		/*
			Now, check, whether the client gets some packages itself
		*/
		if (!isset($_SESSION['lastDeliveredPackage'])) {
			$_SESSION['lastDeliveredPackage'] = 0;
		}

		// if the client is not the moderator, just choose broadcasts and personal ones
		if (!$_SESSION['isModerator']) {
			if ($stmt = $db->prepare("SELECT id, to_client, data FROM packages WHERE room = :room AND (id > :lastId AND (to_client = :clientId OR to_client = 'BROADCAST'))")) {
				$stmt->bindValue(':clientId', $clientId, SQLITE3_TEXT);
			}
		} else {
			$stmt = $db->prepare("SELECT id, to_client, data FROM packages WHERE room = :room AND (id > :lastId AND to_client = 'MODERATOR')");
		}

		$stmt->bindValue(':room', $_GET['room'], SQLITE3_TEXT);
		$stmt->bindValue(':lastId', $_SESSION['lastDeliveredPackage'], SQLITE3_INTEGER);

		$result = $stmt->execute();
		$receivedPackages = array();
		// remember delivered ids to delete later
		$deliveredIds = array();

		while ($row = $result->fetchArray(SQLITE3_ASSOC)) {
			$receivedPackages[] = json_decode($row['data'], true);

			// Broadcasts should not be deleted
			if ($row['to_client'] != 'BROADCAST') {
				$deliveredIds[] = $row['id'];
			}

			$_SESSION['lastDeliveredPackage'] = $row['id'];
		}

		// delete delivered ones
		$stmt = $db->prepare("DELETE FROM packages WHERE id IN (" . implode(',', $deliveredIds) . ")");
		$stmt->execute();

		/*
			prepare return
		*/

		$returnedData = array(
			'inbox' => $receivedPackages,
			'errorMessages' => $errorMessages,
			'clientId' => $clientId
		);

	} else {
		/*
			prepare return in case of failure
		*/

		$returnedData = array(
			'errorMessages' => $errorMessages
		);
	}

	echo json_encode($returnedData, JSON_UNESCAPED_UNICODE);
