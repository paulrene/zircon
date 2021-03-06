package org.codetome.zircon.internal.behavior.impl

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.TextCharacter
import org.codetome.zircon.api.behavior.Boundable
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.internal.behavior.InternalLayerable
import org.codetome.zircon.internal.behavior.Dirtiable
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue

class DefaultLayerable(size: Size,
                       boundable: Boundable = DefaultBoundable(size),
                       dirtiable: Dirtiable = DefaultDirtiable())
    : InternalLayerable, Boundable by boundable, Dirtiable by dirtiable {

    private val layers: BlockingDeque<Layer> = LinkedBlockingDeque()

    override fun pushLayer(layer: Layer) {
        layers.add(layer)
        markLayerPositionsDirty(layer)
    }

    // TODO: regression test this!
    override fun popLayer() = Optional.ofNullable(layers.pollLast()).also {
        it.map { markLayerPositionsDirty(it) }
    }

    override fun removeLayer(layer: Layer) {
        layers.remove(layer)
        markLayerPositionsDirty(layer)
    }

    override fun getLayers() = layers.toList()

    override fun drainLayers() = mutableListOf<Layer>().also {
        layers.drainTo(it)
        it.forEach {
            markLayerPositionsDirty(it)
        }
    }

    override fun fetchOverlayZIntersection(absolutePosition: Position): List<TextCharacter> {
        return fetchZIntersectionFor(layers, absolutePosition)
    }

    private fun fetchZIntersectionFor(queue: Queue<Layer>, position: Position): List<TextCharacter> {
        return queue.filter { layer ->
            layer.containsPosition(position)
        }.map { layer ->
            layer.getCharacterAt(position).get()
        }
    }

    private fun markLayerPositionsDirty(layer: Layer) {
        layer.fetchPositions().forEach {
            setPositionDirty(it)
        }
    }
}