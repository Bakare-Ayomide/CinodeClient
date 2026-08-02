<?php
/**
 * Automated Database Installer & Migration Script for CINJELLY REST API
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: text/html; charset=utf-8');
require_once __DIR__ . '/config/config.php';

echo "<h2>CINJELLY MySQL Migration Tool</h2>";
echo "<p>Connecting to MySQL database <strong>" . DB_NAME . "</strong>...</p>";

try {
    $pdo = get_db_connection();
    $driver = $pdo->getAttribute(PDO::ATTR_DRIVER_NAME);
    echo "<p style='color:green;'>✓ Successfully connected using <strong>{$driver}</strong> driver!</p>";

    $schemaFile = __DIR__ . '/database/schema.sql';
    if (!file_exists($schemaFile)) {
        die("<p style='color:red;'>Error: schema.sql file not found at {$schemaFile}</p>");
    }

    $sql = file_get_contents($schemaFile);
    
    // Split multi-statement SQL safely
    $statements = array_filter(array_map('trim', explode(';', $sql)));
    foreach ($statements as $stmt) {
        if (!empty($stmt)) {
            $pdo->exec($stmt);
        }
    }

    echo "<p style='color:green; font-weight:bold;'>✓ MySQL Tables, Foreign Keys, and Default Seed Data created successfully!</p>";

    // Live sync from Jellyfin Server API into MySQL users table
    echo "<h3>Syncing Jellyfin Server Users into MySQL...</h3>";
    $jfResult = call_jellyfin_admin_api('Users', 'GET');
    if ($jfResult['code'] === 200 && is_array($jfResult['response'])) {
        $stmtInsert = $pdo->prepare("INSERT INTO `users` (`username`, `email`, `password_hash`, `full_name`, `jellyfin_user_id`, `is_admin`, `is_premium`) 
            VALUES (:username, :email, :password_hash, :full_name, :jellyfin_user_id, :is_admin, 1)
            ON DUPLICATE KEY UPDATE `jellyfin_user_id` = VALUES(`jellyfin_user_id`), `is_admin` = VALUES(`is_admin`)");
        
        foreach ($jfResult['response'] as $u) {
            $uName = $u['Name'] ?? 'unknown';
            $uId = $u['Id'] ?? null;
            $isAdmin = !empty($u['Policy']['IsAdministrator']) ? 1 : 0;
            $email = strtolower($uName) . "@cinode.zerolord.com";
            $dummyHash = password_hash('@f33rinimi', PASSWORD_BCRYPT);
            $stmtInsert->execute([
                ':username' => $uName,
                ':email' => $email,
                ':password_hash' => $dummyHash,
                ':full_name' => $uName,
                ':jellyfin_user_id' => $uId,
                ':is_admin' => $isAdmin
            ]);
        }
        echo "<p style='color:green;'>✓ Successfully synced " . count($jfResult['response']) . " users live from Jellyfin API into MySQL!</p>";
    }

    // Display Current System Credentials in Database
    $stmtConfig = $pdo->query("SELECT * FROM `system_config` ORDER BY `config_key` ASC");
    $configs = $stmtConfig->fetchAll();
    echo "<h3>System Configuration in MySQL Database:</h3><table border='1' cellpadding='6' cellspacing='0'><tr><th>Config Key</th><th>Config Value</th></tr>";
    foreach ($configs as $cfg) {
        $valDisplay = (strpos($cfg['config_key'], 'PASS') !== false || strpos($cfg['config_key'], 'SECRET') !== false || strpos($cfg['config_key'], 'KEY') !== false)
            ? htmlspecialchars($cfg['config_value'])
            : htmlspecialchars($cfg['config_value']);
        echo "<tr><td><strong>" . htmlspecialchars($cfg['config_key']) . "</strong></td><td>{$valDisplay}</td></tr>";
    }
    echo "</table>";

    // Display Users in MySQL Database
    $stmtUsers = $pdo->query("SELECT `id`, `username`, `email`, `jellyfin_user_id`, `is_admin`, `is_premium`, `created_at` FROM `users` ORDER BY `id` ASC");
    $dbUsers = $stmtUsers->fetchAll();
    echo "<h3>Users in MySQL Database:</h3><table border='1' cellpadding='6' cellspacing='0'><tr><th>ID</th><th>Username</th><th>Email</th><th>Jellyfin User ID</th><th>Is Admin</th><th>Is Premium</th><th>Created At</th></tr>";
    foreach ($dbUsers as $usr) {
        echo "<tr>
            <td>" . htmlspecialchars($usr['id']) . "</td>
            <td><strong>" . htmlspecialchars($usr['username']) . "</strong></td>
            <td>" . htmlspecialchars($usr['email']) . "</td>
            <td>" . htmlspecialchars($usr['jellyfin_user_id'] ?? 'N/A') . "</td>
            <td>" . ($usr['is_admin'] ? 'YES (1)' : 'NO (0)') . "</td>
            <td>" . ($usr['is_premium'] ? 'YES (1)' : 'NO (0)') . "</td>
            <td>" . htmlspecialchars($usr['created_at']) . "</td>
        </tr>";
    }
    echo "</table>";

    if ($driver === 'mysql') {
        $stmt = $pdo->query("SHOW TABLES;");
        $tables = $stmt->fetchAll(PDO::FETCH_COLUMN);
        echo "<h3>Tables in Database:</h3><ul>";
        foreach ($tables as $t) {
            echo "<li><strong>{$t}</strong></li>";
        }
        echo "</ul>";
    }

} catch (Throwable $e) {
    echo "<p style='color:red;'>Migration Exception: " . htmlspecialchars($e->getMessage()) . "</p>";
    echo "<pre>" . htmlspecialchars($e->getTraceAsString()) . "</pre>";
}

