<?php 
require 'config.php';
require 'auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: 403.php');
    exit;
}

header('Content-Type: text/csv; charset=utf-8');
header('Content-Disposition: attachment; filename=inquiries.csv');

$output = fopen('php://output', 'w');
fputcsv($output, ['Name', 'Email', 'Message', 'Date']);

$stmt = $pdo->query("SELECT name, email, message, created_at FROM inquiries");
while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    fputcsv($output, $row);
}

fclose($output);
