package com.example.model

enum class AppCategory(val labelKey: String, val displayName: String) {
  ALL("category_all", "Todos"),
  SOCIAL("category_social", "Social"),
  TOOLS("category_tools", "Ferramentas"),
  MEDIA("category_media", "Mídia"),
  GAMES("category_games", "Jogos"),
  PRODUCTIVITY("category_productivity", "Produtividade"),
  SHOPPING("category_shopping", "Compras"),
  OTHER("category_other", "Outros");

  companion object {
    fun fromName(name: String?): AppCategory {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: ALL
    }
  }
}
