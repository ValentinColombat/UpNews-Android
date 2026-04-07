package com.valentincolombat.upnews.ui.components

import androidx.annotation.DrawableRes
import com.valentincolombat.upnews.R

@DrawableRes
fun companionDrawable(id: String): Int = when (id) {
    "mousse"                          -> R.drawable.mousse
    "cannelle"                        -> R.drawable.cannelle
    "givreetplume", "givre_et_plume"  -> R.drawable.givreetplume
    "brume"                           -> R.drawable.brume
    "flocon"                          -> R.drawable.flocon
    "vera"                            -> R.drawable.vera
    "jura"                            -> R.drawable.jura
    "caramel"                         -> R.drawable.caramel
    "ecorce"                          -> R.drawable.ecorce
    "luciole"                         -> R.drawable.luciole
    "olga"                            -> R.drawable.olga
    "luka"                            -> R.drawable.luka
    "nina"                            -> R.drawable.nina
    "seb"                             -> R.drawable.seb
    "mochi"                           -> R.drawable.mochi
    "seve"                            -> R.drawable.seve
    "jo"                              -> R.drawable.jo
    "pepite"                          -> R.drawable.pepite
    "noisette"                        -> R.drawable.noisette
    else                              -> R.drawable.mousse
}
