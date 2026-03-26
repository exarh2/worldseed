import { createSlice } from "@reduxjs/toolkit";

export interface MapWindowState {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface UiState {
  initialized: boolean;
  isMapVisible: boolean;
  mapWindow: MapWindowState;
}

const getDefaultMapWindow = (): MapWindowState => {
  if (typeof window === "undefined") {
    return {
      x: 0,
      y: 0,
      width: 320,
      height: 240
    };
  }

  return {
    x: Math.round(window.innerWidth * 3 / 4 - 10),
    y: Math.round(window.innerHeight * 3 / 4 - 10),
    width: Math.round(window.innerWidth / 4),
    height: Math.round(window.innerHeight / 4)
  };
};

const initialState: UiState = {
  initialized: false,
  isMapVisible: false,
  mapWindow: getDefaultMapWindow()
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    setInitialized(state) {
      state.initialized = true;
    },
    setMapVisible(state, action: { payload: boolean }) {
      state.isMapVisible = action.payload;
    },
    setMapWindow(state, action: { payload: MapWindowState }) {
      state.mapWindow = action.payload;
    }
  }
});

export const { setInitialized, setMapVisible, setMapWindow } = uiSlice.actions;
export const uiReducer = uiSlice.reducer;

