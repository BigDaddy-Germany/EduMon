<?php
	require_once(dirname(__FILE__).'/config.php');
	
	$_POST['recipient'] = $_GET['r'];
	$_POST['package'] = $_GET['p'];
	
	if (isset($_POST['recipient']) and isset($_POST['package']))
	{
		$stmnt = $mysqli->prepare('insert into `'.SQL_PREFIX.'mails` (`recipient`, `data`) values (?, ?)');
		$stmnt->bind_param('is', $_POST['recipient'], $_POST['package']);
		if ($stmnt->execute())
		{
			echo 'success';
		}
		else
		{
			echo 'Error while executing statement: '.$stmnt->error;
		}
	} 
	else 
	{
		echo 'No data received.';
	}