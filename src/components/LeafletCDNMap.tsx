import React, { useEffect, useRef } from 'react';

declare global {
  interface Window {
    L: any; // Leaflet from CDN
  }
}

interface MarkerItem {
  lat: number;
  lng: number;
  label?: string;
}

interface LeafletCDNMapProps {
  center: { lat: number; lng: number };
  zoom?: number;
  markers?: MarkerItem[];
  height?: number | string; // e.g., 400 or '400px'
  fitToMarkers?: boolean;
  sirenOnClick?: boolean; // play siren when a marker is clicked
  playOnNewMarkers?: boolean; // attempt to play siren when new markers arrive
}

const LeafletCDNMap: React.FC<LeafletCDNMapProps> = ({ center, zoom = 7, markers = [], height = 480, fitToMarkers = true, sirenOnClick = true, playOnNewMarkers = true }) => {
  const mapRef = useRef<HTMLDivElement | null>(null);
  const leafletMapRef = useRef<any>(null);
  const sirenRef = useRef<HTMLAudioElement | null>(null);
  const prevMarkerCountRef = useRef<number>(0);
  const distressIconRef = useRef<any>(null);

  useEffect(() => {
    // Ensure Leaflet from CDN is available
    if (!window.L) return;

    // Initialize map only once
    if (!leafletMapRef.current && mapRef.current) {
      leafletMapRef.current = window.L.map(mapRef.current).setView([center.lat, center.lng], zoom);

      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(leafletMapRef.current);

      // Preload explicit distress icon (bold red pin) as SVG
      const pinHtml = `
        <svg width="32" height="48" viewBox="0 0 24 36" xmlns="http://www.w3.org/2000/svg" style="display:block">
          <path d="M12 0C5.9 0 1 4.9 1 11c0 7.5 9.3 24.2 10.1 25.6.2.3.5.4.9.4s.7-.2.9-.5C13.7 35.1 23 18.5 23 11 23 4.9 18.1 0 12 0z" fill="#FF1F1F"/>
          <circle cx="12" cy="11" r="4" fill="#ffffff"/>
        </svg>
      `;
      distressIconRef.current = window.L.divIcon({
        html: pinHtml,
        className: 'custom-pin-icon',
        iconSize: [32, 48],
        iconAnchor: [16, 48],
        popupAnchor: [0, -44],
      });
    }

    // Update view when center/zoom changes
    if (leafletMapRef.current) {
      leafletMapRef.current.setView([center.lat, center.lng], zoom);
    }

    return () => {
      // Optional: do not destroy to keep the map on route re-renders
      // If unmounting permanently, you can call remove()
      // leafletMapRef.current?.remove();
    };
  }, [center.lat, center.lng, zoom]);

  useEffect(() => {
    if (!leafletMapRef.current || !window.L) return;

    // Remove old markers layer if exists
    // For simplicity, clear all layers except the tile layer
    leafletMapRef.current.eachLayer((layer: any) => {
      if (!(layer instanceof window.L.TileLayer)) {
        leafletMapRef.current.removeLayer(layer);
      }
    });

    const validMarkers = markers.filter(m => Number.isFinite(m.lat) && Number.isFinite(m.lng));

    // Add markers with explicit distress icon and click sound
    validMarkers.forEach((m) => {
      const marker = window.L.marker([m.lat, m.lng], {
        icon: distressIconRef.current || undefined,
      }).addTo(leafletMapRef.current);
      if (m.label) {
        marker.bindPopup(m.label);
      }
      if (sirenOnClick) {
        marker.on('click', () => {
          try { sirenRef.current?.play(); } catch {}
        });
      }
    });

    // Fit bounds to markers
    if (fitToMarkers && validMarkers.length > 0) {
      const bounds = window.L.latLngBounds(validMarkers.map(m => [m.lat, m.lng]));
      leafletMapRef.current.fitBounds(bounds.pad(0.2));
    }

    // Auto-play siren if new markers arrived (best-effort; may be blocked until user interacts)
    if (playOnNewMarkers && validMarkers.length > prevMarkerCountRef.current) {
      try { sirenRef.current?.play(); } catch {}
    }
    prevMarkerCountRef.current = validMarkers.length;
  }, [markers, fitToMarkers, sirenOnClick, playOnNewMarkers]);

  return (
    <>
      <div ref={mapRef} style={{ width: '100%', height: typeof height === 'number' ? `${height}px` : height, borderRadius: 8 }} />
      {/* Hidden audio element for siren */}
      <audio ref={sirenRef} preload="auto">
        <source src="https://actions.google.com/sounds/v1/alarms/alarm_clock.ogg" type="audio/ogg" />
        <source src="https://actions.google.com/sounds/v1/alarms/alarm_clock.ogg" type="audio/mpeg" />
      </audio>
    </>
  );
};

export default LeafletCDNMap;