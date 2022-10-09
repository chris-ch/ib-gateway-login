package ibgatewaylogin

import java.awt.Toolkit
import java.awt.Window
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

/**
 * IBGatewayLogin is the component responsible for the interaction with the IBGateway user interface.
 */
class IBGatewayLogin(userName: String, password: String, tradingMode: String, portNumber: Int) {
    /**
     * Gets the IBAutomater settings.
     *
     * @return Returns the [IBLoginSettings] instance
     */
    val settings: IBLoginSettings
    private lateinit var printWriter: PrintWriter

    var mainWindow: Window? = null

    private fun run() {
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

    companion object {
        /**
         * The Java agent premain method is called before the IBGateway main method.
         *
         * @param args The name of a text file containing the values of the IBAutomater settings
         */
        fun premain(args: String?) {
            val userName = System.getenv("IB_USERNAME")
            val password = System.getenv("IB_PASSWORD")
            val tradingMode = System.getenv("IB_TRADING_MODE")
            val portNumber = System.getenv("IB_PORT_NUMBER").toInt()
            val automater = IBGatewayLogin(userName, password, tradingMode, portNumber)
            automater.run()
        }
    }

    init {
        settings = IBLoginSettings(userName, password, tradingMode, portNumber)
    }
}