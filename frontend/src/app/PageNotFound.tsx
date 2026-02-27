import React from "react";
import { Link } from "react-router-dom";

export const PageNotFound: React.FC = () => {
  return (
    <section>
      <h1>Page not found</h1>
      <p>The page you are looking for does not exist.</p>
      <Link to="/">Go to home</Link>
    </section>
  );
};
