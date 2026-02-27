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
import { uiReducer } from "./slices/uiSlice";
import { authReducer } from "./slices/authSlice";
import { baseApi } from "./api/baseApi";

const rootReducer = combineReducers({
  ui: uiReducer,
  auth: authReducer,
  [baseApi.reducerPath]: baseApi.reducer
});

const persistConfig = {
  key: "root",
  storage,
  whitelist: ["auth"]
};

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER]
      }
    }).concat(baseApi.middleware)
});

export const persistor = persistStore(store);

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

