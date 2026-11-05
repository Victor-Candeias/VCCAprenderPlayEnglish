package com.vcc.aprender.playenglish.domain.repository

import com.vcc.aprender.playenglish.domain.model.OfflineVocabularyCatalog

/** Read-only port; its implementation must load from packaged content only. */
interface VocabularyContentRepository {
    fun loadCatalog(): OfflineVocabularyCatalog
}
