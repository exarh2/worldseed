import React, { useState } from "react";
import { Alert, Button, Container, Modal, Stack, Text } from "@mantine/core";
import { useDropAllTerrainsMutation } from "../../store/api/adminApi";

export const Operations: React.FC = () => {
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [hasAskedConfirmation, setHasAskedConfirmation] = useState(false);

  const [lastInProgress, setLastInProgress] = useState<boolean | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [dropAllTerrains, { isLoading }] = useDropAllTerrainsMutation();

  const callDropAllTerrains = async () => {
    setErrorMessage(null);
    try {
      const res = await dropAllTerrains().unwrap();
      setLastInProgress(res.inProgress);
    } catch {
      setErrorMessage("Не удалось выполнить запрос удаления террейнов.");
    }
  };

  const handleClick = async () => {
    if (!hasAskedConfirmation) {
      setHasAskedConfirmation(true);
      setConfirmOpen(true);
      return;
    }
    await callDropAllTerrains();
  };

  return (
    <Container size="lg">
      <Stack gap="sm">
        <Text fw={600} fz="lg">
          Удаление всех террейнов
        </Text>

        <Button
          onClick={handleClick}
          loading={isLoading}
          disabled={isLoading}
          variant="outline"
          size="compact-sm"
          style={{ alignSelf: "center" }}
        >
          Удалить все террейны
        </Button>

        {lastInProgress !== null && (
          <Alert mt="xs" color={lastInProgress ? "yellow" : "green"}>
            {lastInProgress
              ? "Удаление выполняется. Нажмите снова, чтобы обновить статус."
              : "Удаление не выполняется. Нажмите снова, чтобы проверить статус."}
          </Alert>
        )}

        {errorMessage && (
          <Text c="red" size="sm">
            {errorMessage}
          </Text>
        )}
      </Stack>

      <Modal
        opened={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        title="Подтверждение"
        centered
      >
        <Stack>
          <Text>
            Это удалит все террейны. Вы уверены, что хотите продолжить?
          </Text>
          <Button
            onClick={async () => {
              setConfirmOpen(false);
              await callDropAllTerrains();
            }}
          >
            Подтвердить
          </Button>
        </Stack>
      </Modal>
    </Container>
  );
};

