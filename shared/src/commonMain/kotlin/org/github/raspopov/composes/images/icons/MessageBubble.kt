package org.github.raspopov.composes.images.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.github.raspopov.composes.images.Icons

public val Icons.MessageBubble: ImageVector
    get() {
        if (_messageBubble != null) {
            return _messageBubble!!
        }
        _messageBubble = Builder(name = "MessageBubble", defaultWidth = 800.0.dp, defaultHeight =
                800.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(7.7f, 18.3f)
                horizontalLineTo(19.4f)
                arcToRelative(2.1f, 2.1f, 0.0f, false, false, 2.1f, -2.1f)
                verticalLineTo(4.6f)
                arcToRelative(2.1f, 2.1f, 0.0f, false, false, -2.1f, -2.1f)
                horizontalLineTo(4.6f)
                arcTo(2.1f, 2.1f, 0.0f, false, false, 2.5f, 4.6f)
                verticalLineTo(21.5f)
                close()
            }
        }
        .build()
        return _messageBubble!!
    }

private var _messageBubble: ImageVector? = null
