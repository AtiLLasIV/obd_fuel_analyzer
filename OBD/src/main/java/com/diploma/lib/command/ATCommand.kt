package com.lib.obd.command

abstract class ATCommand : ObdCommand() {
    override val mode = "AT"
    override val skipDigitCheck = true
}
