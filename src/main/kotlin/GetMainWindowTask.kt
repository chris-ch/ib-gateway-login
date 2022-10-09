package ibgatewaylogin

import java.awt.Window
import java.util.concurrent.Callable

/**
 * Handles the task of finding the IBGateway main window and waiting for it to be ready.
 *
 * @author QuantConnect Corporation
 */
class GetMainWindowTask
/**
 * Creates a new instance of the [GetMainWindowTask] class.
 *
 * @param automater The [ibgatewaylogin.IBGatewayLogin] instance
 */ internal constructor(private val automater: IBGatewayLogin) : Callable<Window> {
    /**
     * Returns the IBGateway main window, or throws an exception if unable to do so.
     *
     * @return Returns the IBGateway main window
     */
    @Throws(Exception::class)
    override fun call(): Window {
        while (true) {
            automater.logMessage("finding main window...")
            for (w in Window.getWindows()) {
                val menuItem = getMenuItem(w, "Configure", "IBAutomaterSettings")
                if (menuItem != null) {
                    automater.logMessage("found main window (Window title: [" + getTitle(w) + "] - Window name: [" + w.name + "])")

                    // when the main window is found and is ready,
                    // save it for future use and open the configuration window
                    automater.mainWindow = w
                    menuItem.doClick()
                    return w
                }
            }
            automater.logMessage("main window not found.")
            Thread.sleep(1000)
        }
    }
}