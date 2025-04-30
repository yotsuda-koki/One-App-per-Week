<?php
session_start();
if (!($_SESSION['logged_in'] ?? false)) {
    header('Location: 403.php');
    exit;
}
