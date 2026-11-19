package com.example.terminal

data class TerminalPackage(
    val id: String,
    val name: String,
    val version: String,
    val category: String,
    val sizeLabel: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val binaryCommands: List<String>,
)

object TerminalPackageCatalog {

    val availablePackages: List<TerminalPackage> = listOf(
        TerminalPackage(
            id = "fastfetch",
            name = "fastfetch",
            version = "2.31.0-arm64",
            category = "system",
            sizeLabel = "1.4 MB",
            descriptionRu = "Сверхбыстрый вывод информации о системе, SoC, One UI, ядре и ОЗУ с ASCII-логотипом.",
            descriptionEn = "Blazing-fast system info fetcher displaying Galaxy A SoC, One UI, kernel, and RAM with ASCII art.",
            binaryCommands = listOf("fastfetch"),
        ),
        TerminalPackage(
            id = "neofetch",
            name = "neofetch",
            version = "7.1.0",
            category = "system",
            sizeLabel = "320 KB",
            descriptionRu = "Классический CLI-скрипт отображения конфигурации Android и терминала.",
            descriptionEn = "Classic CLI system information tool tailored for Android & One UI.",
            binaryCommands = listOf("neofetch"),
        ),
        TerminalPackage(
            id = "htop",
            name = "htop",
            version = "3.3.0",
            category = "monitor",
            sizeLabel = "680 KB",
            descriptionRu = "Интерактивный монитор процессов CPU, нагрузки ядер, ОЗУ и ZRAM.",
            descriptionEn = "Process, CPU core load, RAM and ZRAM live monitor.",
            binaryCommands = listOf("htop"),
        ),
        TerminalPackage(
            id = "curl",
            name = "curl",
            version = "8.11.0",
            category = "network",
            sizeLabel = "1.1 MB",
            descriptionRu = "Утилита для HTTP/HTTPS-запросов, проверки заголовков и API прямо из консоли.",
            descriptionEn = "HTTP/HTTPS transfer and API testing utility.",
            binaryCommands = listOf("curl"),
        ),
        TerminalPackage(
            id = "wget",
            name = "wget",
            version = "1.24.5",
            category = "network",
            sizeLabel = "890 KB",
            descriptionRu = "Загрузчик файлов по HTTP/HTTPS в постоянную папку downloads/.",
            descriptionEn = "Network file downloader saving directly to persistent downloads/.",
            binaryCommands = listOf("wget"),
        ),
        TerminalPackage(
            id = "git",
            name = "git",
            version = "2.47.1",
            category = "dev",
            sizeLabel = "6.2 MB",
            descriptionRu = "Клиент GitHub/Git для инспекции и скачивания репозиториев (git clone).",
            descriptionEn = "GitHub/Git repository fetcher and release inspector (git clone).",
            binaryCommands = listOf("git"),
        ),
        TerminalPackage(
            id = "gh-install",
            name = "gh-install",
            version = "1.8.0",
            category = "installer",
            sizeLabel = "540 KB",
            descriptionRu = "CLI-инсталлятор APK и релизов с GitHub по имени owner/repo.",
            descriptionEn = "Direct GitHub release APK installer CLI by owner/repo.",
            binaryCommands = listOf("gh-install"),
        ),
        TerminalPackage(
            id = "jq",
            name = "jq",
            version = "1.7.1",
            category = "utils",
            sizeLabel = "410 KB",
            descriptionRu = "Парсер и форматтер JSON-данных в терминале.",
            descriptionEn = "Lightweight command-line JSON processor.",
            binaryCommands = listOf("jq"),
        ),
        TerminalPackage(
            id = "tree",
            name = "tree",
            version = "2.1.3",
            category = "utils",
            sizeLabel = "180 KB",
            descriptionRu = "Древовидный вывод структуры каталогов и файлов.",
            descriptionEn = "Recursive directory tree viewer.",
            binaryCommands = listOf("tree"),
        ),
        TerminalPackage(
            id = "python-mini",
            name = "python-mini",
            version = "3.12-lite",
            category = "dev",
            sizeLabel = "4.8 MB",
            descriptionRu = "Базовая среда Python для быстрых вычислений, выражений и скриптов.",
            descriptionEn = "Lightweight Python expression & script runner.",
            binaryCommands = listOf("python", "python3"),
        ),
        TerminalPackage(
            id = "samsung-csc-tools",
            name = "samsung-csc-tools",
            version = "2.4.0",
            category = "samsung",
            sizeLabel = "420 KB",
            descriptionRu = "Набор утилит проверки One UI, CSC, Knox, батареи и параметров модема Samsung.",
            descriptionEn = "Samsung One UI, CSC, Knox, battery cycles & modem inspection toolkit.",
            binaryCommands = listOf("saminfo", "csc-check"),
        ),
        TerminalPackage(
            id = "net-probe",
            name = "net-probe",
            version = "1.3.2",
            category = "network",
            sizeLabel = "310 KB",
            descriptionRu = "Диагностика DNS, IP-интерфейсов и проверка доступности хостов.",
            descriptionEn = "Network interface, DNS, and host reachability diagnostics.",
            binaryCommands = listOf("net-probe", "dns-check"),
        ),
        TerminalPackage(
            id = "a-series-debloater",
            name = "a-series-debloater",
            version = "3.0.1",
            category = "samsung",
            sizeLabel = "290 KB",
            descriptionRu = "CLI-аудитор лишних системных пакетов Samsung Galaxy A через Shizuku.",
            descriptionEn = "Samsung Galaxy A bloatware package auditor via Shizuku.",
            binaryCommands = listOf("debloat-audit"),
        ),
    )

    fun findPackage(nameOrCmd: String): TerminalPackage? {
        val clean = nameOrCmd.trim().lowercase()
        return availablePackages.firstOrNull { pkg ->
            pkg.id.equals(clean, true) ||
                pkg.name.equals(clean, true) ||
                pkg.binaryCommands.any { it.equals(clean, true) }
        }
    }
}
