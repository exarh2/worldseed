import React, {useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {Box, Burger, Button, Drawer, Text} from "@mantine/core";
import {Login} from "../components/Login";
import {clearAuth} from "../store/slices/authSlice";
import type {RootState} from "../store";

export const App: React.FC = () => {
    const dispatch = useDispatch();
    const token = useSelector((state: RootState) => state.auth.token);
    const login = useSelector((state: RootState) => state.auth.login);
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);
    const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

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
            </Drawer>
            <Login opened={isLoginModalOpen} onClose={() => setIsLoginModalOpen(false)}/>
        </Box>
    );
};

