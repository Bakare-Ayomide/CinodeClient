<?php
/**
 * CINJELLY PHP REST API - Jellyfin Activity Logs Endpoint
 */

require_once __DIR__ . '/../../config/config.php';

$result = call_jellyfin_admin_api('System/ActivityLog/Entries?Limit=20', 'GET');

if ($result['code'] === 200 && is_array($result['response'])) {
    json_response(true, $result['response'], "Activity logs retrieved.");
}

json_response(true, ['Items' => [], 'TotalRecordCount' => 0], "Activity log entries.");
