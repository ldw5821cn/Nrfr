# realme UI 16：解除 Shizuku ADB 权限限制

在部分搭载 Android 16 的 realme UI / ColorOS 设备上，旧版系统中的“禁用权限监控”被改名并隐藏。若 Shizuku 显示“制造商限制了 adb 权限”，或应用已获得 Shizuku 授权但仍无法连接服务，可按以下步骤处理。

## 操作步骤

1. 打开 **设置 → 其他设置 → 语言与地区**，临时将系统语言切换为 **English (United States)**。
2. 进入 **Developer options**，搜索或向下查找 **Disable system optimization**。
3. 打开该开关，确认系统警告后重启手机。
4. 重启后重新启动 Shizuku，再打开 Nrfr 测试。

在新版 ColorOS / realme UI 中，该开关通常只会在英文界面显示。已有 Android 16 用户确认它对应旧版系统中的“禁用权限监控”。详见 [Shizuku issue #2158](https://github.com/RikkaApps/Shizuku/issues/2158)。

Shizuku 官方也说明，厂商系统可能限制 ADB 权限；Shizuku 页面显示的“制造商限制了 adb 权限”即属于此类问题。参见 [Shizuku README](https://github.com/RikkaApps/Shizuku)。

## 说明

通过 USB 启动 Shizuku 可以绕过无线调试配对流程，但不会解除厂商对 ADB 权限的限制。先启用 **Disable system optimization**，再重启并重新启动 Shizuku，才是让 Nrfr、App Ops 等应用获得可用 Shizuku 权限的关键。
