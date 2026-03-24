package online.worldseed.service.srtm;

public interface StrmConstants {
    //Разрешение DEM3 шагов на градус
    int DEM3_RESOLUTION = 1200;
    //Размер файла DEM c 3 секундами
    int DEM3_FILE_SIZE = (DEM3_RESOLUTION + 1) * (DEM3_RESOLUTION + 1) * 2/*байта*/;

    void fake();
}
