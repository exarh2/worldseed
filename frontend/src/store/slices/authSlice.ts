import { createSlice } from "@reduxjs/toolkit";

const TOKEN_KEY = "worldseed_jwt";

function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export interface AuthState {
  token: string | null;
  login: string | null;
  role: string | null;
}

const initialState: AuthState = {
  token: getStoredToken(),
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
      setStoredToken(action.payload.token);
    },
    clearAuth(state) {
      state.token = null;
      state.login = null;
      state.role = null;
      setStoredToken(null);
    }
  }
});

export const { setAuth, clearAuth } = authSlice.actions;
export const authReducer = authSlice.reducer;
