<?php
/**
 * CINJELLY PHP REST API - Jellyfin Library Scan Endpoint
 */

require_once __DIR__ . '/../../config/config.php';

$result = call_jellyfin_admin_api('Library/Refresh', 'POST');
json_response(true, null, "Library scan triggered successfully on Jellyfin server.");
