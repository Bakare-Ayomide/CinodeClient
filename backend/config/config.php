<?php
/**
 * CINJELLY PHP REST API - Main Configuration & Helper Services
 * Encapsulates all server secrets (Jellyfin Admin credentials, Monnify Secret Key, JWT secrets)
 * and isolates them from the client APK.
 */

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Load Environment Variables from backend/.env if present
$envFile = __DIR__ . '/../.env';
if (file_exists($envFile)) {
    $lines = file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue;
        list($name, $value) = explode('=', $line, 2) + [null, null];
        if ($name && $value !== null) {
            $_ENV[trim($name)] = trim($value);
            putenv(trim($name) . '=' . trim($value));
        }
    }
}

function get_env($key, $default = '') {
    $val = getenv($key);
    if ($val !== false && $val !== '') return $val;
    if (isset($_ENV[$key]) && $_ENV[$key] !== '') return $_ENV[$key];
    return $default;
}

// Global Backend Configuration (SENSITIVE - Server side only!)
define('DB_HOST', get_env('DB_HOST', '105.113.98.181'));
define('DB_PORT', get_env('DB_PORT', '3306'));
define('DB_NAME', get_env('DB_NAME', 'zerolord_cinback'));
define('DB_USER', get_env('DB_USER', 'zerolord_cinback'));
define('DB_PASS', get_env('DB_PASS', '@f33rinimi'));

define('JELLYFIN_SERVER_URL', rtrim(get_env('JELLYFIN_SERVER_URL', 'https://cinode.zerolord.com'), '/'));
define('JELLYFIN_FALLBACK_URL', rtrim(get_env('JELLYFIN_FALLBACK_URL', 'http://163.245.193.7:8096'), '/'));
define('JELLYFIN_ADMIN_USER', get_env('JELLYFIN_ADMIN_USER', 'duwit'));
define('JELLYFIN_ADMIN_PASS', get_env('JELLYFIN_ADMIN_PASS', '@f33rinimi'));
define('JELLYFIN_API_KEY', get_env('JELLYFIN_API_KEY', '79ee2e15ee1f47fd881188ef4da13391'));

define('MONNIFY_API_KEY', get_env('MONNIFY_API_KEY', 'MK_PROD_123456789'));
define('MONNIFY_SECRET_KEY', get_env('MONNIFY_SECRET_KEY', 'SK_PROD_987654321'));
define('MONNIFY_CONTRACT_CODE', get_env('MONNIFY_CONTRACT_CODE', '1234567890'));
define('MONNIFY_USE_SANDBOX', get_env('MONNIFY_USE_SANDBOX', 'true') === 'true');

define('JWT_SECRET', get_env('JWT_SECRET', 'cinjelly_jwt_secret_key_2026_super_secure'));

/**
 * Database Connection Helper (PDO MySQL with SQLite fallback)
 */
function get_db_connection() {
    static $pdo = null;
    if ($pdo !== null) return $pdo;

    $hostsToTry = array_unique([DB_HOST, 'localhost', '127.0.0.1']);
    
    foreach ($hostsToTry as $host) {
        try {
            $dsn = "mysql:host=" . $host . ";port=" . DB_PORT . ";dbname=" . DB_NAME . ";charset=utf8mb4";
            $pdo = new PDO($dsn, DB_USER, DB_PASS, [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_TIMEOUT => 5
            ]);
            return $pdo;
        } catch (PDOException $e) {
            // Try next host in loop
        }
    }

    try {
        // Fallback to SQLite database file if MySQL server is not locally accessible
        $sqlitePath = __DIR__ . '/../database/cinjelly_fallback.sqlite';
        $pdo = new PDO("sqlite:" . $sqlitePath);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

        // Auto initialize SQLite tables if empty
        $pdo->exec("CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE,
            email TEXT UNIQUE,
            password_hash TEXT,
            full_name TEXT,
            jellyfin_user_id TEXT,
            jellyfin_access_token TEXT,
            is_admin INTEGER DEFAULT 0,
            is_premium INTEGER DEFAULT 0,
            preferred_quality TEXT DEFAULT '4K Ultra HD',
            subscription_expires_at TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT DEFAULT CURRENT_TIMESTAMP
        )");
        $pdo->exec("CREATE TABLE IF NOT EXISTS subscriptions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            plan_name TEXT,
            amount_paid REAL,
            currency TEXT DEFAULT 'NGN',
            payment_reference TEXT UNIQUE,
            transaction_reference TEXT,
            status TEXT DEFAULT 'PENDING',
            created_at TEXT DEFAULT CURRENT_TIMESTAMP
        )");
        $pdo->exec("CREATE TABLE IF NOT EXISTS transactions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            payment_reference TEXT,
            transaction_reference TEXT,
            amount REAL,
            status TEXT,
            raw_payload TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP
        )");
        return $pdo;
    } catch (Throwable $err) {
        die("Database Connection Error: " . $err->getMessage());
    }
}

/**
 * Send Standard JSON Response
 */
function json_response($isSuccess, $data = null, $message = "", $code = 200) {
    http_response_code($code);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'isSuccess' => (bool)$isSuccess,
        'message' => (string)$message,
        'data' => $data
    ], JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
    exit();
}

/**
 * Get Request JSON Input
 */
function get_json_input() {
    $raw = file_get_contents('php://input');
    if (empty($raw)) return $_REQUEST;
    $data = json_decode($raw, true);
    return is_array($data) ? array_merge($_REQUEST, $data) : $_REQUEST;
}

/**
 * Generate Simple JWT Token
 */
function generate_jwt($userId, $username, $isAdmin = false) {
    $header = base64_encode(json_encode(['alg' => 'HS256', 'typ' => 'JWT']));
    $payload = base64_encode(json_encode([
        'sub' => $userId,
        'username' => $username,
        'is_admin' => $isAdmin,
        'iat' => time(),
        'exp' => time() + (86400 * 30) // 30 days valid
    ]));
    $signature = hash_hmac('sha256', "$header.$payload", JWT_SECRET, true);
    $jwtSig = base64_encode($signature);
    return "$header.$payload.$jwtSig";
}

/**
 * Authenticate with Jellyfin Admin API to obtain Admin Authorization Token
 */
function get_jellyfin_admin_token() {
    static $cachedAdminToken = null;
    if ($cachedAdminToken !== null) return $cachedAdminToken;

    $url = JELLYFIN_SERVER_URL . '/Users/AuthenticateByName';
    $authHeader = 'MediaBrowser Client="Cinode Backend API", Device="PHP Server", DeviceId="cinode_backend_server", Version="1.0.0"';
    $body = json_encode([
        'Username' => JELLYFIN_ADMIN_USER,
        'Pw' => JELLYFIN_ADMIN_PASS
    ]);

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $body);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'X-Emby-Authorization: ' . $authHeader
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 8);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode === 200 && $response) {
        $json = json_decode($response, true);
        if (isset($json['AccessToken'])) {
            $cachedAdminToken = $json['AccessToken'];
            return $cachedAdminToken;
        }
    }

    return JELLYFIN_API_KEY;
}

/**
 * Call Jellyfin Admin API Endpoint safely from PHP
 */
function call_jellyfin_admin_api($endpoint, $method = 'GET', $data = null) {
    $adminToken = get_jellyfin_admin_token();
    $serverUrl = JELLYFIN_SERVER_URL;
    $url = $serverUrl . '/' . ltrim($endpoint, '/');

    // Attach api_key query param
    $separator = (strpos($url, '?') !== false) ? '&' : '?';
    $url .= $separator . 'api_key=' . urlencode($adminToken);

    $authHeader = 'MediaBrowser Client="Cinode Backend API", Device="PHP Server", DeviceId="cinode_backend", Version="1.0.0", Token="' . $adminToken . '"';

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'X-Emby-Authorization: ' . $authHeader
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);

    if ($data !== null && in_array($method, ['POST', 'PUT'])) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, is_string($data) ? $data : json_encode($data));
    }

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    return [
        'code' => $httpCode,
        'response' => json_decode($response, true) ?: $response
    ];
}
