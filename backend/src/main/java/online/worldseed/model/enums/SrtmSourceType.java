package online.worldseed.generator.service.srtm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Источник высотных данных
 */
@AllArgsConstructor
@Getter
public enum SrtmSourceType {
    VFP,
    ARDUPILOT,
    CGIAR
}
