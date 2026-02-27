import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import { PersistGate } from "redux-persist/integration/react";
import { persistor, store } from "./store";
import { App } from "./app/App";
import { logger } from "./logger";
import { config } from "./config";

const rootElement = document.getElementById("root");

logger.info("Starting world seedapp1...", { env: config.appEnv });

if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <Provider store={store}>
        <PersistGate loading={null} persistor={persistor}>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </PersistGate>
      </Provider>
    </React.StrictMode>
  );
}

