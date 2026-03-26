import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, Navigate } from "react-router-dom";
import { Alert, Button, Group, Modal, PasswordInput, Stack, TextInput } from "@mantine/core";
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
  const [signIn, { isLoading: isSignInLoading }] = useSignInMutation();
  const [signUp, { isLoading: isSignUpLoading }] = useSignUpMutation();
  const isSubmitting = isSignInLoading || isSignUpLoading;

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
        <Stack>
          <TextInput
            label="Login"
            placeholder="Enter login"
            value={login}
            onChange={(e) => setLogin(e.currentTarget.value)}
            required
          />
          <PasswordInput
            label="Password"
            placeholder="Enter password"
            value={password}
            onChange={(e) => setPassword(e.currentTarget.value)}
            required
          />
          {isSignUp && (
            <TextInput
              label="Email"
              placeholder="Enter email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.currentTarget.value)}
              required
            />
          )}
          {error && (
            <Alert color="red" variant="light">
              {error}
            </Alert>
          )}
          <Button type="submit" loading={isSubmitting} fullWidth>
            {isSignUp ? "Sign up" : "Sign in"}
          </Button>
        </Stack>
      </form>
      <Group mt="sm" justify="center">
        <Button
          variant="subtle"
          type="button"
          onClick={() => {
            setIsSignUp(!isSignUp);
            setError(null);
          }}
        >
          {isSignUp ? "Already have an account? Sign in" : "Need an account? Sign up"}
        </Button>
      </Group>
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
