package com.diploma.fuelstats.presentation.stats

import com.patrykandpatrick.vico.views.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.views.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.views.common.component.LineComponent
import com.patrykandpatrick.vico.views.common.data.ExtraStore

class VerticalLinesDecoration(
    private val xs: (ExtraStore) -> List<Double>,
    private val line: LineComponent,
) : Decoration {

    override fun drawOverLayers(context: CartesianDrawingContext) {
        with(context) {
            val xList = xs(model.extraStore)

            for (x in xList) {
                if (x < ranges.minX || x > ranges.maxX) continue

                val canvasX =
                    layerBounds.left +
                            layerDimensions.startPadding +
                            (((x - ranges.minX) / ranges.xStep).toFloat() * layerDimensions.xSpacing) -
                            scroll

                if (canvasX !in layerBounds.left..layerBounds.right) continue

                line.drawVertical(
                    context = context,
                    x = canvasX,
                    top = layerBounds.top,
                    bottom = layerBounds.bottom,
                )
            }
        }
    }
}