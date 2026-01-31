package ua.nin.media.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "media")
public class MediaProperties {

    private Storage storage = new Storage();
    private Limits limits = new Limits();

    @Getter
    @Setter
    public static class Storage {
        private Local local = new Local();

        @Getter
        @Setter
        public static class Local {
            /**
             * Base directory for local storage.
             * Example: ./storage
             */
            private String root = "./storage";
        }
    }

    @Getter
    @Setter
    public static class Limits {
        /**
         * Max allowed upload size. Spring multipart limits are configured separately.
         */
        private long maxSizeBytes = 50L * 1024 * 1024; // 50 MB
    }
}
