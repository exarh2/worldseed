import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useSignInMutation, useSignUpMutation } from "../../store/api/baseApi";
import { setAuth } from "../../store/slices/authSlice";

export const AuthScene: React.FC = () => {
  const [isSignUp, setIsSignUp] = useState(false);
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);

  const dispatch = useDispatch();
  const [signIn] = useSignInMutation();
  const [signUp] = useSignUpMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const result = isSignUp
        ? await signUp({ login, password, email }).unwrap()
        : await signIn({ login, password }).unwrap();
      dispatch(setAuth({ token: result.token, login: result.login, role: result.role }));
    } catch (err: unknown) {
      let msg = "Auth failed";
      if (err && typeof err === "object" && "data" in err) {
        const data = (err as { data: unknown }).data;
        if (data && typeof data === "object" && "message" in data && typeof (data as { message: unknown }).message === "string") {
          msg = (data as { message: string }).message;
        } else if (typeof data === "string") {
          msg = data;
        }
      } else if (err instanceof Error) {
        msg = err.message;
      }
      setError(msg);
    }
  };

  return (
    <section>
      <h1>{isSignUp ? "Sign up" : "Sign in"}</h1>
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
};
