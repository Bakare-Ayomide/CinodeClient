<?php
/**
 * CINJELLY PHP REST API - Health Check & System Status Endpoint
 */

require_once __DIR__ . '/config/config.php';

json_response(true, [
    'app_name' => 'CINJELLY PHP REST API',
    'version' => '2.0.0',
    'status' => 'ONLINE',
    'backend_engine' => 'PHP ' . PHP_VERSION,
    'database' => 'MySQL (cinjelly_db)',
    'jellyfin_server' => JELLYFIN_SERVER_URL,
    'monnify_gateway' => MONNIFY_USE_SANDBOX ? 'SANDBOX' : 'LIVE',
    'timestamp' => date('Y-m-d H:i:s')
], "CINJELLY PHP REST API Backend is Operational.");
