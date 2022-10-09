@file:JvmName("IBGatewayLogin")

package ibgatewaylogin

import java.awt.Toolkit
import java.awt.Window
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.instrument.Instrumentation

/**
 * IBGatewayLogin is the component responsible for the interaction with the IBGateway user interface.
 */
class IBGatewayLoginManager(userName: String, password: String, tradingMode: String, portNumber: Int) {
    /**
     * Gets the IBLoginSettings settings.
     *
     * @return Returns the [IBLoginSettings] instance
     */
    val settings: IBLoginSettings
    private lateinit var printWriter: PrintWriter

    var mainWindow: Window? = null

    internal fun run() {
        try {
            printWriter = PrintWriter(FileWriter("ib-gateway-login.log"), true)
        } catch (exception: IOException) {
            println(exception.message)
        }
        Toolkit.getDefaultToolkit().addAWTEventListener(WindowEventListener(this), 64L)
        logMessage("IBGateway started")
    }

    /**
     * Writes the text message to the log file.
     *
     * @param text The text message to be logged
     */
    fun logMessage(text: String?) {
        try {
            printWriter.println(text)
        } catch (exception: Exception) {
            println(exception.message)
        }
    }

    /**
     * Writes the exception message to the log file.
     *
     * @param exception The exception to be logged
     */
    fun logError(exception: Exception) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        logMessage("Error: $sw")
    }

    init {
        settings = IBLoginSettings(userName, password, tradingMode, portNumber)
    }
}

/**
 * The Java agent premain method is called before the IBGateway main method.
 */
fun premain(args: String?, instrumentation: Instrumentation) {
    val userName = System.getenv("IB_USERNAME")
    val password = System.getenv("IB_PASSWORD")
    val tradingMode = System.getenv("IB_TRADING_MODE")
    val portNumber = System.getenv("IB_PORT_NUMBER").toInt()
    val gatewayLogin = IBGatewayLoginManager(userName, password, tradingMode, portNumber)
    gatewayLogin.run()
}
