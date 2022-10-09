package ibgatewaylogin

import java.awt.AWTEvent
import java.awt.Component
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.AWTEventListener
import java.awt.event.WindowEvent
import java.io.File
import java.lang.Exception
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.*
import java.util.function.Consumer
import javax.swing.*
import javax.swing.tree.TreePath

/**
 * The event listener implementation handles the detection and handling of known and supported IBGateway windows.
 *
 * @author QuantConnect Corporation
 */
class WindowEventListener
/**
 * Creates a new instance of the [WindowEventListener] class.
 *
 * @param automater The [ibgatewaylogin.IBGatewayLoginManager] instance
 */ internal constructor(private val automater: IBGatewayLoginManager) : AWTEventListener {
    private val handledEvents: HashMap<Int?, String?> = object : HashMap<Int?, String?>() {
        init {
            this[WindowEvent.WINDOW_OPENED] = "WINDOW_OPENED"
            this[WindowEvent.WINDOW_ACTIVATED] = "WINDOW_ACTIVATED"
            this[WindowEvent.WINDOW_DEACTIVATED] = "WINDOW_DEACTIVATED"
            this[WindowEvent.WINDOW_CLOSING] = "WINDOW_CLOSING"
            this[WindowEvent.WINDOW_CLOSED] = "WINDOW_CLOSED"
        }
    }
    private var isAutoRestartTokenExpired = false
    private var restartNow = false
    private var viewLogsWindow: Window? = null
    private var twoFactorConfirmationRequestTime: Instant? = null
    private var twoFactorConfirmationAttempts = 0

    /**
     * Invoked when an event is dispatched in the AWT.
     *
     * @param awtEvent The event to be processed
     */
    override fun eventDispatched(awtEvent: AWTEvent) {
        val eventId = awtEvent.id
        val window = (awtEvent as WindowEvent).window
        when {
            handledEvents.containsKey(eventId) -> {
                automater.logMessage("Window event: [" + handledEvents[eventId] + "] - Window title: [" + getTitle(window) + "] - Window name: [" + window.name + "]")
            }
            else -> {
                return
            }
        }
        try {
            when {
                handleLoginWindow(window, eventId) -> {
                    return
                }
                handleLoginFailedWindow(window, eventId) -> {
                    return
                }
                handleServerDisconnectedWindow(window, eventId) -> {
                    return
                }
                handleTooManyFailedLoginAttemptsWindow(window, eventId) -> {
                    return
                }
                handlePasswordNoticeWindow(window, eventId) -> {
                    return
                }
                handleInitializationWindow(window, eventId) -> {
                    return
                }
                handlePaperTradingAccountWindow(window, eventId) -> {
                    return
                }
                handleUnsupportedVersionWindow(window, eventId) -> {
                    return
                }
                handleConfigurationWindow(window, eventId) -> {
                    return
                }
                handleExistingSessionDetectedWindow(window, eventId) -> {
                    return
                }
                handleReloginRequiredWindow(window, eventId) -> {
                    return
                }
                handleFinancialAdvisorWarningWindow(window, eventId) -> {
                    return
                }
                handleExitSessionSettingWindow(window, eventId) -> {
                    return
                }
                handleApiNotAvailableWindow(window, eventId) -> {
                    return
                }
                handleEnableAutoRestartConfirmationWindow(window, eventId) -> {
                    return
                }
                handleAutoRestartTokenExpiredWindow(window, eventId) -> {
                    return
                }
                handleViewLogsWindow(window, eventId) -> {
                    return
                }
                handleExportFileNameWindow(window, eventId) -> {
                    return
                }
                handleExportFinishedWindow(window, eventId) -> {
                    return
                }
                handleAutoRestartNowWindow(window, eventId) -> {
                    return
                }
                handleTwoFactorAuthenticationWindow(window, eventId) -> {
                    return
                }
                handleDisplayMarketDataWindow(window, eventId) -> {
                    return
                }
                handleUseSslEncryptionWindow(window, eventId) -> {
                    return
                }
                else -> handleUnknownMessageWindow(window, eventId)
            }
        } catch (e: Exception) {
            automater.logError(e)
        }
    }

    /**
     * Detects and handles the main login window.
     * - selects the "IB API" toggle button
     * - selects the "Live Trading" or "Paper Trading" toggle button
     * - enters the IB user name and password
     * - selects the "Use SSL" check box
     * - clicks the "Log In" or "Paper Log In" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handleLoginWindow(window: Window?, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window!!)

        // v981
        if (!isFrame(window) || title != "IB Gateway" && title != "Interactive Brokers Gateway") {
            return false
        }
        automater.mainWindow = window
        automater.logMessage("Main window - Window title: [" + title + "] - Window name: [" + window.name + "]")
        val isLiveTradingMode = automater.settings.tradingMode == "live"
        val buttonIbApiText = "IB API"
        val ibApiButton = getToggleButton(window, buttonIbApiText)
        if (ibApiButton == null) {
            automater.logMessage("Unexpected window found")
            logWindowContents(window)
            throw Exception("IB API toggle button not found")
        }
        if (!ibApiButton.isSelected) {
            automater.logMessage("Click button: [$buttonIbApiText]")
            ibApiButton.doClick()
        }
        val buttonTradingModeText = if (isLiveTradingMode) "Live Trading" else "Paper Trading"
        val tradingModeButton = getToggleButton(window, buttonTradingModeText)
            ?: throw Exception("Trading Mode toggle button not found")
        if (!tradingModeButton.isSelected) {
            automater.logMessage("Click button: [$buttonTradingModeText]")
            tradingModeButton.doClick()
        }
        automater.logMessage("Trading mode: " + automater.settings.tradingMode)
        val userNameTextField = getTextField(window, 0)
            ?: throw Exception("IB API user name text field not found")
        userNameTextField.text = automater.settings.userName
        val passwordTextField = getTextField(window, 1)
            ?: throw Exception("IB API password text field not found")
        passwordTextField.text = automater.settings.password
        val useSslText = "Use SSL"
        val useSslCheckbox = getCheckBox(window, useSslText)
        if (useSslCheckbox == null) {
            automater.logMessage("Use SSL checkbox not found")
        } else {
            if (!useSslCheckbox.isSelected) {
                automater.logMessage("Select checkbox: [$useSslText]")
                useSslCheckbox.isSelected = true
            }
        }
        val loginButtonText = if (isLiveTradingMode) "Log In" else "Paper Log In"
        val loginButton = getButton(window, loginButtonText) ?: throw Exception("Login button not found")
        automater.logMessage("Click button: [$loginButtonText]")
        loginButton.doClick()
        return true
    }

    /**
     * Detects and handles the login failed window.
     * - logs the error message text
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleLoginFailedWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title == "Login failed") {
            val textPane = getTextPane(window)
            var text = ""
            if (textPane != null) {
                text = textPane.text.replace("<.*?>".toRegex(), " ").trim { it <= ' ' }
            }
            automater.logMessage("Login failed: $text")
            val button = getButton(window, "OK")
            if (button != null) {
                automater.logMessage("Click button: [OK]")
                button.doClick()
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Server Disconnected window.
     * - clicks the "OK" button
     * - closes the main window
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleServerDisconnectedWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val text = getWindowText(window)
        if (text.contains("Connection to server failed: Server disconnected, please try again")) {
            automater.logMessage(text)
            val button = getButton(window, "OK")
            if (button != null) {
                automater.logMessage("Click button: [OK]")
                button.doClick()
            }
            automater.logMessage("Server disconnection detected, closing IBGateway.")
            closeMainWindow()
            return true
        }
        return false
    }

    /**
     * Detects and handles the "Too many failed login attempts" window.
     * - clicks the "OK" button
     * - closes the main window
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleTooManyFailedLoginAttemptsWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val text = getWindowText(window)
        if (text.contains("Too many failed login attempts")) {
            automater.logMessage(text)
            val button = getButton(window, "OK")
            if (button != null) {
                automater.logMessage("Click button: [OK]")
                button.doClick()
            }
            automater.logMessage("Too many failed login attempts, closing IBGateway.")
            closeMainWindow()
            return true
        }
        return false
    }

    /**
     * Detects and handles the Password Notice window.
     * - logs the error message text
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handlePasswordNoticeWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title.contains("Password Notice")) {
            val textPane = getTextPane(window)
            var text = ""
            if (textPane != null) {
                text = textPane.text.replace("<.*?>".toRegex(), " ").trim { it <= ' ' }
            }
            automater.logMessage("Login failed: $text")
            val button = getButton(window, "OK")
            if (button != null) {
                automater.logMessage("Click button: [OK]")
                button.doClick()
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Initialization window.
     * - starts the [GetMainWindowTask] task to find the main window
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleInitializationWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_CLOSED) {
            return false
        }
        val title = getTitle(window)
        if (title.contains("Starting application...")) {
            // The main window might not be completely initialized at this point,
            // so we start a task and wait 30 seconds maximum for the window to be ready.
            runInitializationUsingThread()
            runRestartWatcher()
        }
        return false
    }

    /**
     * Will start a thread which will get the main window and setup our settings
     *
     */
    private fun runInitializationUsingThread() {
        Thread {
            val executor = Executors.newSingleThreadExecutor()
            val future = executor.submit(GetMainWindowTask(automater))
            try {
                future[30, TimeUnit.SECONDS]
            } catch (e: InterruptedException) {
                automater.logError(e)
            } catch (e: ExecutionException) {
                automater.logError(e)
            } catch (e: TimeoutException) {
                automater.logError(e)
            }
            executor.shutdown()
        }.start()
    }

    /**
     * Will start a thread which will monitor for restart requests and trigger a restart when detected
     *
     */
    private fun runRestartWatcher() {
        Thread {
            automater.logMessage("Start running restart watcher thread...")
            while (true) {
                try {
                    val file = File("restart")
                    if (file.exists()) {
                        if (file.delete()) {
                            automater.logMessage("Restart request detected, starting restart...")
                            restartNow = true
                            runInitializationUsingThread()
                        } else {
                            automater.logError(RuntimeException("failed to clean restart file"))
                        }
                    }
                    Thread.sleep((1000 * 10).toLong())
                } catch (ex: InterruptedException) {
                    // stopped
                }
            }
        }.start()
    }

    /**
     * Detects and handles the Paper Trading warning window.
     * - clicks the "I understand and accept" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handlePaperTradingAccountWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        if (getLabel(window, "This is not a brokerage account") == null) {
            return false
        }
        val buttonText = "I understand and accept"
        val button = getButton(window, buttonText)
        if (button != null) {
            automater.logMessage("Click button: [$buttonText]")
            button.doClick()
        } else {
            throw Exception("Button not found: [$buttonText]")
        }
        return true
    }

    /**
     * Detects and handles the Unsupported Version window.
     * - logs the error message text
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleUnsupportedVersionWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        getTitle(window)
        return false
    }

    /**
     * Detects and handles the Configuration window.
     * - in the Configuration/API/IBAutomaterSettings panel:
     * - deselects the "Read-Only API" check box
     * - sets the API Port Number
     * - selects the "Create API message log file" check box
     * - deselects the "Use Account Groups with Allocation Methods" check box
     * - in the Configuration/API/Precautions panel:
     * - selects the "Bypass Order Precautions for API Orders" check box
     * - in the Configuration/Lock and Exit panel:
     * - selects the "Auto restart" check box
     * - if requested, opens the Export IB logs window
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handleConfigurationWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (!title.contains(" Configuration")) {
            return false
        }
        val tree = getTree(window) ?: throw Exception("Configuration tree not found")
        selectTreeNode(tree, TreePath(arrayOf("Configuration", "API", "IBAutomaterSettings")))
        val readOnlyApiText = "Read-Only API"
        val readOnlyApi = getCheckBox(window, readOnlyApiText)
            ?: throw Exception("Read-Only API check box not found")
        if (readOnlyApi.isSelected) {
            automater.logMessage("Unselect checkbox: [$readOnlyApiText]")
            readOnlyApi.isSelected = false
        }
        val portNumber = getTextField(window, 0) ?: throw Exception("API Port Number text field not found")
        val portText = Integer.toString(automater.settings.portNumber)
        automater.logMessage("Set API port textbox value: [$portText]")
        portNumber.text = portText
        val createApiLogText = "Create API message log file"
        val createApiLog = getCheckBox(window, createApiLogText)
            ?: throw Exception("'Create API message log file' check box not found")
        if (!createApiLog.isSelected) {
            automater.logMessage("Select checkbox: [$createApiLogText]")
            createApiLog.isSelected = true
        }

        // v983+
        val faText = "Use Account Groups with Allocation Methods"
        val faCheckBox = getCheckBox(window, faText)
        if (faCheckBox != null) {
            if (faCheckBox.isSelected) {
                automater.logMessage("Unselect checkbox: [$faText]")
                faCheckBox.isSelected = false
            }
        }
        selectTreeNode(tree, TreePath(arrayOf("Configuration", "API", "Precautions")))
        val bypassOrderPrecautionsText = "Bypass Order Precautions for API Orders"
        val bypassOrderPrecautions = getCheckBox(window, bypassOrderPrecautionsText)
            ?: throw Exception("Bypass Order Precautions check box not found")
        if (!bypassOrderPrecautions.isSelected) {
            automater.logMessage("Select checkbox: [$bypassOrderPrecautionsText]")
            bypassOrderPrecautions.isSelected = true
        }
        selectTreeNode(tree, TreePath(arrayOf("Configuration", "Lock and Exit")))
        val autoRestartText = "Auto restart"
        val autoRestart = getRadioButton(window, autoRestartText)
            ?: throw Exception("Auto restart radio button not found")
        if (!autoRestart.isSelected) {
            automater.logMessage("Select radio button: [$autoRestartText]")
            autoRestart.isSelected = true
        }
        val amButton = getRadioButton(window, "AM") ?: throw Exception("Auto restart AM button not found")
        val pmButton = getRadioButton(window, "PM") ?: throw Exception("Auto restart PM button not found")
        val restartTimeField = getTextField(window, 0) ?: throw Exception("Restart time text field not found")
        val dtf = DateTimeFormatter.ofPattern("hh:mma")
        // defaults
        var restartTime = "11:45"
        var timeButton = pmButton
        if (restartNow) {
            restartNow = false
            // will restart in 2 minutes
            val now = LocalDateTime.now().plusMinutes(2)
            val completeTime = dtf.format(now)
            restartTime = completeTime.substring(0, 5)
            if ("am".equals(completeTime.substring(5), ignoreCase = true)) {
                timeButton = amButton
            }
        }
        automater.logMessage("Set restart time value: [$restartTime]")
        restartTimeField.text = restartTime
        if (!timeButton.isSelected) {
            automater.logMessage("Select radio button: [" + timeButton.text + "]")
            timeButton.isSelected = true
        } else {
            automater.logMessage("Radio button: [" + timeButton.text + "] already selected")
        }
        val okButton = getButton(window, "OK") ?: throw Exception("OK button not found")
        automater.logMessage("Click button: [OK]")
        okButton.doClick()
        automater.logMessage("Configuration settings updated.")
        return true
    }

    /**
     * Detects and handles the Existing Session Detected window.
     * - clicks the "Exit Application" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handleExistingSessionDetectedWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title == "Existing session detected") {
            val buttonText = "Exit Application"
            val button = getButton(window, buttonText)
            if (button != null) {
                automater.logMessage("Click button: [$buttonText]")
                button.doClick()
            } else {
                throw Exception("Button not found: [$buttonText]")
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Re-login Required window.
     * - clicks the "Re-login" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handleReloginRequiredWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title == "Re-login is required") {
            val buttonText = "Re-login"
            val button = getButton(window, buttonText)
            if (button != null) {
                automater.logMessage("Click button: [$buttonText]")
                button.doClick()
            } else {
                throw Exception("Button not found: [$buttonText]")
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Financial Advisor warning window.
     * - clicks the "Yes" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handleFinancialAdvisorWarningWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title.contains("Financial Advisor Warning")) {
            val buttonText = "Yes"
            val button = getButton(window, buttonText)
            if (button != null) {
                automater.logMessage("Click button: [$buttonText]")
                button.doClick()
            } else {
                throw Exception("Button not found: [$buttonText]")
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Exit Session Setting window.
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handleExitSessionSettingWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_ACTIVATED) {
            return false
        }
        val title = getTitle(window)
        if (title.contains("Exit Session Setting")) {
            val text = java.lang.String.join(" ", getLabelTextLines(window))
            automater.logMessage("Content: $text")
            val buttonText = "OK"
            val button = getButton(window, buttonText)
            if (button != null) {
                automater.logMessage("Click button: [$buttonText]")
                button.doClick()
            } else {
                throw Exception("Button not found: [$buttonText]")
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the API support not available window (e.g. for IBKR Lite accounts).
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleApiNotAvailableWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        getTitle(window)
        return false
    }

    /**
     * Detects and handles the AutoRestart confirmation window.
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleEnableAutoRestartConfirmationWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val textPane = getTextPane(window)
        var text = ""
        if (textPane != null) {
            text = textPane.text.replace("<.*?>".toRegex(), " ").trim { it <= ' ' }
        }
        if (!text.contains("You have elected to have your trading platform restart automatically")) {
            return false
        }
        automater.logMessage(text)
        val button = getButton(window, "OK")
        if (button != null) {
            automater.logMessage("Click button: [OK]")
            button.doClick()
        }
        return true
    }

    /**
     * Detects and handles the AutoRestart Token Expired window.
     * - clicks the "OK" button
     * - closes the main window
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    @Throws(Exception::class)
    private fun handleAutoRestartTokenExpiredWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        if (getLabel(window, "Soft token=0 received instead of expected permanent") == null) {
            return false
        }
        val buttonText = "OK"
        val button = getButton(window, buttonText)
        if (button != null) {
            automater.logMessage("Click button: [$buttonText]")
            button.doClick()
        } else {
            throw Exception("Button not found: [$buttonText]")
        }

        // we can do this only once, to avoid closing the restarted process
        if (!isAutoRestartTokenExpired) {
            isAutoRestartTokenExpired = true
            automater.logMessage("Auto-restart token expired, closing IBGateway")
            closeMainWindow()
        }
        return true
    }

    /**
     * Detects and handles the AutoRestart Now window.
     * - clicks the "No" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleAutoRestartNowWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val text = getWindowText(window)
        if (text.contains("Would you like to restart now?")) {
            automater.logMessage(text)
            val button = getButton(window, "No")
            if (button != null) {
                automater.logMessage("Click button: [No]")
                button.doClick()
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Two Factor Authentication window.
     * - if the window is closed within 150 seconds since it was opened, 2FA confirmation was successful,
     * otherwise it is considered a timeout and other two attempts to login are performed
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleTwoFactorAuthenticationWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED && eventId != WindowEvent.WINDOW_CLOSED) {
            return false
        }
        val title = getTitle(window)
        if (title == "Second Factor Authentication") {
            val maxTwoFactorConfirmationAttempts = 3
            if (eventId == WindowEvent.WINDOW_OPENED) {
                automater.logMessage("selecting IB Key")
                selectListItem(window, "IB Key")
                val buttonText = "OK"
                val button = getButton(window, buttonText)
                if (button != null) {
                    automater.logMessage("click button: [$buttonText]")
                    button.doClick()
                }

                twoFactorConfirmationRequestTime = Instant.now()
                twoFactorConfirmationAttempts++
                automater.logMessage("twoFactorConfirmationAttempts: $twoFactorConfirmationAttempts/$maxTwoFactorConfirmationAttempts")
                return true
            } else {
                val delta = Duration.between(twoFactorConfirmationRequestTime, Instant.now())
                // the timeout can be a few seconds earlier than 3 minutes, so we use 150 seconds to be safe
                if (delta >= Duration.ofSeconds(150)) {
                    automater.logMessage("2FA confirmation timeout")
                    if (twoFactorConfirmationAttempts == maxTwoFactorConfirmationAttempts) {
                        automater.logMessage("2FA maximum attempts reached")
                    } else {
                        automater.logMessage("New login attempt with 2FA")
                        Thread {
                            try {
                                val delay = 10000 * twoFactorConfirmationAttempts

                                // IB considers a 2FA timeout as a failed login attempt
                                // so we wait before retrying to avoid the "Too many failed login attempts" error
                                Thread.sleep(delay.toLong())

                                // execute asynchronously on the AWT event dispatching thread
                                SwingUtilities.invokeLater {
                                    try {
                                        val mainWindow = automater.mainWindow
                                        handleLoginWindow(mainWindow, WindowEvent.WINDOW_OPENED)
                                    } catch (e: Exception) {
                                        automater.logMessage("HandleLoginWindow error: " + e.message)
                                    }
                                }
                            } catch (e: Exception) {
                                automater.logMessage("HandleLoginWindow error: " + e.message)
                            }
                        }.start()
                    }
                } else {
                    automater.logMessage("2FA confirmation success")
                    twoFactorConfirmationAttempts = 0
                }
                return true
            }
        }
        return false
    }

    /**
     * Detects and handles the Display Market Data window.
     * - clicks the "I understand - display market data" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleDisplayMarketDataWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val text = getWindowText(window)
        if (text.contains("Bid, Ask and Last Size Display Update")) {
            automater.logMessage(text)
            val buttonText = "I understand - display market data"
            val button = getButton(window, buttonText)
            if (button != null) {
                automater.logMessage("Click button: [$buttonText]")
                button.doClick()
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Use SSL Encryption window.
     * - clicks the "Reconnect using SSL" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleUseSslEncryptionWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title.contains("Use SSL encryption")) {
            val buttonText = "Reconnect using SSL"
            val button = getButton(window, buttonText)
            if (button != null) {
                automater.logMessage("Click button: [$buttonText]")
                button.doClick()
            }
            return true
        }
        return false
    }

    /**
     * Returns whether the given window title is known.
     *
     * @param title The window title
     *
     * @return Returns true if the window title is known, false otherwise
     */
    private fun isKnownWindowTitle(title: String): Boolean {
        return title == "Second Factor Authentication" || title == "Security Code Card Authentication" || title == "Enter security code"
    }

    /**
     * Gets the text content of the window (labels, text panes and text areas only).
     *
     * @param window The window instance
     *
     * @return Returns the text content of the window
     */
    private fun getWindowText(window: Window): String {
        var text = ""
        val textPane = getTextPane(window)
        if (textPane != null) {
            val t = textPane.text
            if (t != null) {
                text += t.replace("<.*?>".toRegex(), " ").trim { it <= ' ' }
            }
        }
        val textArea = getTextArea(window)
        if (textArea != null) {
            val t = textArea.text
            if (t != null) {
                text += " " + t.replace("<.*?>".toRegex(), " ").trim { it <= ' ' }
            }
        }
        text += " " + java.lang.String.join(" ", getLabelTextLines(window))
        return text
    }

    /**
     * Detects and handles an unknown message window.
     * - if requested, opens the Export IB logs window
     * - logs the window structure
     * - clicks the "OK" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     */
    private fun handleUnknownMessageWindow(window: Window, eventId: Int) {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return
        }
        val title = getTitle(window)
        val windowName = window.name
        if (windowName != null && windowName.startsWith("dialog") && !isKnownWindowTitle(title)) {
            logWindowContents(window)
            val text = getWindowText(window)
            automater.logMessage("Unknown message window detected: $text")
        }
    }

    /**
     * Detects and handles the View Logs window.
     * - clicks the "Export Today Logs..." button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleViewLogsWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title.contains("View Logs")) {
            var buttonText = "Export Today Logs..."
            var button = getButton(window, buttonText)
            if (button != null) {
                if (button.isEnabled) {
                    viewLogsWindow = window
                    automater.logMessage("Click button: [$buttonText]")
                    button.doClick()
                } else {
                    buttonText = "Cancel"
                    button = getButton(window, buttonText)
                    if (button != null) {
                        automater.logMessage("Click button: [$buttonText]")
                        button.doClick()
                    }
                }
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Enter Export Filename window.
     * - clicks the "Open" button
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleExportFileNameWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        val title = getTitle(window)
        if (title.contains("Enter export filename")) {
            val buttonText = "Open"
            val button = getButton(window, buttonText)
            if (button != null) {
                automater.logMessage("Click button: [$buttonText]")
                button.doClick()
            }
            return true
        }
        return false
    }

    /**
     * Detects and handles the Export Finished window.
     * - clicks the "OK" button
     * - clicks the "Cancel" button on the parent window (View Logs)
     *
     * @param window The window instance
     * @param eventId The id of the window event
     *
     * @return Returns true if the window was detected and handled
     */
    private fun handleExportFinishedWindow(window: Window, eventId: Int): Boolean {
        if (eventId != WindowEvent.WINDOW_OPENED) {
            return false
        }
        if (getOptionPane(window, "Finished exporting logs") == null) {
            return false
        }
        val button = getButton(window, "OK")
        if (button != null) {
            automater.logMessage("Click button: [OK]")
            button.doClick()
        }
        val buttonText = "Cancel"
        val cancelButton = getButton(viewLogsWindow!!, buttonText)
        if (cancelButton != null) {
            viewLogsWindow = null
            automater.logMessage("Click button: [$buttonText]")
            cancelButton.doClick()
        }
        return true
    }

    /**
     * Closes the main window.
     */
    private fun closeMainWindow() {
        Thread {
            automater.logMessage("closeMainWindow thread started")
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                try {
                    val mainWindow = automater.mainWindow
                    automater.logMessage("Closing main window - Window title: [" + getTitle(mainWindow!!) + "] - Window name: [" + mainWindow.name + "]")
                    (automater.mainWindow as JFrame?)!!.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
                    val closingEvent = WindowEvent(automater.mainWindow, WindowEvent.WINDOW_CLOSING)
                    Toolkit.getDefaultToolkit().systemEventQueue.postEvent(closingEvent)
                    automater.logMessage("close main window message sent")
                } catch (e: Exception) {
                    automater.logMessage("closeMainWindow execute error: " + e.message)
                }
            }
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    automater.logMessage("timeout in execution of CloseMainWindow")
                }
            } catch (e: InterruptedException) {
                automater.logMessage("closeMainWindow await error: " + e.message)
            }
            automater.logMessage("closeMainWindow thread ended")
        }.start()
    }

    /**
     * Logs the structure of the specified window.
     *
     * @param window The window instance
     */
    private fun logWindowContents(window: Window?) {
        val components = getComponents(window!!)
        automater.logMessage("DEBUG: Window title: [" + getTitle(window) + "] - Window name: [" + window.name + "]")
        components.forEach(Consumer { component: Component? ->
            var text = ""
            if (component is JLabel) {
                text = " - Text: [" + component.text + "]"
            } else if (component is JTextPane) {
                text = " - Text: [" + component.text + "]"
            } else if (component is JTextField) {
                text = " - Text: [" + component.text + "]"
            } else if (component is JTextArea) {
                text = " - Text: [" + component.text + "]"
            } else if (component is JCheckBox) {
                text = " - Text: [" + component.text + "]"
            } else if (component is JOptionPane) {
                text = " - Message: [" + component.message.toString() + "]"
            }
            automater.logMessage("DEBUG: - Component: [" + component.toString() + "]" + text)
        })
    }
}