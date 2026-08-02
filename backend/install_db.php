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

