package org.codetome.zircon.internal.graphics

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.TextCharacter
import org.codetome.zircon.api.behavior.Boundable
import org.codetome.zircon.api.behavior.DrawSurface
import org.codetome.zircon.api.behavior.Drawable
import org.codetome.zircon.api.builder.TextCharacterBuilder
import org.codetome.zircon.api.graphics.StyleSet
import org.codetome.zircon.api.graphics.TextImage
import org.codetome.zircon.internal.behavior.impl.DefaultBoundable
import java.util.*

/**
 * Simple implementation of [TextImage] that keeps the content as a two-dimensional [TextCharacter] array.
 * Copy operations between two [DefaultTextImage] classes are semi-optimized by using [System.arraycopy]
 * instead of iterating over each character and copying them over one by one.
 */
class DefaultTextImage(size: Size,
                       toCopy: Array<Array<TextCharacter>>,
                       filler: TextCharacter,
                       boundable: Boundable = DefaultBoundable(
                               size = size),
                       styleSet: StyleSet = DefaultStyleSet())
    : TextImage, Boundable by boundable, StyleSet by styleSet {

    private val buffer = (0 until getBoundableSize().rows).map {
        (0 until getBoundableSize().columns).map { filler }.toTypedArray()
    }.toTypedArray()

    init {
        getBoundableSize().fetchPositions().forEach { (col, row) ->
            if (row < toCopy.size && col < toCopy[row].size) {
                buffer[row][col] = toCopy[row][col]
            } else {
                buffer[row][col] = filler
            }
        }
    }

    override fun toString(): String {
        return (0 until getBoundableSize().rows).map { row ->
            (0 until getBoundableSize().columns).map { col ->
                buffer[row][col].getCharacter()
            }.joinToString("").plus("\n")
        }.joinToString("")
    }

    override fun draw(drawable: Drawable, offset: Position) {
        drawable.drawOnto(this, offset)
    }

    override fun putText(text: String, position: Position) {
        text.forEachIndexed { col, char ->
            setCharacterAt(position.withRelativeColumn(col), TextCharacterBuilder
                    .newBuilder()
                    .styleSet(toStyleSet())
                    .character(char)
                    .build())
        }
    }

    @Synchronized
    override fun applyColorsFromStyle(styleSet: StyleSet, offset: Position, size: Size) {
        val bg = styleSet.getBackgroundColor()
        val fg = styleSet.getForegroundColor()
        setStyleFrom(styleSet.toStyleSet().apply { clearModifiers() })
        size.fetchPositions().forEach { pos ->
            pos.plus(offset).let { fixedPos ->
                getCharacterAt(fixedPos).map { char ->
                    setCharacterAt(fixedPos, char.withForegroundColor(fg).withBackgroundColor(bg))
                }
            }
        }
    }

    override fun resize(newSize: Size, filler: TextCharacter): DefaultTextImage {
        if (newSize.rows == buffer.size && (buffer.isEmpty() || newSize.columns == buffer[0].size)) {
            return this
        }
        return DefaultTextImage(newSize, buffer, filler)
    }

    override fun setCharacterAt(position: Position, character: Char)
            = setCharacterAt(position, TextCharacterBuilder.newBuilder()
            .character(character)
            .styleSet(toStyleSet())
            .build())

    override fun setCharacterAt(position: Position, character: TextCharacter) = position.let { (column, row) ->
        if (positionIsOutOfBounds(column, row)) {
            false
        } else {
            val oldChar = buffer[row][column]
            return if (oldChar == character) {
                false //
            } else {
                buffer[row][column] = character
                true
            }
        }
    }

    override fun getCharacterAt(position: Position) = position.let { (column, row) ->
        if (positionIsOutOfBounds(column, row)) {
            Optional.empty()
        } else {
            Optional.of(buffer[row][column])
        }
    }

    override fun drawOnto(surface: DrawSurface, offset: Position) {
        val startRowIdx = 0
        val rows: Int = getBoundableSize().rows
        val startColumnIdx = 0
        val columns: Int = getBoundableSize().columns
        val destRowOffset = offset.row
        val destColumnOffset = offset.column
        // TODO: some other operation involving Cells?
        (startRowIdx until startRowIdx + rows).forEach { y ->
            (startColumnIdx until startColumnIdx + columns).forEach { x ->
                if (buffer[y][x] != TextCharacterBuilder.EMPTY) {
                    surface.setCharacterAt(
                            Position.of(x - startColumnIdx + destColumnOffset,
                                    y - startRowIdx + destRowOffset),
                            buffer[y][x])
                }
            }
        }
    }

    private fun positionIsOutOfBounds(column: Int, row: Int)
            = column < 0 || row < 0 || row >= buffer.size || column >= buffer[0].size

}