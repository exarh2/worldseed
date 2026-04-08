import React, {useCallback, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {Box, Burger, Button, Checkbox, Drawer, Text} from "@mantine/core";
import {Login} from "../components/Login";
import {clearAuth} from "../store/slices/authSlice";
import type {AppDispatch, RootState} from "../store";
import {OsmMap} from "../components/OsmMap";
import {setMapView, setMapVisible, setMapWindow} from "../store/slices/uiSlice";
import {useGetSceneConfigQuery} from "../store/api/sceneApi";
import {SceneSelector} from "../components/SceneSelector";
import {PlanetScene} from "../scene/PlanetScene";
import {TestScene} from "../scene/TestScene";
import {TerrainType} from "../store/slices/sceneSlice";

const SceneWrapper: React.FC = () => {
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentSceneTerrainOptions);
    if (currentSceneTerrainOption?.generationType == TerrainType.TERRAIN_PLANET)
        return <PlanetScene/>;
    return <TestScene/>;
};


export const App: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const {isLoading, isError} = useGetSceneConfigQuery();
    const token = useSelector((state: RootState) => state.auth.token);
    const login = useSelector((state: RootState) => state.auth.login);
    const isMapVisible = useSelector((state: RootState) => state.ui.isMapVisible);
    const mapWindow = useSelector((state: RootState) => state.ui.mapWindow);
    const mapView = useSelector((state: RootState) => state.ui.mapView);
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);
    const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
    const handleMapWindowChange = useCallback((next: RootState["ui"]["mapWindow"]) => {
        dispatch(setMapWindow(next));
    }, [dispatch]);
    const handleMapViewChange = useCallback((next: RootState["ui"]["mapView"]) => {
        dispatch(setMapView(next));
    }, [dispatch]);

    if (isLoading) {
        return null;
    }

    if (isError) {
        return <Text>Service temporary unavailable</Text>;
    }

    return (
        <Box
            style={{
                width: "100%",
                height: "100%",
                display: "flex",
                flexDirection: "column",
                position: "relative",
                backgroundColor: "#eeeeee"
            }}
        >
            <Burger
                opened={isDrawerOpen}
                onClick={() => setIsDrawerOpen((prev) => !prev)}
                aria-label="Toggle drawer"
                style={{
                    zIndex: 100,
                    position: "absolute",
                    top: 10,
                    left: 10
                }}
            />
            <Drawer
                opened={isDrawerOpen}
                onClose={() => setIsDrawerOpen(false)}
                title="Settings"
                padding="md"
                position="left"
                size={240}
            >
                {token && (
                    <Text size="sm" mb="md">
                        {login}
                    </Text>
                )}
                <Button
                    variant="subtle"
                    fullWidth
                    onClick={() => {
                        if (token) {
                            dispatch(clearAuth());
                        } else {
                            setIsLoginModalOpen(true);
                        }
                        setIsDrawerOpen(false);
                    }}
                >
                    {token ? "Logout" : "Login"}
                </Button>
                <Checkbox
                    mt="md"
                    label="Show map"
                    checked={isMapVisible}
                    onChange={(event) => dispatch(setMapVisible(event.currentTarget.checked))}
                />
                <Box mt="md">
                    <SceneSelector/>
                </Box>
            </Drawer>
            <Login opened={isLoginModalOpen} onClose={() => setIsLoginModalOpen(false)}/>
            <SceneWrapper/>
            {isMapVisible && (
                <OsmMap
                    mapWindow={mapWindow}
                    mapView={mapView}
                    onMapWindowChange={handleMapWindowChange}
                    onMapViewChange={handleMapViewChange}
                    onClose={() => dispatch(setMapVisible(false))}
                />
            )}
        </Box>
    );
};

