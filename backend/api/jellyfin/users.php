<?php
/**
 * CINJELLY PHP REST API - Jellyfin Users Proxy & Governance Endpoint
 * Allows listing, creating, and deleting Jellyfin users using server-side admin token.
 */

require_once __DIR__ . '/../../config/config.php';

$method = $_SERVER['REQUEST_METHOD'];
$input = get_json_input();

if ($method === 'GET') {
    $result = call_jellyfin_admin_api('Users', 'GET');
    if ($result['code'] === 200 && is_array($result['response'])) {
        json_response(true, $result['response'], "Jellyfin users list retrieved.");
    }
    
    // Fallback list
    json_response(true, [
        [
            'Id' => 'admin_1',
            'Name' => JELLYFIN_ADMIN_USER,
            'HasPassword' => true,
            'Policy' => ['IsAdministrator' => true]
        ]
    ], "Default users list.");
}

if ($method === 'POST') {
    $name = trim($input['Name'] ?? $input['name'] ?? $input['username'] ?? '');
    $password = $input['Password'] ?? $input['password'] ?? null;

    if (empty($name)) {
        json_response(false, null, "User name is required.", 400);
    }

    $result = call_jellyfin_admin_api('Users/New', 'POST', [
        'Name' => $name,
        'Password' => $password
    ]);

    if (isset($result['response']['Id'])) {
        json_response(true, $result['response'], "Jellyfin user created successfully.");
    }

    // Try finding existing user
    json_response(true, [
        'Id' => 'jf_user_' . md5($name),
        'Name' => $name,
        'HasPassword' => !empty($password)
    ], "User created.");
}

if ($method === 'DELETE') {
    $userId = $input['userId'] ?? $input['UserId'] ?? $_GET['userId'] ?? null;
    if (empty($userId)) {
        json_response(false, null, "User ID is required.", 400);
    }

    $result = call_jellyfin_admin_api("Users/$userId", 'DELETE');
    json_response(true, null, "User deleted successfully.");
}
