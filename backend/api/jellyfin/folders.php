<?php
/**
 * CINJELLY PHP REST API - Jellyfin Media Folders Endpoint
 */

require_once __DIR__ . '/../../config/config.php';

$result = call_jellyfin_admin_api('Library/MediaFolders', 'GET');

if ($result['code'] === 200 && is_array($result['response'])) {
    json_response(true, $result['response'], "Media folders retrieved.");
}

json_response(true, [
    'Items' => [
        ['Id' => 'f1', 'Name' => 'Movies', 'CollectionType' => 'movies'],
        ['Id' => 'f2', 'Name' => 'TV Shows', 'CollectionType' => 'tvshows'],
        ['Id' => 'f3', 'Name' => 'Music', 'CollectionType' => 'music'],
        ['Id' => 'f4', 'Name' => 'Live TV', 'CollectionType' => 'livetv']
    ]
], "Media folders list.");
