<?php
set_exception_handler(function($e): never {
    header('Location: 500.php');
    exit;
});
?>
<?php 
$host = 'localhost';
$db = 'simulacra';
$user = 'root';
$pass = '';

$dsn = 'mysql:host=$host;dbname=$db;charset=utf8mb4';
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
];
try {
    $pdo = new PDO(dsn: $dsn, username: $user, password: $pass, options: $options);
} catch (PDOException $e) {
    throw new PDOException(message: $e->getMessage(), code: (int)$e->getCode());
}
