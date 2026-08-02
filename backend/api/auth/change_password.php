<?php
/**
 * CINJELLY PHP REST API - Password Change Endpoint
 * Updates password in MySQL and Jellyfin server.
 */

require_once __DIR__ . '/../../config/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response(false, null, "Method not allowed.", 405);
}

$input = get_json_input();
$username = trim($input['username'] ?? '');
$currentPassword = $input['current_password'] ?? '';
$newPassword = $input['new_password'] ?? '';

if (empty($username) || empty($newPassword)) {
    json_response(false, null, "Username and new password are required.", 400);
}

if (strlen($newPassword) < 4) {
    json_response(false, null, "Password must be at least 4 characters long.", 400);
}

$db = get_db_connection();
$stmt = $db->prepare("SELECT * FROM users WHERE username = :u");
$stmt->execute([':u' => $username]);
$user = $stmt->fetch();

if ($user && !empty($currentPassword)) {
    if (!password_verify($currentPassword, $user['password_hash'])) {
        json_response(false, null, "Current password is incorrect.", 401);
    }
}

$newHash = password_hash($newPassword, PASSWORD_BCRYPT);
$updateStmt = $db->prepare("UPDATE users SET password_hash = :p WHERE username = :u");
$updateStmt->execute([':p' => $newHash, ':u' => $username]);

// Call Jellyfin user password update if jellyfin_user_id exists
if (!empty($user['jellyfin_user_id'])) {
    call_jellyfin_admin_api("Users/" . $user['jellyfin_user_id'] . "/Password", "POST", [
        "Id" => $user['jellyfin_user_id'],
        "NewPw" => $newPassword,
        "ResetPassword" => false
    ]);
}

json_response(true, null, "Password updated successfully.");
