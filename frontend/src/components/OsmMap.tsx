import React, {useEffect, useRef} from "react";
import {Rnd} from "react-rnd";
import Map from "ol/Map";
import View from "ol/View";
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";
import "ol/ol.css";

export const OsmMap: React.FC = () => {
    const mapContainerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<Map | null>(null);

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

    return (
        <Rnd
            default={{
                x: 80,
                y: 80,
                width: 640,
                height: 420
            }}
            minWidth={320}
            minHeight={240}
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
            onResizeStop={() => mapRef.current?.updateSize()}
            onDragStop={() => mapRef.current?.updateSize()}
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
