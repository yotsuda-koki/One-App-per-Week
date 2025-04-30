<?php include("includes/header.php") ?>
<div class="container">
    <h2 class="mb-4">Contact Us</h2>
    <p class="text-muted">Please fill out the form below, our team may or may not get back to you, depending on simulated interest.</p>

    <form action="thanks.php" method="POST" class="needs-validation" novalidate>
        <div class="mb-3">
            <label for="name" class="from-label">Full name</label>
            <input type="text" class="form-control" id="name" name="name" required>
            <div class="invalid-feedback">Please enter your name</div>
        </div>

        <div class="mb-3">
            <label for="email" class="form-label">Email Address</label>
            <input type="email" class="form-control" id="email" name="email" required>
            <div class="invalid-feedback">Please provide a valid email.</div>
        </div>

        <div class="mb-3">
            <label for="message" class="form-label">Your message</label>
            <textarea name="message" id="message" class="form-control" rows="5" required></textarea>
            <div class="invalid-feedback">Please enter a message.</div>
        </div>

        <button type="submit" class="btn btn-custom">Send Message</button>
    </form>
</div>

<script>
    (() => {
        'use strict';
        const forms = document.querySelectorAll('.needs-validation');
        Array.from(forms).forEach(form => {
            form.addEventListener('submit', event => {
                if(!form.checkValidity()){
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        });
    })();
</script>
<?php include("includes/footer.php") ?>