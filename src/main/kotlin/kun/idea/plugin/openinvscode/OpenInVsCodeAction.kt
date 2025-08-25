package kun.idea.plugin.openinvscode

import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile


class OpenInVsCodeAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(actionEvent: AnActionEvent) {
        super.update(actionEvent)
        val presentation = actionEvent.presentation
        val dataContext = actionEvent.dataContext
        val vFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)
        presentation.isVisible = vFile != null && vFile.isInLocalFileSystem && vFile.isFile
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val dataContext = actionEvent.dataContext
        val vFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) as VirtualFile
        openInVsCode(vFile)
    }

    private fun openInVsCode(vFile: VirtualFile) {
        runCatching {
            var codeExecutable = "code"
            var virtualFilePath: String = vFile.path
            if (SystemInfo.isMac) {
                codeExecutable = "/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code"
            } else if (SystemInfo.isWindows) {
                codeExecutable = "code.cmd"
                virtualFilePath = virtualFilePath.replace("/", "\\")
            }
            val process = ProcessBuilder(*arrayOfNulls<String>(0))
                .command(codeExecutable, "-g", virtualFilePath)
                .inheritIO()
                .start()
            Thread(Runnable {
                runCatching {
                    process.waitFor()
                }
            })
        }.onFailure {
            val notification = Notification("OpenInVsCode", "openInVsCode error", it.localizedMessage, NotificationType.ERROR)
            Notifications.Bus.notify(notification)
        }
    }

}