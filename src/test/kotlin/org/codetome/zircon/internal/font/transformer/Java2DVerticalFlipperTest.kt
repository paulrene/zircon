package org.codetome.zircon.internal.font.transformer

import org.codetome.zircon.api.Modifiers
import org.codetome.zircon.api.builder.TextCharacterBuilder
import org.junit.Before
import org.junit.Test
import java.awt.image.BufferedImage

class Java2DVerticalFlipperTest {

    lateinit var target: Java2DVerticalFlipper

    @Before
    fun setUp() {
        target = Java2DVerticalFlipper()
    }

    @Test
    fun shouldProperlyRun() {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)
        target.transform(image, CHAR)

        // TODO: check vertical flip?
    }

    companion object {
        val WIDTH = 10
        val HEIGHT = 10
        val CHAR = TextCharacterBuilder.newBuilder()
                .modifier(Modifiers.VERTICAL_FLIP)
                .build()
    }
}