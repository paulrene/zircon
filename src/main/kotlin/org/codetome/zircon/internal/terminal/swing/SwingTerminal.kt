package org.codetome.zircon.internal.terminal.swing

import org.codetome.zircon.api.Size
import org.codetome.zircon.api.font.Font
import org.codetome.zircon.api.terminal.config.DeviceConfiguration
import org.codetome.zircon.internal.terminal.virtual.VirtualTerminal
import java.awt.*
import java.awt.event.HierarchyEvent
import java.awt.event.MouseEvent
import java.awt.image.BufferStrategy
import java.awt.image.BufferedImage
import javax.swing.SwingUtilities

/**
 * Concrete implementation of [Java2DTerminalImplementation] that adapts it to Swing
 * and wraps a [Canvas].
 */
class SwingTerminal(
        private val canvas: Canvas,
        val font: Font<BufferedImage>,
        initialSize: Size,
        deviceConfiguration: DeviceConfiguration)

    : Java2DTerminalImplementation(
        deviceConfiguration = deviceConfiguration,
        font = font,
        terminal = VirtualTerminal(
                initialSize = initialSize)) {

    private var firstDraw = true

    init {
        //Prevent us from shrinking beyond one character
        canvas.minimumSize = Dimension(font.getWidth(), font.getHeight())
        canvas.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet<AWTKeyStroke>())
        canvas.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, emptySet<AWTKeyStroke>())
        canvas.addKeyListener(TerminalKeyListener(
                deviceConfiguration = deviceConfiguration))
        val listener = object : TerminalMouseListener(
                deviceConfiguration = deviceConfiguration,
                fontWidth = getFontWidth(),
                fontHeight = getFontHeight()) {
            override fun mouseClicked(e: MouseEvent) {
                super.mouseClicked(e)
                this@SwingTerminal.canvas.requestFocusInWindow()
            }
        }
        canvas.addMouseListener(listener)
        canvas.addMouseMotionListener(listener)
        canvas.addMouseWheelListener(listener)
        canvas.addHierarchyListener { e ->
            if (e.changeFlags == HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) {
                if (e.changed.isDisplayable) {
                    onCreated()
                } else {
                    onDestroyed()
                }
            }
        }
    }

    override fun getFontHeight() = font.getHeight()

    override fun getFontWidth() = font.getWidth()

    override fun getHeight() = canvas.height

    override fun getWidth() = canvas.width

    override fun draw() {
        val bs = canvas.bufferStrategy
        val gc: Graphics2D
        try {
            gc = bs.drawGraphics as Graphics2D
        } catch (e: NullPointerException) {
            // buffer strategy might not be initialized yet
            draw()
            return
        }
        if (SwingUtilities.isEventDispatchThread()) {
            drawAndDispose(bs, gc)
        } else {
            SwingUtilities.invokeLater {
                drawAndDispose(bs, gc)
            }
        }
    }

    private fun drawAndDispose(bs: BufferStrategy, gc: Graphics2D) {
        if (firstDraw) {
            firstDraw = false
            gc.color = Color.BLACK
            gc.fillRect(0, 0, getWidth(), getHeight())
            bs.show()
        }
        draw(gc)
        gc.dispose()
        bs.show()
    }

}
