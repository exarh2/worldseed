import React from "react";
import { useSelector } from "react-redux";
import { Navigate } from "react-router-dom";
import type { RootState } from "../store";

interface AdminProtectedRouteProps {
  children: React.ReactElement;
}

export const AdminProtectedRoute: React.FC<AdminProtectedRouteProps> = ({ children }) => {
  const token = useSelector((state: RootState) => state.auth.token);
  const role = useSelector((state: RootState) => state.auth.role);

  if (!token) {
    return <Navigate to="/login" replace />;
  }
  if (role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  return children;
};

