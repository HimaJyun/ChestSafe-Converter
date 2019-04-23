package jp.jyn.chestsafe_converter.config;

import java.util.Queue;

@FunctionalInterface
public interface InteractiveConfig {
    /**
     * Execute setting
     *
     * @param args User input
     * @return Is it executable? If false, you need to reset the setting.
     */
    boolean next(Queue<String> args);

    /**
     * Check the required requirements
     *
     * @return Are all the requirements? In case of false, do not proceed next.
     */
    default boolean check() {
        return true;
    }
}
