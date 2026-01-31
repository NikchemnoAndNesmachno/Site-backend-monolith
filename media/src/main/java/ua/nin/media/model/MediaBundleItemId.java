package ua.nin.media.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MediaBundleItemId implements Serializable {

    private Long bundleId;
    @Enumerated(EnumType.STRING)
    private BundleItemRole role;
}
