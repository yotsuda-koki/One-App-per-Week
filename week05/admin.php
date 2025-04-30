<?php require 'auth.php'; ?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin</title>
    <link rel="icon" type="image/x-icon" href="assets/img/favicon.ico">
</head>
<body>

<h2>Admin Panel</h2>
<p>Export all inquiries submitted through the contact form.</p>

<form action="./export.php" method="POST">
    <button type="submit">Export CSV</button>
</form>

<p><a href="logout.php">Logout</a></p>

</body>
</html>
