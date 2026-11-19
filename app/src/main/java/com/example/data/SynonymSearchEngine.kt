package com.example.data

import com.example.model.TweakItem

object SynonymSearchEngine {

    private val synonymGroups: List<Set<String>> = listOf(
        setOf("плавность", "герцы", "герцовка", "частота", "120", "90", "hz", "refresh", "smooth", "анимация", "анимации", "fps", "фпс", "скролл", "экран"),
        setOf("лаги", "тормоза", "фризы", "лагает", "тормозит", "ускорение", "быстро", "скорость", "мощность", "производительность", "cpu", "процессор", "разгон", "отклик", "lag", "slow", "fast", "boost"),
        setOf("батарея", "аккумулятор", "разряд", "разряжается", "заряд", "зарядка", "автономность", "энергия", "сон", "doze", "battery", "charge", "drain", "standby", "лимит", "80%", "85%"),
        setOf("озу", "память", "оперативка", "ram", "swap", "zram", "подкачка", "ram plus", "кэш", "мусор", "место", "хранилище", "очистка", "фон", "заморозка"),
        setOf("реклама", "баннеры", "блокировка", "dns", "adguard", "cloudflare", "adblock", "телеметрия", "слежка", "аналитика", "логи", "приватность", "privacy", "ads"),
        setOf("игры", "игра", "гейминг", "gaming", "gos", "тротлинг", "нагрев", "температура", "vulkan", "angle", "720p", "разрешение", "пинг", "ping", "джиттер", "fps"),
        setOf("звук", "громкость", "музыка", "наушники", "блютуз", "bluetooth", "tws", "кодек", "ldac", "aac", "aptx", "динамик", "басы", "audio", "sound", "dolby"),
        setOf("камера", "фото", "затвор", "щелчок", "camera", "shutter", "тихо"),
        setOf("сеть", "интернет", "wifi", "вайфай", "5g", "lte", "4g", "модем", "раздача", "хотспот", "связь", "бэнды", "vpn", "впн", "adb", "5555"),
        setOf("интерфейс", "дисплей", "экран", "цвета", "калибровка", "amoled", "амолед", "яркость", "ночь", "темная", "тема", "шторка", "часы", "секунды", "жесты", "dpi", "масштаб", "вырез", "блюр", "размытие"),
        setOf("деблоат", "удалить", "отключить", "bixby", "биксби", "knox", "нокс", "facebook", "meta", "мусор", "системные", "debloat"),
        setOf("уведомления", "пуши", "оповещения", "выгружает", "notifications", "push"),
    )

    fun search(query: String, tweaks: List<TweakItem>): List<Pair<TweakItem, List<String>>> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return emptyList()

        val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        val expandedPerToken = tokens.map { token ->
            val related = mutableSetOf(token)
            synonymGroups.forEach { group ->
                if (group.any { term -> term.contains(token) || token.contains(term) }) {
                    related.addAll(group)
                }
            }
            related
        }

        return tweaks.mapNotNull { tweak ->
            val searchable = buildString {
                append(tweak.titleRu.lowercase()).append(' ')
                append(tweak.titleEn.lowercase()).append(' ')
                append(tweak.descriptionRu.lowercase()).append(' ')
                append(tweak.descriptionEn.lowercase()).append(' ')
                append(tweak.whyHiddenRu.lowercase()).append(' ')
                append(tweak.synonyms.joinToString(" ").lowercase()).append(' ')
                append(tweak.enableCommand?.lowercase().orEmpty()).append(' ')
                append(tweak.actionCommand?.lowercase().orEmpty())
            }

            var score = 0
            val matchedReasons = mutableSetOf<String>()

            tokens.forEachIndexed { idx, rawToken ->
                if (tweak.titleRu.lowercase().contains(rawToken) || tweak.titleEn.lowercase().contains(rawToken)) {
                    score += 10
                    matchedReasons.add(rawToken)
                } else if (searchable.contains(rawToken)) {
                    score += 6
                    matchedReasons.add(rawToken)
                } else {
                    val synHit = expandedPerToken[idx].firstOrNull { syn ->
                        tweak.synonyms.any { it.equals(syn, ignoreCase = true) } || searchable.contains(syn)
                    }
                    if (synHit != null) {
                        score += 3
                        matchedReasons.add(synHit)
                    }
                }
            }

            if (score > 0) (tweak to matchedReasons.take(3)) to score else null
        }.sortedByDescending { it.second }.map { it.first }
    }

    val suggestedSynonymChipsRu = listOf(
        "плавность",
        "разряд",
        "лаги",
        "реклама",
        "игры",
        "звук",
        "память",
        "сеть",
        "камера",
        "деблоат",
    )

    val suggestedSynonymChipsEn = listOf(
        "smooth",
        "battery",
        "lag",
        "adblock",
        "gaming",
        "audio",
        "ram",
        "wifi",
        "camera",
        "debloat",
    )
}
