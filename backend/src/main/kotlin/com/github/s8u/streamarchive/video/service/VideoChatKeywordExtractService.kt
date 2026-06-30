package com.github.s8u.streamarchive.video.service

import org.apache.lucene.analysis.ko.KoreanTokenizer
import org.apache.lucene.analysis.ko.POS
import org.apache.lucene.analysis.ko.tokenattributes.PartOfSpeechAttribute
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.AttributeFactory
import org.springframework.stereotype.Service

/**
 * 채팅 메시지에서 키워드를 추출한다.
 *
 * Nori(한국어 형태소 분석)로 명사·어근만 남기고 조사·어미·기호를 버린다.
 * 외부 조회 없이 입력 문자열만으로 동작하는 순수 도메인 서비스다.
 */
@Service
class VideoChatKeywordExtractService {

    /**
     * 메시지에서 키워드 목록을 뽑는다.
     *
     * 같은 메시지 안에서 중복 키워드는 제거한다.
     * 이모티콘 코드는 분석 대상에서 제외한다.
     *
     * @param placeholders 그 메시지에 쓰인 이모티콘 코드(플랫폼이 미리 분리해둔 것). 형식을 추측하지 않고 이 목록으로만 제거한다.
     */
    fun extract(message: String, placeholders: Collection<String> = emptyList()): List<String> {
        val cleaned = removeEmoticons(message, placeholders)
        if (cleaned.isBlank()) return emptyList()

        val keywords = LinkedHashSet<String>()

        // 복합명사를 쪼개지 않는다(NONE). 검색 색인과 달리 채팅 키워드는
        // "대머리"·"청소기"처럼 통째로 봐야 라벨이 자연스럽다 (분해하면 "대"+"머리"가 된다).
        val tokenizer = KoreanTokenizer(
            AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
            null,
            KoreanTokenizer.DecompoundMode.NONE,
            false
        )
        tokenizer.use {
            tokenizer.setReader(cleaned.reader())
            val termAttribute = tokenizer.addAttribute(CharTermAttribute::class.java)
            val posAttribute = tokenizer.addAttribute(PartOfSpeechAttribute::class.java)

            tokenizer.reset()
            while (tokenizer.incrementToken()) {
                val tag = posAttribute.leftPOS ?: continue
                if (tag !in MEANINGFUL_TAGS) continue

                val term = termAttribute.toString()
                if (term.length < MIN_KEYWORD_LENGTH) continue

                keywords.add(term)
            }
            tokenizer.end()
        }

        return keywords.toList()
    }

    /**
     * 메시지에서 이모티콘 코드를 걷어내고 공백을 정리한다.
     *
     * 라벨로 보여줄 원문을 다듬을 때도 쓴다.
     * 플랫폼마다 이모티콘 코드 형식이 제각각이라(`{:...:}` / `/.../` / 평문 단어) 정규식으로 추측하지 않는다.
     * 플랫폼이 분리해둔 실제 placeholder 목록으로만 제거한다.
     */
    fun removeEmoticons(message: String, placeholders: Collection<String>): String {
        var result = message
        for (placeholder in placeholders) {
            if (placeholder.isNotEmpty()) {
                result = result.replace(placeholder, " ")
            }
        }
        return result.trim().replace(WHITESPACE_PATTERN, " ")
    }

    companion object {
        /** 연속 공백을 하나로 줄인다. */
        private val WHITESPACE_PATTERN = Regex("""\s+""")

        /** 키워드로 의미가 있는 품사 (일반/고유 명사, 동사, 형용사, 어근, 외국어). */
        private val MEANINGFUL_TAGS = setOf(
            POS.Tag.NNG, // 일반 명사
            POS.Tag.NNP, // 고유 명사
            POS.Tag.VV,  // 동사
            POS.Tag.VA,  // 형용사
            POS.Tag.XR,  // 어근
            POS.Tag.SL   // 외국어 (영어 등)
        )

        /** 한 글자 키워드는 변별력이 낮아 버린다. */
        private const val MIN_KEYWORD_LENGTH = 2
    }

}
