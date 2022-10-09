package ibgatewaylogin

import java.awt.*
import java.util.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

/**
 * Gets the first JTree instance in the container.
 *
 * @param container The container to be queried
 *
 * @return Returns the first JTree instance in the given container, null if the tree is not found
 */
fun getTree(container: Container): JTree? {
    val trees = ArrayList<Component>()
    loadComponents(container, JTree::class.java, trees)
    return if (trees.size > 0) trees[0] as JTree else null
}

/**
 * Recursively gets all components in the given container.
 *
 * @param container The container to be queried
 *
 * @return Returns a list of all components contained in the given container
 */
fun getComponents(container: Container): List<Component?> {
    val components: MutableList<Component?> = ArrayList()
    for (component in container.components) {
        if (component is Container) {
            loadAllComponents(component, components)
        }
        components.add(component)
    }
    return components
}

/**
 * Selects the tree node in the given tree with the specified tree path.
 *
 * @param tree The tree for which the node is to be selected
 * @param path The tree path for the tree node to be selected
 */
fun selectTreeNode(tree: JTree, path: TreePath) {
    val rootNode = tree.model.root as DefaultMutableTreeNode
    selectNode(tree, rootNode, path)
}

/**
 * Gets a list of label text lines in the container.
 *
 * @param container The container to be queried
 *
 * @return Returns a list of label text lines found in the given container
 */
fun getLabelTextLines(container: Container): List<String> {
    val lines: MutableList<String> = ArrayList()
    val labels = ArrayList<Component>()
    loadComponents(container, JLabel::class.java, labels)
    for (component in labels) {
        val label = component as JLabel
        val labelText = label.text
        if (labelText != null && labelText.isNotEmpty()) {
            lines.add(labelText.replace("<.*?>".toRegex(), " ").trim { it <= ' ' })
        }
    }
    return lines
}

/**
 * Gets a JCheckBox instance with the specified text.
 *
 * @param container The container to be queried
 * @param text The check box text to find
 *
 * @return Returns a JCheckBox instance in the given container with the specified text, null if the check box is not found
 */
fun getCheckBox(container: Container, text: String?): JCheckBox? {
    val checkBoxes = ArrayList<Component>()
    loadComponents(container, JCheckBox::class.java, checkBoxes)
    for (component in checkBoxes) {
        val checkBox = component as JCheckBox
        val checkBoxText = checkBox.text
        if (checkBoxText == null || !checkBoxText.equals(text, ignoreCase = true)) continue
        return checkBox
    }
    return null
}

/**
 * Gets the first JTextPane instance in the container.
 *
 * @param container The container to be queried
 *
 * @return Returns the first JTextPane instance in the given container, null if the text pane is not found
 */
fun getTextPane(container: Container): JTextPane? {
    val textPanes = ArrayList<Component>()
    loadComponents(container, JTextPane::class.java, textPanes)
    return if (textPanes.size > 0) textPanes[0] as JTextPane else null
}

/**
 * Gets the first JTextArea instance in the container.
 *
 * @param container The container to be queried
 *
 * @return Returns the first JTextArea instance in the given container, null if the text area is not found
 */
fun getTextArea(container: Container): JTextArea? {
    val textAreas = ArrayList<Component>()
    loadComponents(container, JTextArea::class.java, textAreas)
    return if (textAreas.size > 0) textAreas[0] as JTextArea else null
}

/**
 * Gets a JMenuItem instance in the container with the specified menu item text in the menu with the specified menu text.
 *
 * @param container The container to be queried
 * @param menuText The menu item text to find
 * @param menuItemText The menu text to find
 *
 * @return Returns a JMenuItem instance in the given container with the specified text, null if the menu item is not found
 */
fun getMenuItem(container: Container?, menuText: String, menuItemText: String?): JMenuItem? {
    if (container == null) return null
    val menuBar = (container as JFrame).jMenuBar ?: return null
    for (i in 0 until menuBar.menuCount) {
        val menu = menuBar.getMenu(i)
        if (menu.text != menuText) continue
        for (j in 0 until menu.itemCount) {
            val menuItem = menu.getItem(j)
            if (menuItem == null || !menuItem.text.equals(menuItemText, ignoreCase = true)) continue
            return menuItem
        }
    }
    return null
}

/**
 * Gets a JOptionPane instance containing the specified text.
 *
 * @param container The container to be queried
 * @param text The option pane text to find
 *
 * @return Returns a JOptionPane instance in the given container containing the specified text, null if the option pane is not found
 */
fun getOptionPane(container: Container, text: String): JOptionPane? {
    val optionPanes = ArrayList<Component>()
    loadComponents(container, JOptionPane::class.java, optionPanes)
    for (component in optionPanes) {
        val optionPane = component as JOptionPane
        val optionPaneText = optionPane.message.toString()
        if (!optionPaneText.lowercase(Locale.getDefault())
                .contains(text.lowercase(Locale.getDefault()))
        ) continue
        return optionPane
    }
    return null
}

fun selectListItem(container: Container, listItemText: String) {
    val lists = ArrayList<Component>()
    loadComponents(container, JOptionPane::class.java, lists)
    for (component in lists) {
        val list = component as JList<*>
        val listModel = list.model
        for (i in 0 until listModel.size) {
            val item = listModel.getElementAt(i)
            if (item == listItemText) list.selectedIndex = i
        }
    }
}

/**
 * Gets a JTextField instance at the specified position in the container.
 *
 * @param container The container to be queried
 * @param index The index of the text field to return
 *
 * @return Returns a JTextField instance at the specified position in the list of text fields in the container, null if the index is not valid
 */
fun getTextField(container: Container, index: Int): JTextField? {
    val textFields = ArrayList<Component>()
    loadComponents(container, JTextField::class.java, textFields)
    return if (textFields.size > index) textFields[index] as JTextField else null
}

/**
 * Gets a JLabel instance containing the specified text.
 *
 * @param container The container to be queried
 * @param text The label text to find
 *
 * @return Returns a JLabel instance in the given container containing the specified text, null if the label is not found
 */
fun getLabel(container: Container, text: String): JLabel? {
    val labels = ArrayList<Component>()
    loadComponents(container, JLabel::class.java, labels)
    for (component in labels) {
        val label = component as JLabel
        val labelText = label.text
        if (labelText == null || !labelText.lowercase(Locale.getDefault())
                .contains(text.lowercase(Locale.getDefault()))
        ) continue
        return label
    }
    return null
}

/**
 * Gets a JRadioButton instance with the specified text.
 *
 * @param container The container to be queried
 * @param text The radio button text to find
 *
 * @return Returns a JRadioButton instance in the given container with the specified text, null if the radio button is not found
 */
fun getRadioButton(container: Container, text: String?): JRadioButton? {
    val buttons = ArrayList<Component>()
    loadComponents(container, JRadioButton::class.java, buttons)
    for (component in buttons) {
        val button = component as JRadioButton
        val buttonText = button.text
        if (buttonText == null || !buttonText.equals(text, ignoreCase = true)) continue
        return button
    }
    return null
}

/**
 * Gets a JToggleButton instance with the specified text.
 *
 * @param container The container to be queried
 * @param text The toggle button text to find
 *
 * @return Returns a JToggleButton instance in the given container with the specified text, null if the toggle button is not found
 */
fun getToggleButton(container: Container, text: String?): JToggleButton? {
    val buttons = ArrayList<Component>()
    loadComponents(container, JToggleButton::class.java, buttons)
    for (component in buttons) {
        val button = component as JToggleButton
        val buttonText = button.text
        if (buttonText == null || !buttonText.equals(text, ignoreCase = true)) continue
        return button
    }
    return null
}

/**
 * Gets a JButton instance with the specified text.
 *
 * @param container The container to be queried
 * @param text The button text to find
 *
 * @return Returns a JButton instance in the given container with the specified text, null if the button is not found
 */
fun getButton(container: Container, text: String?): JButton? {
    val buttons = ArrayList<Component>()
    loadComponents(container, JButton::class.java, buttons)
    for (component in buttons) {
        val button = component as JButton
        val buttonText = button.text
        if (buttonText == null || !buttonText.equals(text, ignoreCase = true)) continue
        return button
    }
    return null
}

/**
 * Gets the title of the window.
 *
 * @param window The window to be queried
 *
 * @return Returns the title of the window
 */
fun getTitle(window: Window): String {
    var title = ""
    if (isFrame(window)) {
        title = (window as Frame).title
    } else if (isDialog(window)) {
        title = (window as Dialog).title
    }
    return title
}

/**
 * Gets whether the window is a Frame window.
 *
 * @param window The window to be checked
 *
 * @return Returns true if the window is a Frame window, false otherwise
 */
fun isFrame(window: Window?): Boolean {
    return window is Frame
}

/**
 * Gets whether the window is a Dialog window.
 *
 * @param window The window to be checked
 *
 * @return Returns true if the window is a Dialog window, false otherwise
 */
internal fun isDialog(window: Window?): Boolean {
    return window is Dialog
}

/**
 * Selects the tree node in the given tree with the specified tree path.
 *
 * @param tree The tree for which the node is to be selected
 * @param parentNode The parent node for the tree node to be selected
 * @param path The tree path for the tree node to be selected
 *
 * @return Returns true if the tree node was selected, false otherwise
 */
internal fun selectNode(tree: JTree, parentNode: DefaultMutableTreeNode, path: TreePath): Boolean {
    for (i in 0 until parentNode.childCount) {
        val node = parentNode.getChildAt(i) as DefaultMutableTreeNode
        val treePath = TreePath(node.path)
        if (treePath.toString().equals(path.toString(), ignoreCase = true)) {
            tree.selectionPath = treePath
            return true
        }
        if (!selectNode(tree, node, path)) continue
        return true
    }
    return false
}

/**
 * Recursively loads components of the specified type in the given container into the specified list.
 *
 * @param container The container to be queried
 * @param type The type of the components to be loaded
 * @param components The list to be loaded with the components
 */
internal fun loadComponents(container: Container, type: Class<*>, components: MutableList<Component>) {
    for (component in container.components) {
        if (type.isAssignableFrom(component.javaClass)) {
            components.add(component)
            continue
        }
        if (component !is Container) continue
        loadComponents(component, type, components)
    }
}

/**
 * Recursively loads all components in the given container into the specified list.
 *
 * @param container The container to be queried
 * @param components The list to be loaded with the components
 */
internal fun loadAllComponents(container: Container, components: MutableList<Component?>) {
    for (component in container.components) {
        if (component is Container) {
            loadAllComponents(component, components)
        }
        components.add(component)
    }
}