import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { PersistGate } from "redux-persist/integration/react";
import { persistor, store } from "./store";
import { App } from "./app/App";
import { AdminApplication } from "./app/AdminApplication";
import { Login } from "./app/Login";
import { AdminProtectedRoute } from "./components/AdminProtectedRoute";
import { logger } from "./utils/logger";
import { config } from "./config";

const rootElement = document.getElementById("root");

logger.info("Starting world seedapp1...", { env: config.appEnv });

if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <Provider store={store}>
        <PersistGate loading={null} persistor={persistor}>
          <BrowserRouter>
            <Routes>
              <Route
                path="admin/*"
                element={(
                  <AdminProtectedRoute>
                    <AdminApplication />
                  </AdminProtectedRoute>
                )}
              />
              <Route path="login/*" element={<Login />} />
              <Route path="/*" element={<App />} />
            </Routes>
          </BrowserRouter>
        </PersistGate>
      </Provider>
    </React.StrictMode>
  );
}

