import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { Routes, Route, useLocation, useNavigate } from "react-router-dom";
import {
  ActionIcon,
  AppShell,
  Box,
  Burger,
  Container,
  Group,
  Menu,
  NavLink,
  Stack,
  Text,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import type { RootState } from "../store";
import { persistor } from "../store";
import { clearAuth } from "../store/slices/authSlice";
import { PageNotFound } from "./PageNotFound";
import { Operations } from "../views/admin/Operations";

interface AdminRouteConfig {
  subRoute: string;
  title: string;
  element: React.ReactElement;
}

const AdminHome: React.FC = () => (
  <Container size="lg">
    <Stack gap="md">
      <Text c="dimmed">Выберите раздел в меню слева.</Text>
    </Stack>
  </Container>
);

const adminRoutes: AdminRouteConfig[] = [
  {
    subRoute: "operations",
    title: "Операции",
    element: <Operations />,
  },
];

export const AdminApp: React.FC = () => {
  const dispatch = useDispatch();
  const [opened, { toggle, close }] = useDisclosure();
  const navigate = useNavigate();
  const location = useLocation();
  const login = useSelector((state: RootState) => state.auth.login);

  const match = location.pathname.match(/^\/admin\/?(.*)$/);
  const subRoute = match?.[1] ?? "";

  const currentRoute = adminRoutes.find((route) => route.subRoute === subRoute);
  const title = currentRoute?.title ?? "Администрирование";

  const handleSignOut = async () => {
    await persistor.purge();
    dispatch(clearAuth());
    navigate("/login", { replace: true });
  };

  const handleNavigate = (routeSubRoute: string) => {
    navigate(routeSubRoute || ".", { replace: false });
    close();
  };

  return (
    <AppShell
      padding="md"
      header={{ height: 56 }}
      navbar={{
        width: 260,
        breakpoint: "sm",
        collapsed: { mobile: !opened },
      }}
    >
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between">
          <Group gap="sm">
            <Burger
              opened={opened}
              onClick={toggle}
              hiddenFrom="sm"
              size="sm"
              aria-label="Toggle navigation"
            />
            <ActionIcon
              variant="subtle"
              size="sm"
              aria-label="Go to admin home"
              onClick={() => navigate("/admin")}
              title="Администрирование"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M3 9l9-7 9 7" />
                <path d="M9 22V12h6v10" />
              </svg>
            </ActionIcon>
            <Text fw={600}>{title}</Text>
          </Group>
          <Menu shadow="md" width={220}>
            <Menu.Target>
              <ActionIcon variant="subtle" aria-label="Account menu">
                <Text fw={600}>{login?.charAt(0).toUpperCase() ?? "A"}</Text>
              </ActionIcon>
            </Menu.Target>
            <Menu.Dropdown>
              <Menu.Label>Профиль</Menu.Label>
              <Menu.Item disabled>{login ?? "Неизвестный пользователь"}</Menu.Item>
              <Menu.Divider />
              <Menu.Item onClick={handleSignOut}>Выйти</Menu.Item>
            </Menu.Dropdown>
          </Menu>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="md">
        {adminRoutes.length === 0 ? (
          <Text c="dimmed" fz="sm">
            Разделы администрирования еще не настроены.
          </Text>
        ) : (
          <Box>
            {adminRoutes.map((route) => (
              <NavLink
                key={route.subRoute}
                label={route.title}
                active={subRoute === route.subRoute}
                onClick={() => handleNavigate(route.subRoute)}
              />
            ))}
          </Box>
        )}
      </AppShell.Navbar>

      <AppShell.Main>
        <Routes>
          <Route index element={<AdminHome />} />
          {adminRoutes.map((route) => (
            <Route key={route.subRoute} path={route.subRoute} element={route.element} />
          ))}
          <Route path="*" element={<PageNotFound />} />
        </Routes>
      </AppShell.Main>
    </AppShell>
  );
};

