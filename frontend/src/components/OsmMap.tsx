import React, {useCallback, useEffect, useRef} from "react";
import {Rnd} from "react-rnd";
import {CloseButton} from "@mantine/core";
import {useDispatch, useSelector} from "react-redux";
import Map from "ol/Map";
import View from "ol/View";
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";
import "ol/ol.css";
import type {AppDispatch, RootState} from "../store";
import {type AnyTerrainOptions, setCurrentTerrainOption} from "../store/slices/sceneSlice";
import type {OsmViewState, MapWindowState} from "../store/slices/uiSlice";
import {setOsmViewState, setMapVisible, setMapWindowState} from "../store/slices/uiSlice";

const MIN_MAP_WIDTH = 320;
const MIN_MAP_HEIGHT = 240;
const MAP_CENTER_PRECISION = 6;
const MAP_ZOOM_PRECISION = 3;

const roundToPrecision = (value: number, precision: number): number => {
    const factor = 10 ** precision;
    return Math.round(value * factor) / factor;
};

const normalizeOsmViewState = (osmViewState: OsmViewState): OsmViewState => ({
    center: [
        roundToPrecision(osmViewState.center[0], MAP_CENTER_PRECISION),
        roundToPrecision(osmViewState.center[1], MAP_CENTER_PRECISION)
    ],
    zoom: roundToPrecision(osmViewState.zoom, MAP_ZOOM_PRECISION)
});

//Функция проверки, что диалоговое окно карты влезает в основное окно при его уменьшении (если нужно сдигает)
const clampMapWindowStateToViewport = (mapWindowState: MapWindowState): MapWindowState => {
    if (typeof window === "undefined") {
        return mapWindowState;
    }

    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    const width = Math.min(Math.max(mapWindowState.width, MIN_MAP_WIDTH), viewportWidth);
    const height = Math.min(Math.max(mapWindowState.height, MIN_MAP_HEIGHT), viewportHeight);

    const maxX = Math.max(0, viewportWidth - width);
    const maxY = Math.max(0, viewportHeight - height);

    return {
        x: Math.min(Math.max(mapWindowState.x, 0), maxX),
        y: Math.min(Math.max(mapWindowState.y, 0), maxY),
        width,
        height
    };
};

const pickTerrainOptionsByZoom = (
    terrainOptions: AnyTerrainOptions[],
    currentZoom: number
): AnyTerrainOptions | null => {
    if (terrainOptions.length === 0) {
        return null;
    }
    return terrainOptions.filter((option) => option.zoomFrom >= currentZoom)
        .reduce((best, option) => (option.zoomFrom < best.zoomFrom ? option : best));
};

export const OsmMap: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    //Данные из redux
    const terrainOptions = useSelector((state: RootState) => state.scene.terrainOptions);
    const currentTerrainOptions = useSelector((state: RootState) => state.scene.currentTerrainOptions);
    const mapWindowState = useSelector((state: RootState) => state.ui.mapWindowState);
    const osmViewState = useSelector((state: RootState) => state.ui.osmViewState);

    const mapContainerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<Map | null>(null);

    const osmViewStateRef = useRef<OsmViewState>(normalizeOsmViewState(osmViewState));
    const terrainOptionsRef = useRef(terrainOptions);
    const currentTerrainOptionsRef = useRef(currentTerrainOptions);

    const onMapWindowStateChange = useCallback((next: RootState["ui"]["mapWindowState"]) => {
        dispatch(setMapWindowState(next));
    }, [dispatch]);

    const onOsmViewStateChange = useCallback((next: RootState["ui"]["osmViewState"]) => {
        dispatch(setOsmViewState(next));
    }, [dispatch]);


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
                center: osmViewStateRef.current.center,
                zoom: osmViewStateRef.current.zoom,
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
            const nextOsmViewState = normalizeOsmViewState({
                center: [center[0], center[1]],
                zoom
            });
            if (
                nextOsmViewState.center[0] !== osmViewStateRef.current.center[0] ||
                nextOsmViewState.center[1] !== osmViewStateRef.current.center[1] ||
                nextOsmViewState.zoom !== osmViewStateRef.current.zoom
            ) {
                osmViewStateRef.current = nextOsmViewState;
                onOsmViewStateChange(nextOsmViewState);
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
    }, [dispatch, onOsmViewStateChange]);

    useEffect(() => {
        if (!mapRef.current) {
            return;
        }

        const view = mapRef.current.getView();
        const normalizedOsmViewState = normalizeOsmViewState(osmViewState);
        const currentCenter = view.getCenter();
        const currentZoom = view.getZoom();

        const centerChanged =
            !currentCenter ||
            currentCenter[0] !== normalizedOsmViewState.center[0] ||
            currentCenter[1] !== normalizedOsmViewState.center[1];
        const zoomChanged = currentZoom === undefined || currentZoom !== normalizedOsmViewState.zoom;

        if (centerChanged) {
            view.setCenter(normalizedOsmViewState.center);
        }
        if (zoomChanged) {
            view.setZoom(normalizedOsmViewState.zoom);
        }

        // Keep local snapshot in sync to avoid redundant write-back.
        osmViewStateRef.current = normalizedOsmViewState;
    }, [osmViewState]);

    //Обраотка ресайза окна браузера
    useEffect(() => {
        const normalizeMapWindow = () => {
            const normalized = clampMapWindowStateToViewport(mapWindowState);
            if (
                normalized.x !== mapWindowState.x ||
                normalized.y !== mapWindowState.y ||
                normalized.width !== mapWindowState.width ||
                normalized.height !== mapWindowState.height
            ) {
                onMapWindowStateChange(normalized);
            }
        };

        normalizeMapWindow();
        window.addEventListener("resize", normalizeMapWindow);
        return () => window.removeEventListener("resize", normalizeMapWindow);
    }, [mapWindowState]);

    return (
        <Rnd
            size={{width: mapWindowState.width, height: mapWindowState.height}}
            position={{x: mapWindowState.x, y: mapWindowState.y}}
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
                onMapWindowStateChange(clampMapWindowStateToViewport({
                    x: position.x,
                    y: position.y,
                    width: ref.offsetWidth,
                    height: ref.offsetHeight
                }));
                mapRef.current?.updateSize();
            }}
            onDragStop={(_event, data) => {
                onMapWindowStateChange(clampMapWindowStateToViewport({
                    ...mapWindowState,
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
                <CloseButton aria-label="Close map" onClick={() => dispatch(setMapVisible(false))}/>
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
