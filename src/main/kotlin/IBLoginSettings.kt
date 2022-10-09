package ibgatewaylogin

/**
 * Creates a new instance of the {@link IBAutomaterSettings} class.
 *
 * @param userName The IB username
 * @param password The IB password
 * @param tradingMode The trading mode (allowed values are "live" and "paper")
 * @param portNumber The socket port number to be used for API connections
 */
class IBLoginSettings(
    val userName: String,
    val password: String,
    val tradingMode: String,
    val portNumber: Int
)
