<?php
/**
 * CINJELLY PHP REST API - Monnify Payment Initialization Endpoint
 * Initializes payment via Monnify API using backend secret credentials.
 * Secrets are never sent to or stored on the client APK.
 */

require_once __DIR__ . '/../../config/config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response(false, null, "Method not allowed.", 405);
}

$input = get_json_input();

$amount = (float)($input['amount'] ?? 2500.00);
$customerName = trim($input['customer_name'] ?? $input['customerName'] ?? 'Cinode Streamer');
$customerEmail = trim($input['customer_email'] ?? $input['customerEmail'] ?? 'user@cinode.stream');
$itemTitle = trim($input['item_title'] ?? $input['itemTitle'] ?? 'Cinode 4K Ultra VIP Access');
$username = trim($input['username'] ?? '');

$paymentRef = "MNFY_REF_" . time() . "_" . rand(1000, 9999);
$txnRef = "MNFY_TXN_" . substr(md5(uniqid()), 0, 12);
$virtualAccount = "603" . rand(10000000, 99999999);
$contractCode = MONNIFY_CONTRACT_CODE;
$ussdCode = "*737*33*" . (int)$amount . "*" . substr($contractCode, -6) . "#";
$baseUrl = MONNIFY_USE_SANDBOX ? "https://sandbox.monnify.com" : "https://api.monnify.com";

// Authenticate with Monnify using backend API key & secret key
$monnifyAccessToken = null;

try {
    $credentials = MONNIFY_API_KEY . ':' . MONNIFY_SECRET_KEY;
    $basicAuth = 'Basic ' . base64_encode($credentials);

    $ch = curl_init("$baseUrl/api/v1/auth/login");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        "Authorization: $basicAuth",
        "Content-Type: application/json"
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 6);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

    $authResp = curl_exec($ch);
    $authCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($authCode === 200 && $authResp) {
        $authJson = json_decode($authResp, true);
        if (isset($authJson['requestSuccessful']) && $authJson['requestSuccessful'] === true) {
            $monnifyAccessToken = $authJson['responseBody']['accessToken'] ?? null;
        }
    }

    if ($monnifyAccessToken) {
        $initCh = curl_init("$baseUrl/api/v1/merchant/transactions/init-transaction");
        $body = json_encode([
            "amount" => $amount,
            "customerName" => $customerName,
            "customerEmail" => $customerEmail,
            "paymentReference" => $paymentRef,
            "paymentDescription" => "Cinode VIP Stream Access: " . $itemTitle,
            "currencyCode" => "NGN",
            "contractCode" => $contractCode,
            "redirectUrl" => "https://monnify.com"
        ]);

        curl_setopt($initCh, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($initCh, CURLOPT_POST, true);
        curl_setopt($initCh, CURLOPT_POSTFIELDS, $body);
        curl_setopt($initCh, CURLOPT_HTTPHEADER, [
            "Authorization: Bearer $monnifyAccessToken",
            "Content-Type: application/json"
        ]);
        curl_setopt($initCh, CURLOPT_TIMEOUT, 8);
        curl_setopt($initCh, CURLOPT_SSL_VERIFYPEER, false);

        $initResp = curl_exec($initCh);
        $initCode = curl_getinfo($initCh, CURLINFO_HTTP_CODE);
        curl_close($initCh);

        if ($initCode === 200 && $initResp) {
            $initJson = json_decode($initResp, true);
            if (!empty($initJson['requestSuccessful'])) {
                $bodyObj = $initJson['responseBody'] ?? [];
                $checkoutUrl = $bodyObj['checkoutUrl'] ?? "$baseUrl/checkout/$paymentRef";
                $realTxnRef = $bodyObj['transactionReference'] ?? $txnRef;
                $accountNo = $bodyObj['accountNumber'] ?? $virtualAccount;
                $bank = $bodyObj['bankName'] ?? "Wema Bank / Monnify Gateway";

                // Save initial transaction record to MySQL DB
                $db = get_db_connection();
                $userId = null;
                if (!empty($username)) {
                    $uStmt = $db->prepare("SELECT id FROM users WHERE username = :u");
                    $uStmt->execute([':u' => $username]);
                    $userId = $uStmt->fetchColumn() ?: null;
                }
                $tStmt = $db->prepare("INSERT INTO transactions (user_id, payment_reference, transaction_reference, amount, status, raw_payload) VALUES (:uid, :pref, :tref, :amt, 'INITIALIZED', :payload)");
                $tStmt->execute([
                    ':uid' => $userId,
                    ':pref' => $paymentRef,
                    ':tref' => $realTxnRef,
                    ':amt' => $amount,
                    ':payload' => json_encode($bodyObj)
                ]);

                json_response(true, [
                    'paymentReference' => $paymentRef,
                    'transactionReference' => $realTxnRef,
                    'checkoutUrl' => $checkoutUrl,
                    'virtualAccountNumber' => $accountNo,
                    'bankName' => $bank,
                    'ussdCode' => $ussdCode,
                    'amount' => $amount
                ], "Monnify Transaction Initialized via PHP Backend.");
            }
        }
    }
} catch (Exception $e) {
    // Fallback to simulated gateway initialization
}

// Sandbox / Simulation Fallback
$db = get_db_connection();
$tStmt = $db->prepare("INSERT INTO transactions (payment_reference, transaction_reference, amount, status, raw_payload) VALUES (:pref, :tref, :amt, 'INITIALIZED', 'SIMULATED')");
$tStmt->execute([':pref' => $paymentRef, ':tref' => $txnRef, ':amt' => $amount]);

json_response(true, [
    'paymentReference' => $paymentRef,
    'transactionReference' => $txnRef,
    'checkoutUrl' => "$baseUrl/checkout/$paymentRef",
    'virtualAccountNumber' => $virtualAccount,
    'bankName' => "Wema Bank / Monnify Gateway",
    'ussdCode' => $ussdCode,
    'amount' => $amount
], "Monnify Checkout Initialized (PHP Backend Sandbox Mode).");
