import React, {useEffect, useRef} from "react";
import {Rnd} from "react-rnd";
import Map from "ol/Map";
import View from "ol/View";
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";
import "ol/ol.css";
import type {MapWindowState} from "../store/slices/uiSlice";

const MIN_MAP_WIDTH = 320;
const MIN_MAP_HEIGHT = 240;

const clampMapWindowToViewport = (windowState: MapWindowState): MapWindowState => {
    if (typeof window === "undefined") {
        return windowState;
    }

    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    const width = Math.min(Math.max(windowState.width, MIN_MAP_WIDTH), viewportWidth);
    const height = Math.min(Math.max(windowState.height, MIN_MAP_HEIGHT), viewportHeight);

    const maxX = Math.max(0, viewportWidth - width);
    const maxY = Math.max(0, viewportHeight - height);

    return {
        x: Math.min(Math.max(windowState.x, 0), maxX),
        y: Math.min(Math.max(windowState.y, 0), maxY),
        width,
        height
    };
};

interface OsmMapProps {
    mapWindow: MapWindowState;
    onMapWindowChange: (next: MapWindowState) => void;
}

export const OsmMap: React.FC<OsmMapProps> = ({mapWindow, onMapWindowChange}) => {
    const mapContainerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<Map | null>(null);
    const clampedMapWindow = clampMapWindowToViewport(mapWindow);

    useEffect(() => {
        if (!mapContainerRef.current || mapRef.current) {
            return;
        }

        mapRef.current = new Map({
            target: mapContainerRef.current,
            layers: [
                new TileLayer({
                    visible: true,
                    source: new OSM()
                })
            ],
            view: new View({
                projection: 'EPSG:4326',
                center: [112, 40],
                zoom: 10,
                maxZoom: 18
            })
        });

        return () => {
            if (!mapRef.current) {
                return;
            }

            mapRef.current.setTarget(undefined);
            mapRef.current = null;
        };
    }, []);

    useEffect(() => {
        const normalizeMapWindow = () => {
            const normalized = clampMapWindowToViewport(mapWindow);
            if (
                normalized.x !== mapWindow.x ||
                normalized.y !== mapWindow.y ||
                normalized.width !== mapWindow.width ||
                normalized.height !== mapWindow.height
            ) {
                onMapWindowChange(normalized);
            }
        };

        normalizeMapWindow();
        window.addEventListener("resize", normalizeMapWindow);
        return () => window.removeEventListener("resize", normalizeMapWindow);
    }, [mapWindow, onMapWindowChange]);

    return (
        <Rnd
            size={{width: clampedMapWindow.width, height: clampedMapWindow.height}}
            position={{x: clampedMapWindow.x, y: clampedMapWindow.y}}
            minWidth={MIN_MAP_WIDTH}
            minHeight={MIN_MAP_HEIGHT}
            bounds="window"
            dragHandleClassName="osm-map-header"
            style={{
                zIndex: 1000,
                border: "1px solid #d9d9d9",
                borderRadius: 8,
                overflow: "hidden",
                background: "#ffffff",
                boxShadow: "0 6px 20px rgba(0, 0, 0, 0.15)"
            }}
            onResizeStop={(_event, _direction, ref, _delta, position) => {
                onMapWindowChange(clampMapWindowToViewport({
                    x: position.x,
                    y: position.y,
                    width: ref.offsetWidth,
                    height: ref.offsetHeight
                }));
                mapRef.current?.updateSize();
            }}
            onDragStop={(_event, data) => {
                onMapWindowChange(clampMapWindowToViewport({
                    ...mapWindow,
                    x: data.x,
                    y: data.y
                }));
                mapRef.current?.updateSize();
            }}
        >
            <div
                className="osm-map-header"
                style={{
                    height: 36,
                    display: "flex",
                    alignItems: "center",
                    padding: "0 12px",
                    borderBottom: "1px solid #ececec",
                    cursor: "move",
                    userSelect: "none",
                    fontWeight: 600
                }}
            >
                OSM Map
            </div>
            <div
                ref={mapContainerRef}
                style={{
                    width: "100%",
                    height: "calc(100% - 36px)"
                }}
            />
        </Rnd>
    );
};
