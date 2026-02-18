import React from "react";
import { Routes, Route, Link } from "react-router-dom";
import { HomeScene } from "../scenes/home/HomeScene";
import { WorldScene } from "../scenes/world/WorldScene";

export const App: React.FC = () => (
  <div>
    <header>
      <nav>
        <Link to="/">Home</Link> | <Link to="/world">World</Link>
      </nav>
    </header>
    <main>
      <Routes>
        <Route path="/" element={<HomeScene />} />
        <Route path="/world" element={<WorldScene />} />
      </Routes>
    </main>
  </div>
);

