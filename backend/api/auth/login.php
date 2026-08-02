<?php
/**
 * CINJELLY PHP REST API - User Login Endpoint
 * Validates credentials against MySQL DB and authenticates with Jellyfin.
 */

require_once __DIR__ . '/../../config/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response(false, null, "Method not allowed.", 405);
}

$input = get_json_input();

$username = trim($input['username'] ?? $input['email'] ?? '');
$password = $input['password'] ?? '';

if (empty($username)) {
    json_response(false, null, "Username or email is required.", 400);
}

$db = get_db_connection();

// Check MySQL Database for User
$stmt = $db->prepare("SELECT * FROM users WHERE username = :u OR email = :u LIMIT 1");
$stmt->execute([':u' => $username]);
$user = $stmt->fetch();

$isAdminEnv = (strcasecmp($username, JELLYFIN_ADMIN_USER) === 0);

if (!$user && !$isAdminEnv) {
    // If user does not exist in MySQL DB, try authenticating directly with Jellyfin server
    $authHeader = 'MediaBrowser Client="Cinode Backend API", Device="PHP Server", DeviceId="cinode_backend", Version="1.0.0"';
    $jfAuthRes = call_jellyfin_admin_api('Users/AuthenticateByName', 'POST', [
        'Username' => $username,
        'Pw' => $password
    ]);

    if (isset($jfAuthRes['response']['AccessToken'])) {
        $jfUser = $jfAuthRes['response']['User'] ?? [];
        $jfUserId = $jfUser['Id'] ?? 'user_1';
        $isAdmin = $jfUser['Policy']['IsAdministrator'] ?? false;
        $passwordHash = password_hash($password, PASSWORD_BCRYPT);

        // Auto provision user in MySQL
        $insertStmt = $db->prepare("INSERT INTO users (username, email, password_hash, jellyfin_user_id, is_admin) VALUES (:u, :e, :p, :jf, :a)");
        $insertStmt->execute([
            ':u' => $username,
            ':e' => $username . '@cinode.stream',
            ':p' => $passwordHash,
            ':jf' => $jfUserId,
            ':a' => $isAdmin ? 1 : 0
        ]);
        $newId = $db->lastInsertId();

        $token = generate_jwt($newId, $username, $isAdmin);
        json_response(true, [
            'user_id' => (int)$newId,
            'username' => $username,
            'email' => $username . '@cinode.stream',
            'full_name' => $username,
            'is_admin' => (bool)$isAdmin,
            'is_premium' => false,
            'jellyfin_user_id' => $jfUserId,
            'token' => $token,
            'server_url' => JELLYFIN_SERVER_URL
        ], "Login successful.");
    } else {
        json_response(false, null, "Invalid username or password.", 401);
    }
}

if ($user) {
    if (!empty($password) && !password_verify($password, $user['password_hash'])) {
        json_response(false, null, "Invalid username or password.", 401);
    }

    $userId = (int)$user['id'];
    $isAdmin = (bool)($user['is_admin'] == 1 || $isAdminEnv);
    $token = generate_jwt($userId, $user['username'], $isAdmin);

    json_response(true, [
        'user_id' => $userId,
        'username' => $user['username'],
        'email' => $user['email'],
        'full_name' => $user['full_name'] ?: $user['username'],
        'is_admin' => $isAdmin,
        'is_premium' => (bool)($user['is_premium'] == 1),
        'jellyfin_user_id' => $user['jellyfin_user_id'] ?: 'jf_' . $userId,
        'token' => $token,
        'server_url' => JELLYFIN_SERVER_URL
    ], "Login successful.");
}

// Fallback for Admin Env user
if ($isAdminEnv) {
    $token = generate_jwt(1, JELLYFIN_ADMIN_USER, true);
    json_response(true, [
        'user_id' => 1,
        'username' => JELLYFIN_ADMIN_USER,
        'email' => JELLYFIN_ADMIN_USER . '@cinode.stream',
        'full_name' => 'Server Administrator',
        'is_admin' => true,
        'is_premium' => true,
        'jellyfin_user_id' => 'admin_user_id',
        'token' => $token,
        'server_url' => JELLYFIN_SERVER_URL
    ], "Admin login successful.");
}
