package com.arukai.uajpspeak.model

import java.util.LinkedHashMap

class Abecadlo {
    private val hol = LinkedHashMap<String, String>()
    private val prh = LinkedHashMap<String, String>()
    private val skl = LinkedHashMap<String, String>()
    private val dzv = LinkedHashMap<String, String>()
    private val spc = LinkedHashMap<String, String>()

    // Romanization maps
    private val romanHol = LinkedHashMap<String, String>()
    private val romanPrh = LinkedHashMap<String, String>()
    private val romanSpc = LinkedHashMap<String, String>()

    init {
        spc["*"] = "ー"
        spc["-"] = "・"
        spc["..."] = "＿＿"
        spc["."] = "。"
        spc[","] = ""
        spc[":"] = "："
        spc[";"] = "；"
        spc["!"] = "！"
        spc["("] = "（"
        spc[")"] = "）"
        spc["?"] = "？"
        spc[" "] = "・"

        hol["а"] = "ア"
        hol["е"] = "エ"
        hol["і"] = "イ"
        hol["о"] = "オ"
        hol["у"] = "ウ"
        hol["ї"] = "イィ"
        hol["є"] = "イェ"
        hol["ю"] = "ユ"
        hol["я"] = "ヤ"

        prh["б"] = "ブ"
        prh["в"] = "ウ"
        prh["г"] = "フ"
        prh["ґ"] = "グ"
        prh["д"] = "ド"
        prh["ж"] = "ジ"
        prh["з"] = "ズ"
        prh["й"] = "イ"
        prh["к"] = "ク"
        prh["л"] = "ル"
        prh["м"] = "ム"
        prh["н"] = "ン"
        prh["п"] = "プ"
        prh["р"] = "ル"
        prh["с"] = "ス"
        prh["т"] = "ト"
        prh["ф"] = "フ"
        prh["х"] = "フ"
        prh["ц"] = "ツ"
        prh["ч"] = "チ"
        prh["ш"] = "シ"
        prh["щ"] = "シチ"

        skl["ба"] = "バ"
        skl["бе"] = "ベ"
        skl["бі"] = "ビ"
        skl["бо"] = "ボ"
        skl["бу"] = "ブ"
        skl["би"] = "ブィ"
        skl["бє"] = "ビェ"
        skl["бю"] = "ビュ"
        skl["бя"] = "ビャ"
        skl["бьо"] = "ビョ"
        skl["бь"] = "ビ"

        skl["ва"] = "ヴァ"
        skl["ве"] = "ヴェ"
        skl["ві"] = "ヴィ"
        skl["во"] = "ヴォ"
        skl["ву"] = "ヴ"
        skl["ви"] = "ウィ"
        skl["вє"] = "ヴェ"
        skl["вю"] = "ヴュ"
        skl["вя"] = "ヴャ"
        skl["вьо"] = "ヴョ"
        skl["вь"] = "ヴ"

        skl["га"] = "ハ"
        skl["ге"] = "ヘ"
        skl["гі"] = "ヒ"
        skl["го"] = "ホ"
        skl["гу"] = "フ"
        skl["ги"] = "ヒ"
        skl["гє"] = "ヒェ"
        skl["гю"] = "ヒュ"
        skl["гя"] = "ヒャ"
        skl["гьо"] = "ヒョ"
        skl["гь"] = "ヒ"

        skl["ґа"] = "ガ"
        skl["ґе"] = "ゲ"
        skl["ґі"] = "ギ"
        skl["ґо"] = "ゴ"
        skl["ґу"] = "グ"
        skl["ґи"] = "グィ"
        skl["ґє"] = "ギェ"
        skl["ґю"] = "ギュ"
        skl["ґя"] = "ギャ"
        skl["ґьо"] = "ギョ"
        skl["ґь"] = "ギ"

        skl["да"] = "ダ"
        skl["де"] = "デ"
        skl["ді"] = "ディ"
        skl["до"] = "ド"
        skl["ду"] = "ドゥ"
        skl["ди"] = "デ"
        skl["дє"] = "ヂェ"
        skl["дю"] = "ヂュ"
        skl["дя"] = "ヂャ"
        skl["дьо"] = "ヂョ"
        skl["дь"] = "ヂ"

        skl["жа"] = "ジァ"
        skl["же"] = "ジェ"
        skl["жі"] = "ジ"
        skl["жо"] = "ジォ"
        skl["жу"] = "ジゥ"
        skl["жи"] = "ジィ"
        skl["жє"] = "ジェ"
        skl["жю"] = "ジュ"
        skl["жя"] = "ジャ"
        skl["жьо"] = "ジョ"
        skl["жь"] = "ジ"

        skl["за"] = "ザ"
        skl["зе"] = "ゼ"
        skl["зі"] = "ジ"
        skl["зо"] = "ゾ"
        skl["зу"] = "ズ"
        skl["зи"] = "ゼィ"
        skl["зє"] = "ジェ"
        skl["зю"] = "ジュ"
        skl["зя"] = "ジャ"
        skl["зьо"] = "ジョ"
        skl["зь"] = "ジ"

        skl["ка"] = "カ"
        skl["ке"] = "ケ"
        skl["кі"] = "キ"
        skl["ко"] = "コ"
        skl["ку"] = "ク"
        skl["ки"] = "ケィ"
        skl["кє"] = "キェ"
        skl["кю"] = "キュ"
        skl["кя"] = "キャ"
        skl["кьо"] = "キョ"
        skl["кь"] = "キ"

        skl["ла"] = "ラ"
        skl["ле"] = "レ"
        skl["лі"] = "リ"
        skl["ло"] = "ロ"
        skl["лу"] = "ル"
        skl["ли"] = "ルィ"
        skl["лє"] = "リェ"
        skl["лю"] = "リュ"
        skl["ля"] = "リャ"
        skl["льо"] = "リョ"
        skl["ль"] = "リ"

        skl["ма"] = "マ"
        skl["ме"] = "メ"
        skl["мі"] = "ミ"
        skl["мо"] = "モ"
        skl["му"] = "ム"
        skl["ми"] = "ムィ"
        skl["мє"] = "ミェ"
        skl["мю"] = "ミュ"
        skl["мя"] = "ミャ"
        skl["мьо"] = "ミョ"
        skl["мь"] = "ミ"

        skl["на"] = "ナ"
        skl["не"] = "ネ"
        skl["ні"] = "ニ"
        skl["но"] = "ノ"
        skl["ну"] = "ヌ"
        skl["ни"] = "ヌィ"
        skl["нє"] = "ニェ"
        skl["ню"] = "ニュ"
        skl["ня"] = "ニャ"
        skl["ньо"] = "ニョ"
        skl["нь"] = "ニ"

        skl["па"] = "パ"
        skl["пе"] = "ペ"
        skl["пі"] = "ピ"
        skl["по"] = "ポ"
        skl["пу"] = "プ"
        skl["пи"] = "プィ"
        skl["пє"] = "ピェ"
        skl["пю"] = "ピュ"
        skl["пя"] = "ピャ"
        skl["пьо"] = "ピョ"
        skl["пь"] = "ピ"

        skl["ра"] = "ラ"
        skl["ре"] = "レ"
        skl["рі"] = "リ"
        skl["ро"] = "ロ"
        skl["ру"] = "ル"
        skl["ри"] = "ルィ"
        skl["рє"] = "リェ"
        skl["рю"] = "リュ"
        skl["ря"] = "リャ"
        skl["рьо"] = "リョ"
        skl["рь"] = "リ"

        skl["са"] = "サ"
        skl["се"] = "セ"
        skl["сі"] = "シ"
        skl["со"] = "ソ"
        skl["су"] = "ス"
        skl["си"] = "スィ"
        skl["сє"] = "シェ"
        skl["сю"] = "シュ"
        skl["ся"] = "シャ"
        skl["сьо"] = "ショ"
        skl["сь"] = "シ"

        skl["та"] = "タ"
        skl["те"] = "テ"
        skl["ті"] = "ティ"
        skl["то"] = "ト"
        skl["ту"] = "トゥ"
        skl["ти"] = "ティ"
        skl["тє"] = "チェ"
        skl["тю"] = "チュ"
        skl["тя"] = "チャ"
        skl["тьо"] = "チョ"
        skl["ть"] = "チ"

        skl["фа"] = "ファ"
        skl["фе"] = "フェ"
        skl["фі"] = "フィ"
        skl["фо"] = "フォ"
        skl["фу"] = "トゥ"
        skl["фи"] = "フィ"
        skl["фє"] = "ヒェ"
        skl["фю"] = "ヒュ"
        skl["фя"] = "ヒャ"
        skl["фьо"] = "ヒョ"
        skl["фь"] = "ヒ"

        skl["ха"] = "ハ"
        skl["хе"] = "ヘ"
        skl["хі"] = "ヒ"
        skl["хо"] = "ホ"
        skl["ху"] = "フ"
        skl["хи"] = "ヒ"
        skl["хє"] = "ヒェ"
        skl["хю"] = "ヒュ"
        skl["хя"] = "ヒャ"
        skl["хьо"] = "ヒョ"
        skl["хь"] = "ヒ"

        skl["ца"] = "ツァ"
        skl["це"] = "ツェ"
        skl["ці"] = "ツィ"
        skl["цо"] = "ツォ"
        skl["цу"] = "ツ"
        skl["ци"] = "ツィ"
        skl["цє"] = "ツェ"
        skl["цю"] = "ツュ"
        skl["ця"] = "ツャ"
        skl["цьо"] = "ツョ"
        skl["ць"] = "チ"

        skl["ча"] = "チァ"
        skl["че"] = "チェ"
        skl["чі"] = "チ"
        skl["чо"] = "チョ"
        skl["чу"] = "チュ"
        skl["чи"] = "チ"
        skl["чє"] = "チェ"
        skl["чю"] = "チュ"
        skl["чя"] = "チャ"
        skl["чьо"] = "チョ"
        skl["чь"] = "チ"

        skl["ша"] = "シァ"
        skl["ше"] = "シェ"
        skl["ші"] = "シ"
        skl["шо"] = "シォ"
        skl["шу"] = "シュ"
        skl["ши"] = "シ"
        skl["шє"] = "シェ"
        skl["шю"] = "シュ"
        skl["шя"] = "シャ"
        skl["шьо"] = "ショ"
        skl["шь"] = "シ"

        skl["ща"] = "シチァ"
        skl["ще"] = "シチェ"
        skl["щі"] = "シチ"
        skl["що"] = "シチォ"
        skl["щу"] = "シチュ"
        skl["щи"] = "シチ"
        skl["щє"] = "シチェ"
        skl["щю"] = "シチュ"
        skl["щя"] = "シチャ"
        skl["щьо"] = "シチョ"
        skl["щь"] = "シチ"

        dzv["дза"] = "ザ"
        dzv["дзі"] = "ジ"
        dzv["дзу"] = "ズ"
        dzv["дзе"] = "ゼ"
        dzv["дзо"] = "ゾ"
        dzv["дзя"] = "ジャ"
        dzv["дзю"] = "ジュ"
        dzv["дзьо"] = "ジョ"

        dzv["джа"] = "ジァ"
        dzv["джі"] = "ジ"
        dzv["джу"] = "ジゥ"
        dzv["дже"] = "ジェ"
        dzv["джо"] = "ジォ"
        dzv["джя"] = "ジャ"
        dzv["джю"] = "ジュ"
        dzv["джьо"] = "ジョ"

        // Initialize romanization maps
        // "*" is handled inline in romanize() to accent the preceding vowel
        romanSpc["-"] = "-"
        romanSpc["..."] = "..."
        romanSpc["."] = "."
        romanSpc[","] = ","
        romanSpc[":"] = ":"
        romanSpc[";"] = ";"
        romanSpc["!"] = "!"
        romanSpc["("] = "("
        romanSpc[")"] = ")"
        romanSpc["?"] = "?"
        romanSpc[" "] = " "

        // Vowels
        romanHol["а"] = "a"
        romanHol["е"] = "e"
        romanHol["і"] = "i"
        romanHol["и"] = "y"
        romanHol["о"] = "o"
        romanHol["у"] = "u"
        romanHol["ї"] = "yi"
        romanHol["є"] = "ye"
        romanHol["ю"] = "yu"
        romanHol["я"] = "ya"

        // Consonants
        romanPrh["б"] = "b"
        romanPrh["в"] = "v"
        romanPrh["г"] = "h"
        romanPrh["ґ"] = "g"
        romanPrh["д"] = "d"
        romanPrh["ж"] = "zh"
        romanPrh["з"] = "z"
        romanPrh["й"] = "y"
        romanPrh["к"] = "k"
        romanPrh["л"] = "l"
        romanPrh["м"] = "m"
        romanPrh["н"] = "n"
        romanPrh["п"] = "p"
        romanPrh["р"] = "r"
        romanPrh["с"] = "s"
        romanPrh["т"] = "t"
        romanPrh["ф"] = "f"
        romanPrh["х"] = "kh"
        romanPrh["ц"] = "ts"
        romanPrh["ч"] = "ch"
        romanPrh["ш"] = "sh"
        romanPrh["щ"] = "shch"
        romanPrh["ь"] = ""
        romanPrh["'"] = ""
    }

    fun convert(str: String): String {
        var s = str.lowercase()

        // povtorennya (repetition)
        var result = ""
        var c: String
        var nc = ""
        for (i in 0 until s.length - 1) {
            c = s[i].toString()
            nc = s[i + 1].toString()
            result += if (prh.containsKey(c) && c != "н" && c == nc) {
                "ッ"
            } else {
                c
            }
        }
        result += nc
        s = result

        // specialni znaky (special characters)
        for ((key, value) in spc) {
            s = s.replace(key, value)
        }

        // ostannya litera (last letter)
        var st = ""
        val parts = s.split("・")
        for (word in parts) {
            if (word.length > 1) {
                var last = word[word.length - 1].toString()
                val prelast = word[word.length - 2].toString()
                if (prh.containsKey(last) && last != "н" && (hol.containsKey(prelast) || prelast == "ー")) {
                    last = "ッ$last"
                }
                st = st + word.substring(0, word.length - 1) + last + "・"
            } else {
                st = "$st$word・"
            }
        }
        st = st.substring(0, st.length - 1)
        s = st

        // dzvinki (voiced sounds)
        for ((key, value) in dzv) {
            s = s.replace(key, value)
        }

        // sklady (syllables)
        for ((key, value) in skl) {
            s = s.replace(key, value)
        }

        // pryholosni (consonants)
        for ((key, value) in prh) {
            s = s.replace(key, value)
        }

        // holosni (vowels)
        for ((key, value) in hol) {
            s = s.replace(key, value)
        }

        // apostrof (apostrophe)
        s = s.replace("'", "")

        return s
    }

    /** Replaces the last plain vowel in [sb] with its acute-accented form. */
    private fun accentLastVowel(sb: StringBuilder) {
        val accents = mapOf('a' to 'á', 'e' to 'é', 'i' to 'í', 'o' to 'ó', 'u' to 'ú', 'y' to 'ý')
        for (i in sb.indices.reversed()) {
            val replacement = accents[sb[i]]
            if (replacement != null) {
                sb.setCharAt(i, replacement)
                return
            }
        }
    }

    fun romanize(str: String): String {
        var s = str.lowercase()
        val result = StringBuilder()

        // Special characters (asterisk excluded — handled in character loop)
        for ((key, value) in romanSpc) {
            s = s.replace(key, value)
        }

        // Process character by character for official Ukrainian National transliteration
        // Based on Resolution No. 55 (2010)
        var i = 0
        while (i < s.length) {
            val currentChar = s[i]
            val prevChar = if (i > 0) s[i - 1] else null
            val nextChar = if (i < s.length - 1) s[i + 1] else null

            // Check if at word beginning (after space, punctuation, apostrophe, or at start)
            val isWordStart = prevChar == null ||
                prevChar in " -.,;:!?()[]{}\"'*/\n\t"

            when (currentChar) {
                // Special case: зг -> zgh (official exception)
                'з' -> {
                    if (nextChar == 'г') {
                        result.append("zgh")
                        i++ // Skip next 'г'
                    } else {
                        result.append("z")
                    }
                }

                // Context-dependent vowels (ye/yi/yu/ya ONLY at word start, otherwise ie/i/iu/ia)
                'є' -> {
                    result.append(if (isWordStart) "ye" else "ie")
                }
                'ї' -> {
                    result.append(if (isWordStart) "yi" else "i")
                }
                'ю' -> {
                    result.append(if (isWordStart) "yu" else "iu")
                }
                'я' -> {
                    result.append(if (isWordStart) "ya" else "ia")
                }

                // й is always "y" in official standard
                'й' -> {
                    result.append("y")
                }

                // Apostrophe and soft sign - omit in official transliteration
                '\'' -> {
                    // Omit apostrophe
                }
                'ь' -> {
                    // Omit soft sign
                }

                // Stress mark — accent the last vowel already written to result
                '*' -> {
                    accentLastVowel(result)
                }

                // Regular consonants
                'б' -> result.append("b")
                'в' -> result.append("v")
                'г' -> result.append("h")
                'ґ' -> result.append("g")
                'д' -> result.append("d")
                'ж' -> result.append("zh")
                'к' -> result.append("k")
                'л' -> result.append("l")
                'м' -> result.append("m")
                'н' -> result.append("n")
                'п' -> result.append("p")
                'р' -> result.append("r")
                'с' -> result.append("s")
                'т' -> result.append("t")
                'ф' -> result.append("f")
                'х' -> result.append("kh")
                'ц' -> result.append("ts")
                'ч' -> result.append("ch")
                'ш' -> result.append("sh")
                'щ' -> result.append("shch")

                // Regular vowels
                'а' -> result.append("a")
                'е' -> result.append("e")
                'и' -> result.append("y")
                'і' -> result.append("i")
                'о' -> result.append("o")
                'у' -> result.append("u")

                // Any other character (numbers, punctuation, etc.)
                else -> result.append(currentChar)
            }

            i++
        }

        return result.toString()
    }
}
