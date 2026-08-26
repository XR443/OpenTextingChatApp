package org.github.raspopov.composes.images

import androidx.compose.ui.graphics.vector.ImageVector
import org.github.raspopov.composes.images.icons.Add
import org.github.raspopov.composes.images.icons.Alert
import org.github.raspopov.composes.images.icons.Burger
import org.github.raspopov.composes.images.icons.Clock
import org.github.raspopov.composes.images.icons.Copy
import org.github.raspopov.composes.images.icons.Edit
import org.github.raspopov.composes.images.icons.EyeClosed
import org.github.raspopov.composes.images.icons.EyeOpened
import org.github.raspopov.composes.images.icons.FileAdd
import org.github.raspopov.composes.images.icons.FileRemove
import org.github.raspopov.composes.images.icons.Info
import org.github.raspopov.composes.images.icons.LeftArrow
import org.github.raspopov.composes.images.icons.Lock
import org.github.raspopov.composes.images.icons.MailRead
import org.github.raspopov.composes.images.icons.MailUnread
import org.github.raspopov.composes.images.icons.MessageBubble
import org.github.raspopov.composes.images.icons.MoreHorizontal
import org.github.raspopov.composes.images.icons.MoreVertical
import org.github.raspopov.composes.images.icons.Person
import org.github.raspopov.composes.images.icons.PersonAdd
import org.github.raspopov.composes.images.icons.PersonRemove
import org.github.raspopov.composes.images.icons.PersonX
import org.github.raspopov.composes.images.icons.Search
import org.github.raspopov.composes.images.icons.Signal
import org.github.raspopov.composes.images.icons.Tick
import org.github.raspopov.composes.images.icons.Unlock
import org.github.raspopov.composes.images.icons.Upload
import org.github.raspopov.composes.images.icons.XCirce
import org.github.raspopov.composes.images.icons.XSquare
import kotlin.collections.List as ____KtList

public object Icons

private var __Default: ____KtList<ImageVector>? = null

public val Icons.Default: ____KtList<ImageVector>
  get() {
    if (__Default != null) {
      return __Default!!
    }
    __Default= listOf(Add, Alert, Burger, Clock, Copy, Edit, EyeClosed, EyeOpened, FileAdd,
        FileRemove, Info, LeftArrow, Lock, MailRead, MailUnread, MessageBubble, MoreHorizontal,
        MoreVertical, Person, PersonAdd, PersonRemove, PersonX, Search, Signal, Tick, Unlock,
        Upload, XCirce, XSquare)
    return __Default!!
  }
