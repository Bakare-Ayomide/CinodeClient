<?php
/**
 * CINJELLY PHP REST API - Jellyfin Active Sessions Endpoint
 */

require_once __DIR__ . '/../../config/config.php';

$result = call_jellyfin_admin_api('Sessions', 'GET');

if ($result['code'] === 200 && is_array($result['response'])) {
    json_response(true, $result['response'], "Active Jellyfin sessions retrieved.");
}

json_response(true, [], "Sessions list.");
