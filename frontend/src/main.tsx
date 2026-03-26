import React from "react";
import ReactDOM from "react-dom/client";
import {MantineProvider} from "@mantine/core";
import "@mantine/core/styles.css";
import "./global.css";
import {Provider} from "react-redux";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {PersistGate} from "redux-persist/integration/react";
import {persistor, store} from "./store";
import {App} from "./pages/App";
import {AdminApp} from "./pages/AdminApp";
import {AdminProtectedRoute} from "./components/AdminProtectedRoute";
import {logger} from "./utils/logger";
import {config} from "./config";
import {PageNotFound} from "./pages/PageNotFound";

const rootElement = document.getElementById("root");

logger.info("Starting world seedapp1...", {env: config.appEnv});

if (rootElement) {
    ReactDOM.createRoot(rootElement).render(
        <React.StrictMode>
            <MantineProvider>
                <Provider store={store}>
                    <PersistGate loading={null} persistor={persistor}>
                        <BrowserRouter>
                            <Routes>
                                <Route
                                    path="admin/*"
                                    element={(
                                        <AdminProtectedRoute>
                                            <AdminApp/>
                                        </AdminProtectedRoute>
                                    )}
                                />
                                <Route path="/" element={<App/>}/>
                                <Route path="*" element={<PageNotFound/>}/>
                            </Routes>
                        </BrowserRouter>
                    </PersistGate>
                </Provider>
            </MantineProvider>
        </React.StrictMode>
    );
}

