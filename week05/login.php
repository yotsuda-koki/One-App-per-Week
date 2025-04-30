<?php
session_start();

$loginError = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $user = $_POST['username'] ?? '';
    $pass = $_POST['password'] ?? '';

    if ($user === 'admin' && $pass === 'simulacra123') {
        $_SESSION['logged_in'] = true;
        header(header: 'Location: admin.php');
        exit;
    } else {
        $loginError = 'Invalid credentials.';
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="icon" type="image/x-icon" href="assets/img/favicon.ico">
</head>
<body>

<h2>Admin Login</h2>
<?php if ($loginError): ?>
  <p style="color:red;"><?= $loginError ?></p>
<?php endif; ?>

<form method="POST">
    <label>Username: <input type="text" name="username"></label><br>
    <label>Password: <input type="password" name="password"></label><br>
    <button type="submit">Login</button>
</form>

</body>
</html>