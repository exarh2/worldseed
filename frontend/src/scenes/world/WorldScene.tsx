import React from "react";
import { useGetHelloQuery } from "../../store/api/baseApi";

export const WorldScene: React.FC = () => {
  const { data, isLoading, isError } = useGetHelloQuery();

  return (
    <section>
      <h1>World Scene</h1>
      {isLoading && <p>Loading greeting from backend…</p>}
      {isError && <p>Failed to load greeting.</p>}
      {data && <p>Backend says: {data.message}</p>}
    </section>
  );
};

