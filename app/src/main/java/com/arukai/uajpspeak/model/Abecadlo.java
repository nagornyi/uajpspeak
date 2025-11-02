package com.arukai.uajpspeak.model;

import java.util.LinkedHashMap;

public class Abecadlo {
    LinkedHashMap<String, String> hol;
    LinkedHashMap<String, String> prh;
    LinkedHashMap<String, String> skl;
    LinkedHashMap<String, String> dzv;
    LinkedHashMap<String, String> spc;

    public Abecadlo() {
        hol = new LinkedHashMap<>();
        prh = new LinkedHashMap<>();
        skl = new LinkedHashMap<>();
        dzv = new LinkedHashMap<>();
        spc = new LinkedHashMap<>();

        spc.put("*", "ー");
        spc.put("-", "・");
        spc.put("...", "＿＿");
        spc.put(".", "。");
        spc.put(",", "");
        spc.put(":", "：");
        spc.put(";", "；");
        spc.put("!", "！");
        spc.put("(", "（");
        spc.put(")", "）");
        spc.put("?", "？");
        spc.put(" ", "・");

        hol.put("а", "ア");
        hol.put("е", "エ");
        hol.put("і", "イ");
        hol.put("о", "オ");
        hol.put("у", "ウ");
        hol.put("ї", "イィ");
        hol.put("є", "イェ");
        hol.put("ю", "ユ");
        hol.put("я", "ヤ");

        prh.put("б", "ブ");
        prh.put("в", "ウ");
        prh.put("г", "フ");
        prh.put("ґ", "グ");
        prh.put("д", "ド");
        prh.put("ж", "ジ");
        prh.put("з", "ズ");
        prh.put("й", "イ");
        prh.put("к", "ク");
        prh.put("л", "ル");
        prh.put("м", "ム");
        prh.put("н", "ン");
        prh.put("п", "プ");
        prh.put("р", "ル");
        prh.put("с", "ス");
        prh.put("т", "ト");
        prh.put("ф", "フ");
        prh.put("х", "フ");
        prh.put("ц", "ツ");
        prh.put("ч", "チ");
        prh.put("ш", "シ");
        prh.put("щ", "シチ");

        skl.put("ба", "バ");
        skl.put("бе", "ベ");
        skl.put("бі", "ビ");
        skl.put("бо", "ボ");
        skl.put("бу", "ブ");
        skl.put("би", "ブィ");
        skl.put("бє", "ビェ");
        skl.put("бю", "ビュ");
        skl.put("бя", "ビャ");
        skl.put("бьо", "ビョ");
        skl.put("бь", "ビ");

        skl.put("ва", "ヴァ");
        skl.put("ве", "ヴェ");
        skl.put("ві", "ヴィ");
        skl.put("во", "ヴォ");
        skl.put("ву", "ヴ");
        skl.put("ви", "ウィ");
        skl.put("вє", "ヴェ");
        skl.put("вю", "ヴュ");
        skl.put("вя", "ヴャ");
        skl.put("вьо", "ヴョ");
        skl.put("вь", "ヴ");

        skl.put("га", "ハ");
        skl.put("ге", "ヘ");
        skl.put("гі", "ヒ");
        skl.put("го", "ホ");
        skl.put("гу", "フ");
        skl.put("ги", "ヒ");
        skl.put("гє", "ヒェ");
        skl.put("гю", "ヒュ");
        skl.put("гя", "ヒャ");
        skl.put("гьо", "ヒョ");
        skl.put("гь", "ヒ");

        skl.put("ґа", "ガ");
        skl.put("ґе", "ゲ");
        skl.put("ґі", "ギ");
        skl.put("ґо", "ゴ");
        skl.put("ґу", "グ");
        skl.put("ґи", "グィ");
        skl.put("ґє", "ギェ");
        skl.put("ґю", "ギュ");
        skl.put("ґя", "ギャ");
        skl.put("ґьо", "ギョ");
        skl.put("ґь", "ギ");

        skl.put("да", "ダ");
        skl.put("де", "デ");
        skl.put("ді", "ディ");
        skl.put("до", "ド");
        skl.put("ду", "ドゥ");
        skl.put("ди", "デ");
        skl.put("дє", "ヂェ");
        skl.put("дю", "ヂュ");
        skl.put("дя", "ヂャ");
        skl.put("дьо", "ヂョ");
        skl.put("дь", "ヂ");

        skl.put("жа", "ジァ");
        skl.put("же", "ジェ");
        skl.put("жі", "ジ");
        skl.put("жо", "ジォ");
        skl.put("жу", "ジゥ");
        skl.put("жи", "ジィ");
        skl.put("жє", "ジェ");
        skl.put("жю", "ジュ");
        skl.put("жя", "ジャ");
        skl.put("жьо", "ジョ");
        skl.put("жь", "ジ");

        skl.put("за", "ザ");
        skl.put("зе", "ゼ");
        skl.put("зі", "ジ");
        skl.put("зо", "ゾ");
        skl.put("зу", "ズ");
        skl.put("зи", "ゼィ");
        skl.put("зє", "ジェ");
        skl.put("зю", "ジュ");
        skl.put("зя", "ジャ");
        skl.put("зьо", "ジョ");
        skl.put("зь", "ジ");

        skl.put("ка", "カ");
        skl.put("ке", "ケ");
        skl.put("кі", "キ");
        skl.put("ко", "コ");
        skl.put("ку", "ク");
        skl.put("ки", "ケィ");
        skl.put("кє", "キェ");
        skl.put("кю", "キュ");
        skl.put("кя", "キャ");
        skl.put("кьо", "キョ");
        skl.put("кь", "キ");

        skl.put("ла", "ラ");
        skl.put("ле", "レ");
        skl.put("лі", "リ");
        skl.put("ло", "ロ");
        skl.put("лу", "ル");
        skl.put("ли", "ルィ");
        skl.put("лє", "リェ");
        skl.put("лю", "リュ");
        skl.put("ля", "リャ");
        skl.put("льо", "リョ");
        skl.put("ль", "リ");

        skl.put("ма", "マ");
        skl.put("ме", "メ");
        skl.put("мі", "ミ");
        skl.put("мо", "モ");
        skl.put("му", "ム");
        skl.put("ми", "ムィ");
        skl.put("мє", "ミェ");
        skl.put("мю", "ミュ");
        skl.put("мя", "ミャ");
        skl.put("мьо", "ミョ");
        skl.put("мь", "ミ");

        skl.put("на", "ナ");
        skl.put("не", "ネ");
        skl.put("ні", "ニ");
        skl.put("но", "ノ");
        skl.put("ну", "ヌ");
        skl.put("ни", "ヌィ");
        skl.put("нє", "ニェ");
        skl.put("ню", "ニュ");
        skl.put("ня", "ニャ");
        skl.put("ньо", "ニョ");
        skl.put("нь", "ニ");

        skl.put("па", "パ");
        skl.put("пе", "ペ");
        skl.put("пі", "ピ");
        skl.put("по", "ポ");
        skl.put("пу", "プ");
        skl.put("пи", "プィ");
        skl.put("пє", "ピェ");
        skl.put("пю", "ピュ");
        skl.put("пя", "ピャ");
        skl.put("пьо", "ピョ");
        skl.put("пь", "ピ");

        skl.put("ра", "ラ");
        skl.put("ре", "レ");
        skl.put("рі", "リ");
        skl.put("ро", "ロ");
        skl.put("ру", "ル");
        skl.put("ри", "ルィ");
        skl.put("рє", "リェ");
        skl.put("рю", "リュ");
        skl.put("ря", "リャ");
        skl.put("рьо", "リョ");
        skl.put("рь", "リ");

        skl.put("са", "サ");
        skl.put("се", "セ");
        skl.put("сі", "シ");
        skl.put("со", "ソ");
        skl.put("су", "ス");
        skl.put("си", "スィ");
        skl.put("сє", "シェ");
        skl.put("сю", "シュ");
        skl.put("ся", "シャ");
        skl.put("сьо", "ショ");
        skl.put("сь", "シ");

        skl.put("та", "タ");
        skl.put("те", "テ");
        skl.put("ті", "ティ");
        skl.put("то", "ト");
        skl.put("ту", "トゥ");
        skl.put("ти", "ティ");
        skl.put("тє", "チェ");
        skl.put("тю", "チュ");
        skl.put("тя", "チャ");
        skl.put("тьо", "チョ");
        skl.put("ть", "チ");

        skl.put("фа", "ファ");
        skl.put("фе", "フェ");
        skl.put("фі", "フィ");
        skl.put("фо", "フォ");
        skl.put("фу", "トゥ");
        skl.put("фи", "フィ");
        skl.put("фє", "ヒェ");
        skl.put("фю", "ヒュ");
        skl.put("фя", "ヒャ");
        skl.put("фьо", "ヒョ");
        skl.put("фь", "ヒ");

        skl.put("ха", "ハ");
        skl.put("хе", "ヘ");
        skl.put("хі", "ヒ");
        skl.put("хо", "ホ");
        skl.put("ху", "フ");
        skl.put("хи", "ヒ");
        skl.put("хє", "ヒェ");
        skl.put("хю", "ヒュ");
        skl.put("хя", "ヒャ");
        skl.put("хьо", "ヒョ");
        skl.put("хь", "ヒ");

        skl.put("ца", "ツァ");
        skl.put("це", "ツェ");
        skl.put("ці", "ツィ");
        skl.put("цо", "ツォ");
        skl.put("цу", "ツ");
        skl.put("ци", "ツィ");
        skl.put("цє", "ツェ");
        skl.put("цю", "ツュ");
        skl.put("ця", "ツャ");
        skl.put("цьо", "ツョ");
        skl.put("ць", "チ");

        skl.put("ча", "チァ");
        skl.put("че", "チェ");
        skl.put("чі", "チ");
        skl.put("чо", "チョ");
        skl.put("чу", "チュ");
        skl.put("чи", "チ");
        skl.put("чє", "チェ");
        skl.put("чю", "チュ");
        skl.put("чя", "チャ");
        skl.put("чьо", "チョ");
        skl.put("чь", "チ");

        skl.put("ша", "シァ");
        skl.put("ше", "シェ");
        skl.put("ші", "シ");
        skl.put("шо", "シォ");
        skl.put("шу", "シュ");
        skl.put("ши", "シ");
        skl.put("шє", "シェ");
        skl.put("шю", "シュ");
        skl.put("шя", "シャ");
        skl.put("шьо", "ショ");
        skl.put("шь", "シ");

        skl.put("ща", "シチァ");
        skl.put("ще", "シチェ");
        skl.put("щі", "シチ");
        skl.put("що", "シチォ");
        skl.put("щу", "シチュ");
        skl.put("щи", "シチ");
        skl.put("щє", "シチェ");
        skl.put("щю", "シチュ");
        skl.put("щя", "シチャ");
        skl.put("щьо", "シチョ");
        skl.put("щь", "シチ");

        dzv.put("дза", "ザ");
        dzv.put("дзі", "ジ");
        dzv.put("дзу", "ズ");
        dzv.put("дзе", "ゼ");
        dzv.put("дзо", "ゾ");
        dzv.put("дзя", "ジャ");
        dzv.put("дзю", "ジュ");
        dzv.put("дзьо", "ジョ");

        dzv.put("джа", "ジァ");
        dzv.put("джі", "ジ");
        dzv.put("джу", "ジゥ");
        dzv.put("дже", "ジェ");
        dzv.put("джо", "ジォ");
        dzv.put("джя", "ジャ");
        dzv.put("джю", "ジュ");
        dzv.put("джьо", "ジョ");
    }

    public String convert(String str){
        str = str.toLowerCase();
        //povtorennya
        String s="", c="", nc="";
        for (int i = 0; i < str.length()-1; i++){
            c = String.valueOf(str.charAt(i));
            nc = String.valueOf(str.charAt(i + 1));
            if(prh.containsKey(c) && !c.equals("н") && c.equals(nc)) c="ッ";
            s+=c;
        }
        s+=nc;
        //specialni znaky
        for (LinkedHashMap.Entry<String, String> entry : spc.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }
        //ostannya litera
        String st = "";
        String[] parts = s.split("・");
        for (String word : parts) {
            if(word.length() > 1) {
                String last = String.valueOf(word.charAt(word.length() - 1));
                String prelast = String.valueOf(word.charAt(word.length() - 2));
                if (prh.containsKey(last) && !last.equals("н") && (hol.containsKey(prelast) || prelast.equals("ー")))
                    last = "ッ" + last;
                st = st + word.substring(0, word.length() - 1) + last + "・";
            } else {
                st = st + word + "・";
            }
        }
        st = st.substring(0, st.length() - 1);
        s = st;
        //dzvinki
        for (LinkedHashMap.Entry<String, String> entry : dzv.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }
        //sklady
        for (LinkedHashMap.Entry<String, String> entry : skl.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }
        //pryholosni
        for (LinkedHashMap.Entry<String, String> entry : prh.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }
        //holosni
        for (LinkedHashMap.Entry<String, String> entry : hol.entrySet()) {
            s = s.replace(entry.getKey(), entry.getValue());
        }
        //apostrof
        s = s.replace("'", "");
        return s;
    }
}
