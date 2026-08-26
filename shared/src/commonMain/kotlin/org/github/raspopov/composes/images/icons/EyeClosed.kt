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

public val Icons.EyeClosed: ImageVector
    get() {
        if (_eyeClosed != null) {
            return _eyeClosed!!
        }
        _eyeClosed = Builder(name = "EyeClosed", defaultWidth = 800.0.dp, defaultHeight = 800.0.dp,
                viewportWidth = 64.0f, viewportHeight = 64.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(13.673f, 10.345f)
                lineToRelative(-3.097f, 3.096f)
                lineToRelative(39.853f, 39.854f)
                lineToRelative(3.097f, -3.097f)
                lineToRelative(-39.853f, -39.853f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(17.119f, 19.984f)
                lineToRelative(2.915f, 2.915f)
                curveToRelative(-3.191f, 2.717f, -5.732f, 6.099f, -7.374f, 9.058f)
                lineToRelative(-0.005f, 0.01f)
                curveToRelative(4.573f, 7.646f, 11.829f, 14.872f, 20.987f, 13.776f)
                curveToRelative(2.472f, -0.296f, 4.778f, -1.141f, 6.885f, -2.35f)
                lineToRelative(2.951f, 2.95f)
                curveToRelative(-4.107f, 2.636f, -8.815f, 4.032f, -13.916f, 3.342f)
                curveToRelative(-9.198f, -1.244f, -16.719f, -8.788f, -21.46f, -17.648f)
                curveToRelative(2.226f, -4.479f, 5.271f, -8.764f, 9.017f, -12.053f)
                close()
                moveTo(23.749f, 15.664f)
                curveToRelative(2.572f, -1.146f, 5.355f, -1.82f, 8.327f, -1.868f)
                curveToRelative(0.165f, -0.001f, 2.124f, 0.092f, 3.012f, 0.238f)
                curveToRelative(0.557f, 0.092f, 1.112f, 0.207f, 1.659f, 0.35f)
                curveToRelative(8.725f, 2.273f, 15.189f, 10.054f, 19.253f, 17.653f)
                curveToRelative(-1.705f, 3.443f, -3.938f, 6.398f, -6.601f, 9.277f)
                lineToRelative(-2.827f, -2.827f)
                curveToRelative(1.967f, -2.12f, 3.622f, -4.161f, 4.885f, -6.45f)
                curveToRelative(0.0f, 0.0f, -1.285f, -2.361f, -2.248f, -3.643f)
                curveToRelative(-0.619f, -0.824f, -1.27f, -1.624f, -1.954f, -2.395f)
                curveToRelative(-0.54f, -0.608f, -2.637f, -2.673f, -3.136f, -3.103f)
                curveToRelative(-3.348f, -2.879f, -7.279f, -5.138f, -11.994f, -5.1f)
                curveToRelative(-1.826f, 0.029f, -3.582f, 0.389f, -5.249f, 0.995f)
                lineToRelative(-3.127f, -3.127f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(25.054f, 27.92f)
                lineToRelative(2.399f, 2.398f)
                curveToRelative(-0.157f, 0.477f, -0.243f, 0.987f, -0.243f, 1.516f)
                curveToRelative(0.0f, 2.672f, 2.169f, 4.841f, 4.841f, 4.841f)
                curveToRelative(0.529f, 0.0f, 1.039f, -0.085f, 1.516f, -0.243f)
                lineToRelative(2.399f, 2.399f)
                curveToRelative(-1.158f, 0.65f, -2.494f, 1.02f, -3.915f, 1.02f)
                curveToRelative(-4.425f, 0.0f, -8.017f, -3.592f, -8.017f, -8.017f)
                curveToRelative(0.0f, -1.421f, 0.371f, -2.756f, 1.02f, -3.914f)
                close()
                moveTo(31.903f, 23.819f)
                curveToRelative(0.049f, -0.001f, 0.099f, -0.002f, 0.148f, -0.002f)
                curveToRelative(4.425f, 0.0f, 8.017f, 3.593f, 8.017f, 8.017f)
                curveToRelative(0.0f, 0.05f, 0.0f, 0.099f, -0.001f, 0.148f)
                lineToRelative(-8.164f, -8.163f)
                close()
            }
        }
        .build()
        return _eyeClosed!!
    }

private var _eyeClosed: ImageVector? = null
