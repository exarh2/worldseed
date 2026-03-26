import { createSlice } from "@reduxjs/toolkit";

export interface MapWindowState {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface MapViewState {
  center: [number, number];
  zoom: number;
}

export interface UiState {
  isMapVisible: boolean;
  mapWindow: MapWindowState;
  mapView: MapViewState;
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
  isMapVisible: false,
  mapWindow: getDefaultMapWindow(),
  mapView: {
    center: [112, 40],
    zoom: 10
  }
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    setMapVisible(state, action: { payload: boolean }) {
      state.isMapVisible = action.payload;
    },
    setMapWindow(state, action: { payload: MapWindowState }) {
      state.mapWindow = action.payload;
    },
    setMapView(state, action: { payload: MapViewState }) {
      state.mapView = action.payload;
    }
  }
});

export const { setMapVisible, setMapWindow, setMapView } = uiSlice.actions;
export const uiReducer = uiSlice.reducer;

