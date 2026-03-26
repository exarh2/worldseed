import React, { useState } from "react";
import { Routes, Route, Link, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { HomeScene } from "../views/home/HomeScene";
import { WorldScene } from "../views/world/WorldScene";
import { Login } from "./Login";
import { PageNotFound } from "./PageNotFound";
import { clearAuth } from "../store/slices/authSlice";
import type { RootState } from "../store";

export const App: React.FC = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const token = useSelector((state: RootState) => state.auth.token);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

  const handleLogout = () => {
    dispatch(clearAuth());
    navigate("/");
  };

  return (
    <div>
      <header>
        <nav>
          <Link to="/">Home</Link> | <Link to="/world">World</Link> | 
          {token ? (
            <button type="button" onClick={handleLogout}>Logout</button>
          ) : (
            <button type="button" onClick={() => setIsLoginModalOpen(true)}>Login</button>
          )}
        </nav>
      </header>
      <main>
        <Login opened={isLoginModalOpen} onClose={() => setIsLoginModalOpen(false)} />
        <Routes>
          <Route path="/" element={<HomeScene />} />
          <Route path="/world" element={<WorldScene />} />
          <Route path="/login" element={<Login />} />
          <Route path="*" element={<PageNotFound />} />
        </Routes>
      </main>
    </div>
  );
};

