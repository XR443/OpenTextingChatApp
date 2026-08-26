package org.github.raspopov.composes.images.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.github.raspopov.composes.images.Icons

public val Icons.Unlock: ImageVector
    get() {
        if (_unlock != null) {
            return _unlock!!
        }
        _unlock = Builder(name = "Unlock", defaultWidth = 800.0.dp, defaultHeight = 800.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(6.0f, 11.0f)
                lineTo(18.0f, 11.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 20.0f, 13.0f)
                lineTo(20.0f, 19.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 18.0f, 21.0f)
                lineTo(6.0f, 21.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 4.0f, 19.0f)
                lineTo(4.0f, 13.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 6.0f, 11.0f)
                close()
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(16.5f, 11.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.0f, -2.8f, -0.5f, -5.0f, -4.5f, -5.0f)
            }
        }
        .build()
        return _unlock!!
    }

private var _unlock: ImageVector? = null
