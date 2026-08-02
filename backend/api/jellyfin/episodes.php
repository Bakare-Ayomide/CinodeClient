<?php
/**
 * CINJELLY PHP REST API - Jellyfin Episodes Proxy Endpoint
 */

require_once __DIR__ . '/../../config/config.php';

$input = get_json_input();
$seriesId = $input['series_id'] ?? $input['seriesId'] ?? null;

if (empty($seriesId)) {
    json_response(false, null, "Series ID is required.", 400);
}

$query = [
    'IncludeItemTypes' => 'Episode',
    'Recursive' => 'true',
    'Fields' => 'Overview,RunTimeTicks,UserData',
    'Limit' => 100
];

$queryString = http_build_query($query);
$result = call_jellyfin_admin_api("Shows/$seriesId/Episodes?" . $queryString, 'GET');

if ($result['code'] === 200 && is_array($result['response'])) {
    json_response(true, $result['response'], "Episodes retrieved successfully.");
}

json_response(true, ['Items' => [], 'TotalRecordCount' => 0], "Episodes returned.");
