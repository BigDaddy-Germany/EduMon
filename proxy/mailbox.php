<?php
	/**
	
		This is the mailbox of edumon. Please access it as follows and remeber to hold the session!
		
		Your request needs to contain the following GET data:
		
			- room [String] : The room, inside which you are at the moment
			- moderatorPassphrase [String] : If you want to authenticate as a moderator, you have to send this passphase (needed only once in a session)
		
		All packages you want to send should be in the BODY as a JSON Array:
		
			- each element of this array has to contain the following fields:
				- type [int] : the package's type
				- id [int] : the package's id
				- time [int] : the packages timestamp
				- from [String] : the sender (your SESSION_ID)
				- to [String] : the receiver's SESSION_ID or "MODERATOR" or "BROADCAST"
				- room [String] : the room you are in
				- body [Array] : the packages body
		
		
		An object containing the following fields will be returned as JSON:
		
			- inbox [Array] : All Packages which are stored for you. They contain the following fields: 
				- type [int] : the package's type
				- id [int] : the package's id
				- time [int] : the packages timestamp
				- from [String] : the sender (your SESSION_ID)
				- to [String] : the receiver's SESSION_ID or "MODERATOR" or "BROADCAST"
				- room [String] : the room you are in
				- body [Array] : the packages body
			- errorMessages [Array (String)] : All error messages
			- clientId [String] : Your clientId
			
		
		Unless you are the moderator, your client id is your SESSION_ID. 
			
	*/


    if (isset($_SERVER['HTTP_ORIGIN'])) {
        $url = parse_url($_SERVER['HTTP_ORIGIN']);
        header('Access-Control-Allow-Origin: '.$url['scheme'].'://'.$url['host']);
        header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
        header('Access-Control-Allow-Credentials: true');
    }
	header('Content-type: application/json; charset=utf-8');
	
	session_start();
	
	$errorMessages = array();
	
	// try to get access to the database
	try {
		$db = new SQLite3(dirname(__FILE__).'/.htedumondatabase', SQLITE3_OPEN_READWRITE);
		
		// check table packages to be available in the right structure
		if ($stmt = @$db->prepare('PRAGMA table_info(packages)')) {
			$result = $stmt->execute();
			
			$givenStructure = array();
			
			while($row = $result->fetchArray(SQLITE3_ASSOC)) {
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
						$errorMessages[] = 'Database table \'packages\' does not match the needed scheme. [Column \''.$name.'\' is missing]';
						http_response_code(500);
					} elseif ($givenStructure[$name] != $type) {
						$errorMessages[] = 'Column \''.$name.'\' of table \'packages\' does not match the needed type. [IS: '.$givenStructure[$name].', SHOULD: '.$type.']';
						http_response_code(500);
					}
				}
				
			} else {
				$errorMessages[] = 'Database table \'packages\' does not match the needed scheme. [Col count doesn\'t match]';
				http_response_code(500);
			}
			
		} else {
			$errorMessages[] = 'Error while accessing the database. ['.$db->lastErrorMsg ().']';
			http_response_code(500);
		}
		
	} catch (Exception $e) {
		$errorMessages[] = 'No database available. Did you set up the system correctly?';
		http_response_code(500);
	}
	
	if (!isset($_GET['room']) or !is_string($_GET['room'])) {
		$errorMessages[] = 'Please select a valid room.';
		http_response_code(400);
	}
	
	
	// check whether this client is the moderator
	if (isset($_GET['moderatorPassphrase'])) {
		if (!($passphrase = @file_get_contents(dirname(__FILE__).'/.htedumonpassword'))) {
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
	
	
	// if everything is okay, we can start 
	if (http_response_code() == 200) {
	
		// delete old database entries
		$stmt = $db->prepare("delete from packages where time < :timestamp");
		$stmt->bindValue(':timestamp', time()-24*3600, SQLITE3_INTEGER);
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
		if (is_array($outbox) and count($outbox) > 0) {
			foreach ($outbox as $key => $package) {
				// check package header on a basic level
				
				// if there is no array, we are wrong -> continue with next package
				if (!is_array($package)) {
					$errorMessages[] = 'Package '.$key.' is no array. Ignored package.';
					continue;
				}
				
				// needed field types
				$packageValidation = array(
					'integer' => array('type', 'id', 'time'),
					'string' => array('from', 'to', 'room'),
					'array' => array('body')
				);
				
				$packageError = false;
				
				// save all validated data into new array, so we now there are no additional fields
				$validatedPackage = array();
				
				foreach($packageValidation as $type => $fields) {
					foreach($fields as $field) {
						// If field doesn't exist, save error and continue with next one
						if (!isset($package[$field])) {
							$packageError = true;
							$errorMessages[] = 'Field "'.$field.'" in package '.$key.' is missing.';
							continue;
						}
						// check type of field
						if (gettype($package[$field]) != $type) {
							$packageError = true;
							$errorMessages[] = 'Type of field "'.$field.'" in package '.$key.' is invalid.'
								.'(IS '.gettype($package[$field]).', SHOULD: '.$type.')';
							continue;
						}
						$validatedPackage[$field] = $package[$field];
					}
				}
				
				// if an error is registered, continue with next package
				if ($packageError) {
					$errorMessages[] = 'Ignored package '.$key.' because of errors.';
					continue;
				}
				
				// Moderator's session id will be replaced
				if ($_SESSION['isModerator']) {
					$package['from'] = 'MODERATOR';
				}
				
				// Don't send from other clients...
				if ($package['from'] != $clientId) {
					$errorMessages[] = 'You are not client "'.$package['from'].'" (Package '.$key.').';
					continue;
				}
				
				// Don't send packages to yourself...
				if ($package['to'] == $clientId) {
					$errorMessages[] = 'You cannot send a package to yourself (Package '.$key.').';
					continue;
				}
				
				// Only the moderator can broadcast and send to clients. Hence, clients can only send to MODERATOR
				if ($package['to'] != 'MODERATOR' and !$_SESSION['isModerator']) {
					$errorMessages[] = 'Participants can only send packages to MODERATOR (Package '.$key.').';
					continue;
				}
				
				// send packages only to own room
				if ($package['room'] != $_GET['room']) {
					$errorMessages[] = 'Only packages inside the room are allowed [You are in room "'.$_GET['room'].'" and wanted to send something to "'.$package['room'].'"] (Package '.$key.').';
					continue;
				}
				
				// otherwise, we can continue
				$package = $validatedPackage;
				unset($validatedPackage);
				
				/*
					If we are here, the package has the right rough structure
					We will NOT check the body's structure now. This work should be done by the receiver
				*/
				
				$stmt = $db->prepare("insert into packages (room, to_client, data) values (:room, :toClient, :data)");
				
				$stmt->bindValue(':room', $package['room'], SQLITE3_TEXT) or die($db->lastErrorMsg());
				$stmt->bindValue(':toClient', $package['to'], SQLITE3_TEXT);
				$stmt->bindValue(':data', json_encode($package, JSON_UNESCAPED_UNICODE), SQLITE3_TEXT);
				
				if (!$stmt->execute()) {
					$errorMessages[] = 'Could not save package '.$key.' to database. [Error: "'.$db->lastErrorMsg().'"]';
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
			if ($stmt = $db->prepare("select id, to_client, data from packages where room = :room and (id > :lastId and (to_client = :clientId or to_client = 'BROADCAST'))")) {
				$stmt->bindValue(':clientId', $clientId, SQLITE3_TEXT);
			}
		} else {
			$stmt = $db->prepare("select id, to_client, data from packages where room = :room and (id > :lastId and to_client = 'MODERATOR')");
		}
		
		$stmt->bindValue(':room', $_GET['room'], SQLITE3_TEXT);
		$stmt->bindValue(':lastId', $_SESSION['lastDeliveredPackage'], SQLITE3_INTEGER);
		
		$result = $stmt->execute();
		$receivedPackages = array();
		// remember delivered ids to delete later
		$deliveredIds = array();
		
		while($row = $result->fetchArray(SQLITE3_ASSOC)) {
			$receivedPackages[] = json_decode($row['data'], true);
			
			// Broadcasts should not be deleted
			if ($row['to_client'] != 'BROADCAST') {
				$deliveredIds[] = $row['id'];
			}
			
			$_SESSION['lastDeliveredPackage'] = $row['id'];
		}
		
		// delete delivered ones
		$stmt = $db->prepare("delete from packages where id in (".implode(',', $deliveredIds).")");
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
