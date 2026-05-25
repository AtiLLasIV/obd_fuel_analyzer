package com.diploma.fuelstats.presentation.stats

import android.content.Context
import androidx.core.content.ContextCompat
import com.diploma.fuelstats.R
import com.patrykandpatrick.vico.views.cartesian.CartesianChart
import com.patrykandpatrick.vico.views.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.views.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.views.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.views.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.views.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.views.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.views.common.Fill
import com.patrykandpatrick.vico.views.common.component.LineComponent
import com.patrykandpatrick.vico.views.common.component.TextComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StatsChartFactory {

    fun createConsumptionIntervalsChart(context: Context): CartesianChart {
        val columnColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)

        return CartesianChart(
            ColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    LineComponent(fill = Fill(columnColor), thicknessDp = 16f)
                )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.1f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { contextMeasuring, x, _ ->
                    val labels = contextMeasuring.model.extraStore
                        .getOrNull(StatsViewModel.ConsumptionIntervalLabelsKey)
                    labels?.getOrNull(x.toInt()) ?: "-"
                },
                titleComponent = axisLabel,
            )
        )
    }

    fun createInstantConsumptionChart(context: Context): CartesianChart {
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val axisColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(axisColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(axisColor), thicknessDp = 1f)

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val refuelLine = LineComponent(
            fill = Fill(ContextCompat.getColor(context, R.color.md_theme_secondary)),
            thicknessDp = 2f
        )

        return CartesianChart(
            LineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor))
                    )
                ),
                rangeProvider = CartesianLayerRangeProvider.fixed(
                    minY = 0.0,
                    maxY = 40.0,
                    )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.1f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { context, x, _ ->
                    val labels = context.model.extraStore
                        .getOrNull(StatsViewModel.InstantConsumptionLabelsKey)

                    val index = x.toInt()
                    if (labels == null || index !in labels.indices) {
                        return@CartesianValueFormatter "-"
                    }
                    labels[index]
                },
                titleComponent = axisLabel,
            ),
            decorations = listOf(
                VerticalLinesDecoration(
                    xs = { store -> store.getOrNull(StatsViewModel.RefuelMarkersXKey) ?: emptyList() },
                    line = refuelLine
                )
            ),
        )
    }

    fun createSpeedConsumptionChart(context: Context): CartesianChart {
        val columnColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)

        return CartesianChart(
            ColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    LineComponent(fill = Fill(columnColor), thicknessDp = 16f)
                )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.1f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { contextMeasuring, x, _ ->
                    val labels = contextMeasuring.model.extraStore
                        .getOrNull(StatsViewModel.SpeedConsumptionLabelsKey)
                    labels?.getOrNull(x.toInt()) ?: "-"
                },
                titleComponent = axisLabel,
            )
        )
    }

    fun createRpmConsumptionChart(context: Context): CartesianChart {
        val columnColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)

        return CartesianChart(
            ColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    LineComponent(fill = Fill(columnColor), thicknessDp = 16f)
                )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.1f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { contextMeasuring, x, _ ->
                    val labels = contextMeasuring.model.extraStore
                        .getOrNull(StatsViewModel.RpmConsumptionLabelsKey)
                    labels?.getOrNull(x.toInt()) ?: "-"
                },
                titleComponent = axisLabel,
            )
        )
    }

    fun createAmbientConsumptionChart(context: Context): CartesianChart {
        val columnColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)

        return CartesianChart(
            ColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    LineComponent(fill = Fill(columnColor), thicknessDp = 16f)
                )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.1f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { contextMeasuring, x, _ ->
                    val labels = contextMeasuring.model.extraStore
                        .getOrNull(StatsViewModel.AmbientConsumptionLabelsKey)
                    labels?.getOrNull(x.toInt()) ?: "-"
                },
                titleComponent = axisLabel,
            )
        )
    }

    fun createCoolantConsumptionChart(context: Context): CartesianChart {
        val columnColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)

        return CartesianChart(
            ColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    LineComponent(fill = Fill(columnColor), thicknessDp = 16f)
                )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.1f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { contextMeasuring, x, _ ->
                    val labels = contextMeasuring.model.extraStore
                        .getOrNull(StatsViewModel.CoolantConsumptionLabelsKey)
                    labels?.getOrNull(x.toInt()) ?: "-"
                },
                titleComponent = axisLabel,
            )
        )
    }

    fun createFuelLevelChart(context: Context): CartesianChart {
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val axisColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(axisColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(axisColor), thicknessDp = 1f)

        val refuelLine = LineComponent(
            fill = Fill(ContextCompat.getColor(context, R.color.md_theme_secondary)),
            thicknessDp = 2f
        )

        return CartesianChart(
            LineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor))
                    )
                )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.0f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { contextMeasuring, x, _ ->
                    val labels = contextMeasuring.model.extraStore
                        .getOrNull(StatsViewModel.FuelLevelLabelsKey)

                    val index = x.toInt()
                    if (labels == null || index !in labels.indices) {
                        "-"
                    } else {
                        labels[index]
                    }
                },
                titleComponent = axisLabel,
            ),
            decorations = listOf(
                VerticalLinesDecoration(
                    xs = { store -> store.getOrNull(StatsViewModel.RefuelMarkersXKey) ?: emptyList() },
                    line = refuelLine
                )
            ),
        )
    }

    fun createManualVsObdConsumptionChart(context: Context): CartesianChart {
        val manualColor = ContextCompat.getColor(context, R.color.md_theme_secondary_light)
        val obdColor = ContextCompat.getColor(context, R.color.md_theme_primary)
        val labelColor = ContextCompat.getColor(context, R.color.md_theme_onBackground)
        val lineColor = ContextCompat.getColor(context, R.color.md_theme_outline)

        val axisLabel = TextComponent(color = labelColor, textSizeSp = 11f)
        val axisLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)
        val gridLine = LineComponent(fill = Fill(lineColor), thicknessDp = 1f)

        return CartesianChart(
            ColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    LineComponent(fill = Fill(manualColor), thicknessDp = 14f),
                    LineComponent(fill = Fill(obdColor), thicknessDp = 14f),
                )
            ),
            startAxis = VerticalAxis.start(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = gridLine,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(Locale.getDefault(), "%.1f", value)
                },
                titleComponent = axisLabel,
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                tick = axisLine,
                tickLengthDp = 4f,
                guideline = null,
                valueFormatter = CartesianValueFormatter { contextMeasuring, x, _ ->
                    val labels = contextMeasuring.model.extraStore
                        .getOrNull(StatsViewModel.ManualVsObdLabelsKey)
                    labels?.getOrNull(x.toInt()) ?: "-"
                },
                titleComponent = axisLabel,
            )
        )
    }

}