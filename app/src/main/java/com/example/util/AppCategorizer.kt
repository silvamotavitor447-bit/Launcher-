package com.example.util

import android.content.pm.ApplicationInfo
import android.os.Build
import com.example.model.AppCategory

object AppCategorizer {

  fun categorize(
      appInfo: ApplicationInfo?,
      packageName: String,
      label: String,
  ): AppCategory {
    val pkg = packageName.lowercase()
    val name = label.lowercase()

    // 1. Check system ApplicationInfo.category if API >= 26
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
      when (appInfo.category) {
        ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
        ApplicationInfo.CATEGORY_AUDIO,
        ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
        ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
        ApplicationInfo.CATEGORY_NEWS -> return AppCategory.MEDIA
        ApplicationInfo.CATEGORY_MAPS -> return AppCategory.TOOLS
      }
    }

    // Check game flag on ApplicationInfo
    if (appInfo != null) {
      val isGame = (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
      if (isGame) return AppCategory.GAMES
    }

    // 2. Intelligent Package & Label heuristics

    // Social & Communication
    if (matchesAny(pkg, name,
            "whatsapp", "telegram", "discord", "instagram", "facebook", "twitter",
            "x.corp", "tiktok", "snapchat", "messenger", "reddit", "linkedin",
            "wechat", "signal", "threads", "social", "chat", "direct", "contatos", "contacts")) {
      return AppCategory.SOCIAL
    }

    // Media & Entertainment
    if (matchesAny(pkg, name,
            "youtube", "spotify", "netflix", "primevideo", "disney", "twitch",
            "deezer", "sound", "music", "musica", "video", "player", "camera", "câmera",
            "gallery", "galeria", "photos", "fotos", "podcast", "vlc", "hbo", "max",
            "tidal", "streaming", "radio", "rádio")) {
      return AppCategory.MEDIA
    }

    // Productivity & Office
    if (matchesAny(pkg, name,
            "docs", "sheets", "slides", "excel", "word", "powerpoint", "notion",
            "evernote", "keep", "trello", "slack", "teams", "zoom", "meet", "drive",
            "dropbox", "onedrive", "notes", "notas", "calendar", "calendário", "agenda",
            "office", "pdf", "reader", "scanner", "workspace", "gmail", "email", "mail")) {
      return AppCategory.PRODUCTIVITY
    }

    // Games
    if (matchesAny(pkg, name,
            "game", "jogo", "craft", "clash", "candy", "subway", "pubg", "freefire",
            "roblox", "minecraft", "chess", "xadrez", "puzzle", "arcade", "simulator",
            "racing", "rpg", "play.games")) {
      return AppCategory.GAMES
    }

    // Shopping & Finance
    if (matchesAny(pkg, name,
            "amazon", "shopee", "mercadolivre", "mercadolibre", "mercado", "mercadopago", "aliexpress", "magalu",
            "shein", "nubank", "inter", "itau", "bradesco", "santander", "caixa",
            "banco", "bank", "wallet", "carteira", "pay", "pagseguro", "picpay",
            "ifood", "uber", "rappi", "shopping", "loja", "store")) {
      return AppCategory.SHOPPING
    }

    // Tools & Utilities
    if (matchesAny(pkg, name,
            "calculator", "calculadora", "clock", "relógio", "relogio", "alarm", "alarme",
            "settings", "configurações", "config", "files", "arquivos", "browser", "chrome",
            "firefox", "edge", "opera", "navegador", "compass", "bússola", "flashlight",
            "lanterna", "weather", "clima", "tempo", "maps", "mapas", "waze", "cleaner",
            "terminal", "launcher", "dialer", "telefone", "phone", "recorder", "gravador",
            "system", "tool", "util")) {
      return AppCategory.TOOLS
    }

    return AppCategory.OTHER
  }

  private fun matchesAny(pkg: String, name: String, vararg keywords: String): Boolean {
    for (kw in keywords) {
      if (pkg.contains(kw) || name.contains(kw)) {
        return true
      }
    }
    return false
  }
}
