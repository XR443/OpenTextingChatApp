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

public val Icons.MailUnread: ImageVector
    get() {
        if (_mailUnread != null) {
            return _mailUnread!!
        }
        _mailUnread = Builder(name = "MailUnread", defaultWidth = 800.0.dp, defaultHeight =
                800.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(4.0f, 8.2f)
                lineToRelative(8.0f, 5.9f)
                lineToRelative(8.0f, -5.9f)
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(5.0f, 6.5f)
                lineTo(19.0f, 6.5f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 21.0f, 8.5f)
                lineTo(21.0f, 18.5f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 19.0f, 20.5f)
                lineTo(5.0f, 20.5f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 3.0f, 18.5f)
                lineTo(3.0f, 8.5f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 5.0f, 6.5f)
                close()
            }
        }
        .build()
        return _mailUnread!!
    }

private var _mailUnread: ImageVector? = null
