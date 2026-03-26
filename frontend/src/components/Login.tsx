import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, Navigate } from "react-router-dom";
import { Modal } from "@mantine/core";
import { useSignInMutation, useSignUpMutation } from "../store/api/authApi";
import { setAuth } from "../store/slices/authSlice";
import type { RootState } from "../store";
import { getErrorMessage } from "../utils/error";

type LoginProps = {
  opened?: boolean;
  onClose?: () => void;
};

export const Login: React.FC<LoginProps> = ({ opened, onClose }) => {
  const [isSignUp, setIsSignUp] = useState(false);
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);

  const dispatch = useDispatch();
  const navigate = useNavigate();
  const token = useSelector((state: RootState) => state.auth.token);
  const role = useSelector((state: RootState) => state.auth.role);
  const [signIn] = useSignInMutation();
  const [signUp] = useSignUpMutation();

  if (token) {
    return <Navigate to={role === "ADMIN" ? "/admin" : "/"} replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const result = isSignUp
        ? await signUp({ login, password, email }).unwrap()
        : await signIn({ login, password }).unwrap();
      dispatch(setAuth({ token: result.token, login: result.login, role: result.role }));
      onClose?.();
      navigate(result.role === "ADMIN" ? "/admin" : "/", { replace: true });
    } catch (err: unknown) {
      setError(getErrorMessage(err, "Auth failed"));
    }
  };

  const content = (
    <section>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="login">Login</label>
          <input
            id="login"
            type="text"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
            required
          />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {isSignUp && (
          <div>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
        )}
        {error && <p style={{ color: "red" }}>{error}</p>}
        <button type="submit">{isSignUp ? "Sign up" : "Sign in"}</button>
      </form>
      <button type="button" onClick={() => { setIsSignUp(!isSignUp); setError(null); }}>
        {isSignUp ? "Already have an account? Sign in" : "Need an account? Sign up"}
      </button>
    </section>
  );

  if (opened === undefined) {
    return content;
  }

  if (!opened) {
    return null;
  }

  return (
    <Modal
      opened={opened}
      onClose={onClose ?? (() => undefined)}
      centered
      size="lg"
      title={isSignUp ? "Sign up" : "Sign in"}
    >
      {content}
    </Modal>
  );
};
