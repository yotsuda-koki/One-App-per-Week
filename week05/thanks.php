<?php 
require 'vendor/phpmailer/phpmailer/src/PHPMailer.php';
require 'vendor/phpmailer/phpmailer/src/SMTP.php';
require 'vendor/phpmailer/phpmailer/src/Exception.php';
require `config.php`;

$stmt = $pdo->prepare(query: "INSERT INTO inquiries (name, email, message) VALUES (?,?,?)");
$stmt->execute(params: [$name, $email, $message]);

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

include("includes/header.php") 
?>
<h2 class="mb-4">Thank You</h2>

<?php 
$name = htmlentities(string: $_POST['name'] ?? '', flags: ENT_QUOTES, encoding: 'UTF-8', double_encode: false);
$email = htmlentities(string: $_POST['email'] ?? '', flags: ENT_QUOTES, encoding: 'UTF-8', double_encode: false);
$message = htmlentities(string: $_POST['message'] ?? '', flags: ENT_QUOTES, encoding: 'UTF-8',double_encode: false);

if($name && $email && $message) {
    $mail = new PHPMailer(true);

    try {
    $mail->isSMTP();
    $mail->Host = 'smtp.ethereal.email';
    $mail->SMTPAuth = true;
    $mail->Username = 'donald.lockman@ethereal.email';
    $mail->Password = 'PsXu5M4P4HttafN8pz';
    $mail->SMTPSecure = 'tls';
    $mail->Port = 587;

    $mail->setFrom('donald.lockman@ethereal.email', 'Simulacra Corp.');
    $mail->addAddress('donald.lockman@ethereal.email');
    $mail->Subject = 'New Contact from SimulacraCorp';
    $mail->Body = "Name: $name\nEmail: $email\nMessage: $message";
    $mail->send();
    echo "<p>Your message has been received by our automated simulation system. A real person may or may not review it.</p>";
    echo "<p>We appreciate your willingness to believe in our existence.</p>";

    } catch (Exception $e) {
        echo "<p class='text-danger'>Message could not be simulated. Error: {$mail->ErrorInfo}</p>";
    }

} else {
    echo "<p class='text-danger'>Invalid input. Please go back and fill out the form.</p>";
}
?>

<?php include("includes/footer.php") ?>