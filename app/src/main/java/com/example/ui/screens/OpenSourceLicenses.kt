package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.QuickKartThemeTokens

/*
 * Open-source licences shown in Account. Keep in sync with gradle/libs.versions.toml
 * and app/src/main/res/font when dependencies or fonts change.
 */

private data class Notice(
    val name: String,
    val copyright: String,
    val licence: String,
    val url: String
)

private val FontNotices = listOf(
    Notice("Outfit (font)", "Copyright 2021 The Outfit Project Authors", "SIL Open Font License 1.1", "github.com/Outfitio/Outfit-Fonts"),
    Notice("Plus Jakarta Sans (font)", "Copyright 2020 The Plus Jakarta Sans Project Authors", "SIL Open Font License 1.1", "github.com/tokotype/PlusJakartaSans"),
    Notice("JetBrains Mono (font)", "Copyright 2020 The JetBrains Mono Project Authors", "SIL Open Font License 1.1", "github.com/JetBrains/JetBrainsMono")
)

private val LibraryNotices = listOf(
    Notice("Jetpack Compose, AndroidX (Core, Activity, Lifecycle, Navigation, Room, DataStore)", "Copyright The Android Open Source Project", "Apache License 2.0", "developer.android.com/jetpack"),
    Notice("Material Icons", "Copyright Google LLC", "Apache License 2.0", "fonts.google.com/icons"),
    Notice("Kotlin & kotlinx.coroutines", "Copyright JetBrains s.r.o. and contributors", "Apache License 2.0", "github.com/Kotlin/kotlinx.coroutines"),
    Notice("osmdroid", "Copyright osmdroid contributors", "Apache License 2.0", "github.com/osmdroid/osmdroid"),
    Notice("Google Play services – Location", "Copyright Google LLC", "Android Software Development Kit License", "developers.google.com/android/guides/overview")
)

private val DataNotices = listOf(
    Notice("Map data", "© OpenStreetMap contributors", "Open Database License (ODbL) 1.0", "openstreetmap.org/copyright")
)

@Composable
fun ColumnScope.OpenSourceLicensesContent() {
    val tokens = QuickKartThemeTokens.colors
    Text(
        "QuickKart is built with the open-source software and data below. Thank you to their authors.",
        style = MaterialTheme.typography.bodySmall,
        color = tokens.muted
    )
    NoticeGroup("Fonts", FontNotices)
    OflFullText()
    NoticeGroup("Libraries", LibraryNotices)
    NoticeGroup("Map data", DataNotices)
    Text(
        "Apache License 2.0: apache.org/licenses/LICENSE-2.0",
        style = MaterialTheme.typography.labelSmall,
        color = tokens.muted
    )
}

@Composable
private fun NoticeGroup(title: String, notices: List<Notice>) {
    val tokens = QuickKartThemeTokens.colors
    Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.ink, fontWeight = FontWeight.Bold)
    notices.forEach { n ->
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceElevated,
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("licence_${n.name.take(12).lowercase().replace(' ', '_')}")
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(n.name, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                Text(n.copyright, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                Text(n.licence, style = MaterialTheme.typography.labelMedium, color = tokens.ink, fontWeight = FontWeight.Bold)
                Text(n.url, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            }
        }
    }
}

/** The OFL requires its full text to accompany the bundled fonts. */
@Composable
private fun OflFullText() {
    val tokens = QuickKartThemeTokens.colors
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .testTag("ofl_full_text")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "SIL Open Font License 1.1 — full text",
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.ink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Hide licence text" else "Show licence text",
                    tint = tokens.muted
                )
            }
            if (expanded) {
                Text(OFL_TEXT, style = MaterialTheme.typography.bodySmall, color = tokens.ink)
            }
        }
    }
}

private const val OFL_TEXT = """This Font Software is licensed under the SIL Open Font License, Version 1.1.

PREAMBLE
The goals of the Open Font License (OFL) are to stimulate worldwide development of collaborative font projects, to support the font creation efforts of academic and linguistic communities, and to provide a free and open framework in which fonts may be shared and improved in partnership with others.

The OFL allows the licensed fonts to be used, studied, modified and redistributed freely as long as they are not sold by themselves. The fonts, including any derivative works, can be bundled, embedded, redistributed and/or sold with any software provided that any reserved names are not used by derivative works. The fonts and derivatives, however, cannot be released under any other type of license. The requirement for fonts to remain under this license does not apply to any document created using the fonts or their derivatives.

DEFINITIONS
"Font Software" refers to the set of files released by the Copyright Holder(s) under this license and clearly marked as such. This may include source files, build scripts and documentation.
"Reserved Font Name" refers to any names specified as such after the copyright statement(s).
"Original Version" refers to the collection of Font Software components as distributed by the Copyright Holder(s).
"Modified Version" refers to any derivative made by adding to, deleting, or substituting -- in part or in whole -- any of the components of the Original Version, by changing formats or by porting the Font Software to a new environment.
"Author" refers to any designer, engineer, programmer, technical writer or other person who contributed to the Font Software.

PERMISSION & CONDITIONS
Permission is hereby granted, free of charge, to any person obtaining a copy of the Font Software, to use, study, copy, merge, embed, modify, redistribute, and sell modified and unmodified copies of the Font Software, subject to the following conditions:
1) Neither the Font Software nor any of its individual components, in Original or Modified Versions, may be sold by itself.
2) Original or Modified Versions of the Font Software may be bundled, redistributed and/or sold with any software, provided that each copy contains the above copyright notice and this license. These can be included either as stand-alone text files, human-readable headers or in the appropriate machine-readable metadata fields within text or binary files as long as those fields can be easily viewed by the user.
3) No Modified Version of the Font Software may use the Reserved Font Name(s) unless explicit written permission is granted by the corresponding Copyright Holder. This restriction only applies to the primary font name as presented to the users.
4) The name(s) of the Copyright Holder(s) and the Author(s) of the Font Software shall not be used to promote, endorse or advertise any Modified Version, except to acknowledge the contribution(s) of the Copyright Holder(s) and the Author(s) or with their explicit written permission.
5) The Font Software, modified or unmodified, in part or in whole, must be distributed entirely under this license, and must not be distributed under any other license. The requirement for fonts to remain under this license does not apply to any document created using the Font Software.

TERMINATION
This license becomes null and void if any of the above conditions are not met.

DISCLAIMER
THE FONT SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF COPYRIGHT, PATENT, TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, INCLUDING ANY GENERAL, SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF THE USE OR INABILITY TO USE THE FONT SOFTWARE OR FROM OTHER DEALINGS IN THE FONT SOFTWARE."""
