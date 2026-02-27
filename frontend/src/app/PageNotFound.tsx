import React from "react";
import { Link } from "react-router-dom";
import { Container, Title, Text, Button, Group, Stack, Center } from "@mantine/core";

export const PageNotFound: React.FC = () => {
  return (
    <Container size="sm">
      <Center style={{ minHeight: "60vh" }}>
        <Stack gap="md" align="center">
          <Title order={1}>Page not found</Title>
          <Text c="dimmed" ta="center">
            The page you are looking for does not exist or has been moved.
          </Text>
          <Group justify="center">
            <Button component={Link} to="/" variant="filled">
              Go to home
            </Button>
          </Group>
        </Stack>
      </Center>
    </Container>
  );
};
