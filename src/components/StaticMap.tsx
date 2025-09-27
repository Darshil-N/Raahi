import React from "react";

// Simple static map using OSM tiles, no external JS libs
// - Renders a map image centered at given lat/lng
// - Supports multiple pins via overlayed absolutely-positioned markers
// Note: This is a best-effort static approach: marker positions are approximate.

type LatLng = { lat: number; lng: number; label?: string };

interface StaticMapProps {
  center: { lat: number; lng: number };
  zoom?: number; // 0-19
  width?: number; // px
  height?: number; // px
  markers?: LatLng[];
}

const TILE_SIZE = 256;

function lon2tile(lon: number, zoom: number) {
  return ((lon + 180) / 360) * Math.pow(2, zoom);
}

function lat2tile(lat: number, zoom: number) {
  const rad = (lat * Math.PI) / 180;
  return (
    (1 - Math.log(Math.tan(rad) + 1 / Math.cos(rad)) / Math.PI) / 2 * Math.pow(2, zoom)
  );
}

// Convert lat/lng to pixel position in the map image
function projectToImage(lat: number, lng: number, center: { lat: number; lng: number }, zoom: number, width: number, height: number) {
  const scale = Math.pow(2, zoom) * TILE_SIZE;

  const worldX = (lng + 180) / 360 * scale;
  const sinLat = Math.sin((lat * Math.PI) / 180);
  const worldY = (0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI)) * scale;

  const centerX = (center.lng + 180) / 360 * scale;
  const sinCenterLat = Math.sin((center.lat * Math.PI) / 180);
  const centerY = (0.5 - Math.log((1 + sinCenterLat) / (1 - sinCenterLat)) / (4 * Math.PI)) * scale;

  const x = (worldX - centerX) + width / 2;
  const y = (worldY - centerY) + height / 2;
  return { x, y };
}

export default function StaticMap({ center, zoom = 8, width = 800, height = 400, markers = [] }: StaticMapProps) {
  const tileX = lon2tile(center.lng, zoom);
  const tileY = lat2tile(center.lat, zoom);

  // Build a single tile as background (approximate center)
  const tileUrl = `https://tile.openstreetmap.org/${zoom}/${Math.floor(tileX)}/${Math.floor(tileY)}.png`;

  return (
    <div style={{ width, height, position: 'relative', overflow: 'hidden', borderRadius: 8 }}>
      <img
        src={tileUrl}
        alt="Map"
        style={{ width: '100%', height: '100%', objectFit: 'cover', filter: 'saturate(0.95)' }}
      />
      {markers.map((m, idx) => {
        const { x, y } = projectToImage(m.lat, m.lng, center, zoom, width, height);
        if (x < -20 || y < -20 || x > width + 20 || y > height + 20) return null; // skip far-off markers
        return (
          <div key={idx} style={{ position: 'absolute', left: x, top: y, transform: 'translate(-50%, -100%)' }}>
            <div style={{ width: 0, height: 0, borderLeft: '6px solid transparent', borderRight: '6px solid transparent', borderTop: '10px solid #ef4444' }} />
            {m.label && (
              <div style={{ position: 'absolute', top: -24, left: '50%', transform: 'translateX(-50%)', background: '#ef4444', color: 'white', fontSize: 10, padding: '2px 6px', borderRadius: 4 }}>
                {m.label}
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}