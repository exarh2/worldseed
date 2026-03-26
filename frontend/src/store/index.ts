import { combineReducers, configureStore } from "@reduxjs/toolkit";
import {
  FLUSH,
  PAUSE,
  PERSIST,
  PURGE,
  REGISTER,
  REHYDRATE,
  persistReducer,
  persistStore
} from "redux-persist";
import storage from "redux-persist/lib/storage";
// @ts-ignore
import { createLogger as createReduxLogger } from "redux-logger";
import { uiReducer } from "./slices/uiSlice";
import { authReducer } from "./slices/authSlice";
import { baseApi } from "./api/baseApi";
import { config } from "../config";

const authPersistConfig = {
  key: "auth",
  storage,
  whitelist: ["token", "login", "role"]
};

const uiPersistConfig = {
  key: "ui",
  storage,
  whitelist: ["isMapVisible", "mapWindow", "mapView"]
};

const rootReducer = combineReducers({
  ui: persistReducer(uiPersistConfig, uiReducer),
  auth: persistReducer(authPersistConfig, authReducer),
  [baseApi.reducerPath]: baseApi.reducer
});

const reduxLoggerMiddleware = createReduxLogger({
    //Не показываем назойливые действия
    //predicate: (getState, action) => !action.type.startsWith('@@redux-form'),
    //Свертываем все действия
    collapsed: true
});

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) => {
    const middleware = getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER]
      }
    }).concat(baseApi.middleware);

    if (config.reduxLoggerEnabled) {
      middleware.push(reduxLoggerMiddleware);
    }

    return middleware;
  }
});

export const persistor = persistStore(store);

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
