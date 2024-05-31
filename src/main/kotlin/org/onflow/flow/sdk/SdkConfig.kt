package org.onflow.flow.sdk

import org.slf4j.Logger

class SdkConfig {
    fun setLogger(customLogger: Logger) {
        LoggerProvider.logger = customLogger
    }
}
