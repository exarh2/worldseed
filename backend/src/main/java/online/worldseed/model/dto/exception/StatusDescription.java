package online.worldseed.model.dto.exception;

public final class StatusDescription {
    public static final String HTTP_200_DESC = "Запрос выполнен успешно";
    public static final String HTTP_201_DESC = "Успешно создано";
    public static final String HTTP_400_DESC = "Некорректный запрос";
    public static final String HTTP_401_DESC = "Требуется авторизация";
    public static final String HTTP_403_DESC = "Недостаточно прав доступа";
    public static final String HTTP_404_DESC = "Информация не найдена";
    public static final String HTTP_406_DESC = "Запрошен неподдерживаемый тип содержимого";
    public static final String HTTP_422_DESC = "Запрос не может быть обработан";
    public static final String HTTP_460_DESC = "Ошибка прохождения электронных контролей";
    public static final String HTTP_500_DESC = "Внутренняя ошибка сервера";
    public static final String HTTP_501_DESC = "Операция не поддерживается";
    public static final String HTTP_503_DESC = "Сервис недоступен";
    public static final String HTTP_504_DESC = "Превышено время ожидания ответа от внешней системы";

    private StatusDescription() {
    }
}
