<?php
	/**
	
		This is the mailbox of edumon. Please access it as follows and remeber to hold the session!
		
		Your request needs to contain the following POST data:
		
			- room [String] : The room, inside which you are at the moment
			- moderatorPassphrase [String] : If you want to authenticate as a moderator, you have to send this passphase (needed only once in a session, to log off, just send an incorrect passphrase)
			- outbox [Array] an array of packages you want to send. It needs the following fields and types:
				- type [int] : the package's type
				- id [int] : the package's id
				- time [int] : the packages timestamp
				- from [String] : the sender (your SESSION_ID)
				- to [String] : the receiver's SESSION_ID or "MODERATOR" or "BROADCAST"
				- room [String] : the room you are in
				- body [Array] : the packages body
		
		The following data will be returned as JSON:
		
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


	header('Access-Control-Allow-Origin: *');
	header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
    header('Access-Control-Allow-Credentials: true');
	header('Content-type: application/json; charset=utf-8');
	
	session_start();
	
	$errorMessages = array();
	
	// try to get access to the database
	if (!($db = new SQLite3(dirname(__FILE__).'/.htedumondatabase'))) {
		die('No database available. Did you set up the system correctly?');
	}
	
	if (!isset($_POST['room']) or !is_string($_POST['room'])) {
		die('Please select a valid room.');
	}
	
	// delete old database entries
	$stmt = $db->prepare('delete from packages where time < :timestamp');
	$stmt->bindValue(':timestamp', time()-24*3600, SQLITE3_INTEGER);
	$stmt->execute();
	
	// check whether this client is the moderator
	if (isset($_POST['moderatorPassphrase'])) {
		if (!($passphrase = file_get_contents(dirname(__FILE__).'/.htedumonpassword'))) {
			die('No password file available. Did you set up the system correctly?');
		} else {
			$passphrase = trim($passphrase);
		}
		
		$_SESSION['isModerator'] = ($passphrase == $_POST['moderatorPassphrase']);
		
	} elseif (!isset($_SESSION['isModerator'])) {
		$_SESSION['isModerator'] = false;
	}
	
	// No one should be able to see the moderator's session id
	if ($_SESSION['isModerator']) {
		$clientId = 'MODERATOR';
	} else {
		$clientId = session_id();
	}
	
	// check, whether there are packages to send
	$outbox = array();
	
	if (isset($_POST['outbox'])) {
		$outbox = json_decode($_POST['outbox'], true);
		
		if ($outbox == null) {
			$errorMessages[] = 'Couldn\'nt parse given JSON.';
			$outbox = array();
		}
	}
	
	// at least one entry? we have to send some packages
	if (count($outbox) > 0) {
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
			if ($package['room'] != $_POST['room']) {
				$errorMessages[] = 'Only packages inside the room are allowed [You are in room "'.$_POST['room'].'" and wanted to send something to "'.$package['room'].'"] (Package '.$key.').';
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
		$stmt = $db->prepare("select id, to_client, data from packages where room = :room and (id > :lastId and (to_client = :clientId or to_client = 'BROADCAST'))");
		$stmt->bindValue(':clientId', $clientId, SQLITE3_TEXT);
	} else {
		$stmt = $db->prepare("select id, to_client, data from packages where room = :room and (id > :lastId and to_client = 'MODERATOR')");
	}
	
	$stmt->bindValue(':room', $_POST['room'], SQLITE3_TEXT);
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
	
	echo json_encode($returnedData, JSON_UNESCAPED_UNICODE);
