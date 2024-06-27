package org.onflow.flow.sdk

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerProvider {
    private var _logger: Logger = LoggerFactory.getLogger("DefaultLogger")

    var logger: Logger
        get() = _logger
        set(value) {
            _logger = value
        }
}
