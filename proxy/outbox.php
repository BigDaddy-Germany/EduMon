<?php
	require_once(dirname(__FILE__).'/config.php');
	
	if (isset($_GET['id']))
	{
		$stmnt = $mysqli->prepare('select `id`, `data` from `'.SQL_PREFIX.'mails` where `recipient` = ?');
		
		$stmnt->bind_param('i', $_GET['id']);
		if ($stmnt->execute())
		{
			$result = $stmnt->get_result();
			
			$deliveredMails = array();
			
			while ($row = $result->fetch_array(MYSQLI_ASSOC))
			{
				echo $row['data']."\n";
				$deliveredMails[] = $row['id'];
			}
			
			$result->free();
			
			// dummy value, that will never ever occur in the database to prevent errors on empty array
			$deliveredMails[] = -1;
			
			$stmnt = $mysqli->prepare('delete from `'.SQL_PREFIX.'mails` where `id` in ('.implode(',', $deliveredMails).')');
			$stmnt->execute();
		}
		else
		{
			die('Error while executing statement: '.$stmnt->error());
		}
	} 
	else 
	{
		echo 'No ID provided.';
	}