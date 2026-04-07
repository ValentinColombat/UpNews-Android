package com.valentincolombat.upnews.data.model

data class CompanionInfo(val id: String, val name: String, val unlockLevel: Int)

object CompanionData {

    val all = listOf(
        CompanionInfo("mousse",        "Mousse",          1),
        CompanionInfo("cannelle",      "Cannelle",         1),
        CompanionInfo("givreetplume",  "Givre et Plume",   1),
        CompanionInfo("brume",         "Brume",            2),
        CompanionInfo("flocon",        "Flocon",           2),
        CompanionInfo("vera",          "Vera",             3),
        CompanionInfo("jura",          "Jura",             4),
        CompanionInfo("caramel",       "Caramel",          5),
        CompanionInfo("ecorce",        "Écorce",           5),
        CompanionInfo("luciole",       "Luciole",          5),
        CompanionInfo("olga",          "Olga",             6),
        CompanionInfo("luka",          "Luka",             7),
        CompanionInfo("nina",          "Nina",             8),
        CompanionInfo("seb",           "Seb",              9),
        CompanionInfo("mochi",         "Mochi",            10),
        CompanionInfo("seve",          "Sève",             10),
        CompanionInfo("jo",            "Jo",               11),
        CompanionInfo("pepite",        "Pépite",           15),
        CompanionInfo("noisette",      "Noisette",         20)
    )

    // Groupés par niveau de déblocage (niveaux > 1 uniquement, pour les notifications d'unlock)
    val byUnlockLevel: Map<Int, List<CompanionInfo>> = all
        .filter { it.unlockLevel > 1 }
        .groupBy { it.unlockLevel }
}
