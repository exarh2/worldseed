import { createSlice } from "@reduxjs/toolkit";

export interface AuthState {
  token: string | null;
  login: string | null;
  role: string | null;
}

const initialState: AuthState = {
  token: null,
  login: null,
  role: null
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setAuth(
      state,
      action: { payload: { token: string; login: string; role: string } }
    ) {
      state.token = action.payload.token;
      state.login = action.payload.login;
      state.role = action.payload.role;
    },
    clearAuth(state) {
      state.token = null;
      state.login = null;
      state.role = null;
    }
  }
});

export const { setAuth, clearAuth } = authSlice.actions;
export const authReducer = authSlice.reducer;
