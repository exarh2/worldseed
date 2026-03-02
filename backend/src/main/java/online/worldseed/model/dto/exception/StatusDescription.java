package online.worldseed.model.dto.exception;

public interface StatusDescription {
    String HTTP_200_DESC = "Запрос выполнен успешно";
    String HTTP_201_DESC = "Успешно создано";
    String HTTP_400_DESC = "Некорректный запрос";
    String HTTP_401_DESC = "Требуется авторизация";
    String HTTP_403_DESC = "Недостаточно прав доступа";
    String HTTP_404_DESC = "Информация не найдена";
    String HTTP_406_DESC = "Запрошен неподдерживаемый тип содержимого";
    String HTTP_422_DESC = "Запрос не может быть обработан";
    String HTTP_460_DESC = "Ошибка прохождения электронных контролей";
    String HTTP_500_DESC = "Внутренняя ошибка сервера";
    String HTTP_501_DESC = "Операция не поддерживается";
    String HTTP_503_DESC = "Сервис недоступен";
    String HTTP_504_DESC = "Превышено время ожидания ответа от внешней системы";
}
