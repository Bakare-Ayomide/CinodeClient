<?php
/**
 * CINJELLY PHP REST API - User Profile & Update Endpoint
 */

require_once __DIR__ . '/../../config/config.php';

$input = get_json_input();
$username = trim($input['username'] ?? $_GET['username'] ?? '');

if (empty($username)) {
    json_response(false, null, "Username is required.", 400);
}

$db = get_db_connection();

if ($_SERVER['REQUEST_METHOD'] === 'POST' || $_SERVER['REQUEST_METHOD'] === 'PUT') {
    $name = trim($input['full_name'] ?? $input['name'] ?? '');
    $email = trim(strtolower($input['email'] ?? ''));
    $quality = trim($input['preferred_quality'] ?? '4K Ultra HD');

    $stmt = $db->prepare("UPDATE users SET full_name = COALESCE(NULLIF(:n, ''), full_name), email = COALESCE(NULLIF(:e, ''), email), preferred_quality = COALESCE(NULLIF(:q, ''), preferred_quality) WHERE username = :u");
    $stmt->execute([':n' => $name, ':e' => $email, ':q' => $quality, ':u' => $username]);

    json_response(true, null, "Profile updated successfully.");
}

$stmt = $db->prepare("SELECT id, username, email, full_name, is_admin, is_premium, preferred_quality, subscription_expires_at, created_at FROM users WHERE username = :u");
$stmt->execute([':u' => $username]);
$user = $stmt->fetch();

if (!$user) {
    json_response(false, null, "User not found.", 404);
}

json_response(true, [
    'user_id' => (int)$user['id'],
    'username' => $user['username'],
    'email' => $user['email'],
    'full_name' => $user['full_name'],
    'is_admin' => (bool)($user['is_admin'] == 1),
    'is_premium' => (bool)($user['is_premium'] == 1),
    'preferred_quality' => $user['preferred_quality'],
    'subscription_expires_at' => $user['subscription_expires_at']
], "User profile retrieved.");
