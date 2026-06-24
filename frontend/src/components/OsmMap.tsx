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
import type {MapWindowState, OsmViewState} from "../store/slices/uiSlice";
import {setMapVisible, setMapWindowState, setOsmViewState} from "../store/slices/uiSlice";

const MIN_MAP_WIDTH = 320;
const MIN_MAP_HEIGHT = 240;
const MAP_CENTER_PRECISION = 6;
const MAP_ZOOM_PRECISION = 3;

const roundToPrecision = (value: number, precision: number): number => {
    const factor = 10 ** precision;
    return Math.round(value * factor) / factor;
};

const normalizeOsmViewState = (osmViewState: OsmViewState): OsmViewState =>
    ({
        lon: roundToPrecision(osmViewState.lon, MAP_CENTER_PRECISION),
        lat: roundToPrecision(osmViewState.lat, MAP_CENTER_PRECISION),
        zoom: roundToPrecision(osmViewState.zoom, MAP_ZOOM_PRECISION)
    });

//Функция проверки, что диалоговое окно карты влезает в основное окно при его уменьшении (если нужно сдвигает)
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
    console.log(currentZoom)
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

    const osmMapDivRef = useRef<HTMLDivElement | null>(null);
    const osmMapRef = useRef<Map | null>(null);
    const osmViewStateRef = useRef<OsmViewState>(normalizeOsmViewState(osmViewState));

    const onMapWindowStateChange = useCallback((next: RootState["ui"]["mapWindowState"]) => {
        dispatch(setMapWindowState(next));
    }, [dispatch]);

    const onOsmViewStateChange = useCallback((next: RootState["ui"]["osmViewState"]) => {
        dispatch(setOsmViewState(next));
    }, [dispatch]);

    useEffect(() => {
        if (!osmMapDivRef.current || osmMapRef.current) {
            return;
        }

        osmMapRef.current = new Map({
            target: osmMapDivRef.current,
            layers: [
                new TileLayer({
                    visible: true,
                    source: new OSM()
                })
            ],
            view: new View({
                projection: 'EPSG:4326',
                center: [osmViewStateRef.current.lon, osmViewStateRef.current.lat],
                zoom: osmViewStateRef.current.zoom,
                maxZoom: 18
            })
        });

        const view = osmMapRef.current.getView();
        const handleMoveEnd = () => {
            const center = view.getCenter();
            const zoom = view.getZoom();
            if (!center || zoom === undefined) {
                return;
            }
            const nextOsmViewState = normalizeOsmViewState({
                lon: center[0],
                lat: center[1],
                zoom
            });
            if (
                nextOsmViewState.lon !== osmViewStateRef.current.lon ||
                nextOsmViewState.lat !== osmViewStateRef.current.lat ||
                nextOsmViewState.zoom !== osmViewStateRef.current.zoom
            ) {
                osmViewStateRef.current = nextOsmViewState;
                onOsmViewStateChange(nextOsmViewState);
            }

            const nextTerrainOptions = pickTerrainOptionsByZoom(terrainOptions, zoom);
            if (!nextTerrainOptions) {
                return;
            }
            if (currentTerrainOptions?.resolution !== nextTerrainOptions.resolution) {
                dispatch(setCurrentTerrainOption(nextTerrainOptions));
            }
        };

        osmMapRef.current.on("moveend", handleMoveEnd);

        return () => {
            if (!osmMapRef.current) {
                return;
            }

            osmMapRef.current.un("moveend", handleMoveEnd);
            osmMapRef.current.setTarget(undefined);
            osmMapRef.current = null;
        };
    }, [dispatch, currentTerrainOptions]);

    useEffect(() => {
        if (!osmMapRef.current) {
            return;
        }

        const view = osmMapRef.current.getView();
        const normalizedOsmViewState = normalizeOsmViewState(osmViewState);
        const currentCenter = view.getCenter();
        const currentZoom = view.getZoom();

        const centerChanged =
            !currentCenter ||
            currentCenter[0] !== normalizedOsmViewState.lon ||
            currentCenter[1] !== normalizedOsmViewState.lat;
        const zoomChanged = currentZoom === undefined || currentZoom !== normalizedOsmViewState.zoom;

        if (centerChanged) {
            view.setCenter([normalizedOsmViewState.lon, normalizedOsmViewState.lat]);
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
                osmMapRef.current?.updateSize();
            }}
            onDragStop={(_event, data) => {
                onMapWindowStateChange(clampMapWindowStateToViewport({
                    ...mapWindowState,
                    x: data.x,
                    y: data.y
                }));
                osmMapRef.current?.updateSize();
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
                ref={osmMapDivRef}
                style={{
                    width: "100%",
                    height: "calc(100% - 36px)"
                }}
            />
        </Rnd>
    );
};
