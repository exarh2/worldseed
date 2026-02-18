import { createSlice } from "@reduxjs/toolkit";

export interface UiState {
  initialized: boolean;
}

const initialState: UiState = {
  initialized: false
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    setInitialized(state) {
      state.initialized = true;
    }
  }
});

export const { setInitialized } = uiSlice.actions;
export const uiReducer = uiSlice.reducer;

