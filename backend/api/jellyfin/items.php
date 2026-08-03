<?php
/**
 * CINJELLY PHP REST API - Jellyfin Items Proxy Endpoint
 * Proxies media items requests to Jellyfin server using server-side API Key.
 */

require_once __DIR__ . '/../../config/config.php';

$input = array_merge($_REQUEST, get_json_input());

$includeItemTypes = $input['IncludeItemTypes'] ?? $input['includeItemTypes'] ?? null;
$searchTerm = $input['SearchTerm'] ?? $input['searchTerm'] ?? null;
$userId = $input['UserId'] ?? $input['userId'] ?? null;
$limit = (int)($input['Limit'] ?? $input['limit'] ?? 100);

$query = [
    'Recursive' => 'true',
    'Fields' => 'Overview,Genres,CommunityRating,ProductionYear,RunTimeTicks,SeriesName,UserData',
    'Limit' => $limit
];

if (!empty($includeItemTypes)) $query['IncludeItemTypes'] = $includeItemTypes;
if (!empty($searchTerm)) $query['SearchTerm'] = $searchTerm;
if (!empty($userId)) $query['UserId'] = $userId;

$queryString = http_build_query($query);
$result = call_jellyfin_admin_api('Items?' . $queryString, 'GET');

if ($result['code'] === 200 && is_array($result['response'])) {
    json_response(true, $result['response'], "Items retrieved successfully.");
}

// Fallback empty response
json_response(true, [
    'Items' => [],
    'TotalRecordCount' => 0
], "Jellyfin items returned.");
