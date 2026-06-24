import { createSlice } from "@reduxjs/toolkit";

export interface MapWindowState {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface OsmViewState {
  center: [number, number];
  zoom: number;
}

export interface UiState {
  isMapVisible: boolean;
  mapWindow: MapWindowState;
  osmViewState: OsmViewState;
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
  osmViewState: {
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
    setMapWindowState(state, action: { payload: MapWindowState }) {
      state.mapWindow = action.payload;
    },
    setOsmViewState(state, action: { payload: OsmViewState }) {
      state.osmViewState = action.payload;
    }
  }
});

export const { setMapVisible, setMapWindowState, setOsmViewState } = uiSlice.actions;
export const uiReducer = uiSlice.reducer;

