import React, {useEffect, useRef} from "react";
import {Rnd} from "react-rnd";
import {CloseButton} from "@mantine/core";
import {useDispatch, useSelector} from "react-redux";
import Map from "ol/Map";
import View from "ol/View";
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";
import "ol/ol.css";
import type {AppDispatch, RootState} from "../store";
import {setCurrentTerrainOption, type AnyTerrainOptions} from "../store/slices/sceneSlice";
import type {MapViewState, MapWindowState} from "../store/slices/uiSlice";

const MIN_MAP_WIDTH = 320;
const MIN_MAP_HEIGHT = 240;
const MAP_CENTER_PRECISION = 6;
const MAP_ZOOM_PRECISION = 3;

const roundToPrecision = (value: number, precision: number): number => {
    const factor = 10 ** precision;
    return Math.round(value * factor) / factor;
};

const normalizeMapView = (mapView: MapViewState): MapViewState => ({
    center: [
        roundToPrecision(mapView.center[0], MAP_CENTER_PRECISION),
        roundToPrecision(mapView.center[1], MAP_CENTER_PRECISION)
    ],
    zoom: roundToPrecision(mapView.zoom, MAP_ZOOM_PRECISION)
});

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
    mapView: MapViewState;
    onMapWindowChange: (next: MapWindowState) => void;
    onMapViewChange: (next: MapViewState) => void;
    onClose: () => void;
}

const pickTerrainOptionsByZoom = (
    terrainOptions: AnyTerrainOptions[],
    currentZoom: number
): AnyTerrainOptions | null => {
    const candidates = terrainOptions.filter((option) => option.zoomTo <= currentZoom);
    if (candidates.length === 0) {
        return null;
    }
    return candidates.reduce((best, option) => (option.zoomTo > best.zoomTo ? option : best));
};

export const OsmMap: React.FC<OsmMapProps> = ({mapWindow, mapView, onMapWindowChange, onMapViewChange, onClose}) => {
    const dispatch = useDispatch<AppDispatch>();
    const terrainOptions = useSelector((state: RootState) => state.scene.terrainOptions);
    const currentTerrainOptions = useSelector((state: RootState) => state.scene.currentTerrainOptions);
    const mapContainerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<Map | null>(null);
    const clampedMapWindow = clampMapWindowToViewport(mapWindow);
    const initialMapViewRef = useRef<MapViewState>(normalizeMapView(mapView));
    const lastSavedMapViewRef = useRef<MapViewState>(initialMapViewRef.current);
    const terrainOptionsRef = useRef(terrainOptions);
    const currentTerrainOptionsRef = useRef(currentTerrainOptions);

    useEffect(() => {
        terrainOptionsRef.current = terrainOptions;
    }, [terrainOptions]);

    useEffect(() => {
        currentTerrainOptionsRef.current = currentTerrainOptions;
    }, [currentTerrainOptions]);

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
                center: initialMapViewRef.current.center,
                zoom: initialMapViewRef.current.zoom,
                maxZoom: 18
            })
        });

        const view = mapRef.current.getView();
        const handleMoveEnd = () => {
            const center = view.getCenter();
            const zoom = view.getZoom();
            if (!center || zoom === undefined) {
                return;
            }

            const nextMapView = normalizeMapView({
                center: [center[0], center[1]],
                zoom
            });
            if (
                nextMapView.center[0] !== lastSavedMapViewRef.current.center[0] ||
                nextMapView.center[1] !== lastSavedMapViewRef.current.center[1] ||
                nextMapView.zoom !== lastSavedMapViewRef.current.zoom
            ) {
                lastSavedMapViewRef.current = nextMapView;
                onMapViewChange(nextMapView);
            }

            const nextTerrainOptions = pickTerrainOptionsByZoom(terrainOptionsRef.current, zoom);
            if (!nextTerrainOptions) {
                return;
            }

            if (currentTerrainOptionsRef.current?.resolution !== nextTerrainOptions.resolution) {
                dispatch(setCurrentTerrainOption(nextTerrainOptions));
            }
        };

        mapRef.current.on("moveend", handleMoveEnd);

        return () => {
            if (!mapRef.current) {
                return;
            }

            mapRef.current.un("moveend", handleMoveEnd);
            mapRef.current.setTarget(undefined);
            mapRef.current = null;
        };
    }, [dispatch, onMapViewChange]);

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
                    height: 28,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "flex-end",
                    padding: "0 4px 0 12px",
                    borderBottom: "1px solid #ececec",
                    cursor: "move",
                    userSelect: "none",
                    fontWeight: 600
                }}
            >
                <CloseButton aria-label="Close map" onClick={onClose}/>
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
