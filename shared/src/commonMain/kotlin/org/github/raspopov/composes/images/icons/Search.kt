package org.github.raspopov.composes.images.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.github.raspopov.composes.images.Icons

public val Icons.Search: ImageVector
    get() {
        if (_search != null) {
            return _search!!
        }
        _search = Builder(name = "Search", defaultWidth = 800.0.dp, defaultHeight = 800.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF0F0F0F)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(17.039f, 15.624f)
                curveTo(18.271f, 14.084f, 19.008f, 12.13f, 19.008f, 10.004f)
                curveTo(19.008f, 5.031f, 14.977f, 1.0f, 10.004f, 1.0f)
                curveTo(5.031f, 1.0f, 1.0f, 5.031f, 1.0f, 10.004f)
                curveTo(1.0f, 14.977f, 5.031f, 19.008f, 10.004f, 19.008f)
                curveTo(12.13f, 19.008f, 14.084f, 18.271f, 15.624f, 17.039f)
                lineTo(21.292f, 22.707f)
                curveTo(21.683f, 23.098f, 22.316f, 23.098f, 22.707f, 22.707f)
                curveTo(23.098f, 22.316f, 23.098f, 21.683f, 22.707f, 21.292f)
                lineTo(17.039f, 15.624f)
                close()
                moveTo(10.004f, 17.017f)
                curveTo(6.131f, 17.017f, 2.991f, 13.877f, 2.991f, 10.004f)
                curveTo(2.991f, 6.131f, 6.131f, 2.991f, 10.004f, 2.991f)
                curveTo(13.877f, 2.991f, 17.017f, 6.131f, 17.017f, 10.004f)
                curveTo(17.017f, 13.877f, 13.877f, 17.017f, 10.004f, 17.017f)
                close()
            }
        }
        .build()
        return _search!!
    }

private var _search: ImageVector? = null
