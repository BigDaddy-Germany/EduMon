<?php
	$mysqli = mysqli_connect('localhost', 'root', '', 'edumon');

	// Check connection
	if ($mysqli->connect_errno)
	{
		die('Connection to database failed: '.($mysqli->connect_error));
	}

	define('SQL_PREFIX', 'edumon__');