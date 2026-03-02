package online.worldseed.model.enums;

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
