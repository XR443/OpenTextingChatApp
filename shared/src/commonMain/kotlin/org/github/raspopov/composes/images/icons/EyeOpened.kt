package org.github.raspopov.composes.images.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.github.raspopov.composes.images.Icons

public val Icons.EyeOpened: ImageVector
    get() {
        if (_eyeOpened != null) {
            return _eyeOpened!!
        }
        _eyeOpened = Builder(name = "EyeOpened", defaultWidth = 800.0.dp, defaultHeight = 800.0.dp,
                viewportWidth = 64.0f, viewportHeight = 64.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(32.513f, 13.926f)
                curveToRelative(10.574f, 0.15f, 19.141f, 9.894f, 23.487f, 18.074f)
                curveToRelative(0.0f, 0.0f, -1.422f, 2.892f, -2.856f, 4.895f)
                curveToRelative(-0.694f, 0.969f, -1.424f, 1.913f, -2.191f, 2.826f)
                curveToRelative(-0.547f, 0.65f, -1.112f, 1.283f, -1.698f, 1.898f)
                curveToRelative(-5.237f, 5.5f, -12.758f, 9.603f, -20.7f, 8.01f)
                curveToRelative(-8.823f, -1.77f, -15.732f, -9.498f, -20.058f, -17.629f)
                curveToRelative(0.0f, 0.0f, 1.248f, -2.964f, 2.69f, -4.964f)
                curveToRelative(0.646f, -0.897f, 1.324f, -1.77f, 2.034f, -2.617f)
                curveToRelative(0.544f, -0.649f, 1.108f, -1.282f, 1.691f, -1.897f)
                curveToRelative(4.627f, -4.876f, 10.564f, -8.63f, 17.601f, -8.596f)
                close()
                moveTo(32.476f, 17.926f)
                curveToRelative(-5.89f, -0.022f, -10.788f, 3.267f, -14.663f, 7.35f)
                curveToRelative(-0.527f, 0.555f, -1.035f, 1.127f, -1.527f, 1.713f)
                curveToRelative(-0.647f, 0.772f, -1.265f, 1.569f, -1.854f, 2.386f)
                curveToRelative(-0.544f, 0.755f, -1.057f, 1.805f, -1.451f, 2.59f)
                curveToRelative(3.773f, 6.468f, 9.286f, 12.323f, 16.361f, 13.742f)
                curveToRelative(6.563f, 1.317f, 12.688f, -2.301f, 17.016f, -6.846f)
                curveToRelative(0.529f, -0.555f, 1.04f, -1.128f, 1.534f, -1.715f)
                curveToRelative(0.7f, -0.833f, 1.366f, -1.694f, 1.999f, -2.579f)
                curveToRelative(0.557f, -0.778f, 1.144f, -1.767f, 1.588f, -2.567f)
                curveToRelative(-3.943f, -6.657f, -10.651f, -13.944f, -19.003f, -14.074f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(32.158f, 23.948f)
                curveToRelative(4.425f, 0.0f, 8.018f, 3.593f, 8.018f, 8.017f)
                curveToRelative(0.0f, 4.425f, -3.593f, 8.017f, -8.018f, 8.017f)
                curveToRelative(-4.424f, 0.0f, -8.017f, -3.592f, -8.017f, -8.017f)
                curveToRelative(0.0f, -4.424f, 3.593f, -8.017f, 8.017f, -8.017f)
                close()
                moveTo(32.158f, 27.957f)
                curveToRelative(2.213f, 0.0f, 4.009f, 1.796f, 4.009f, 4.008f)
                curveToRelative(0.0f, 2.213f, -1.796f, 4.009f, -4.009f, 4.009f)
                curveToRelative(-2.212f, 0.0f, -4.008f, -1.796f, -4.008f, -4.009f)
                curveToRelative(0.0f, -2.212f, 1.796f, -4.008f, 4.008f, -4.008f)
                close()
            }
        }
        .build()
        return _eyeOpened!!
    }

private var _eyeOpened: ImageVector? = null
