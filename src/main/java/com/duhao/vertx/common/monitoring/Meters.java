package com.duhao.vertx.common.monitoring;

import io.micrometer.core.instrument.Metrics;
import lombok.experimental.UtilityClass;

/**
 * @author Hao Du
 * @version 1.0
 * @since 2022/5/17
 */
@UtilityClass
public class Meters {

    public static void errorIndex() {
        Metrics.counter("error_index").increment();
    }
}
