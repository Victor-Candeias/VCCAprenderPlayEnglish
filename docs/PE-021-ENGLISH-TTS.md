# PE-021 — Android TTS en-GB (voz britânica offline)

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/22

## Implementação
- `AndroidEnglishPronunciationOutput` implementa a porta `PronunciationOutput` já existente.
- Inicializa `TextToSpeech` de forma assíncrona com o contexto da aplicação, `Locale.UK` e velocidade `0.85f`.
- Exige `isLanguageAvailable(Locale.UK) >= LANG_COUNTRY_AVAILABLE` e confirma que a voz efetivamente selecionada é **en-GB** e não exige rede.
- Se a voz não existir, apresenta informação PT-PT para instalar o pacote inglês (Reino Unido), sem fallback en-US/PT.
- O ecrã usa um texto demonstrativo «Good morning»; reproduções com idioma diferente de en-GB são recusadas pela porta concreta.
- O motor liberta recursos no `onDispose` e não é partilhado com o ecrã de quiz.
- A secção `<queries>` do manifesto permite descobrir motores TTS no Android 11+.

## Testes e validação
- JVM: `EnglishVoicePolicyTest` cobre locale en-GB, rejeição en-US/PT, rejeição de voz online, disponibilidade e ritmo.
- CI: `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug`.
- Verificação manual em dispositivo: voz en-GB instalada, voz em falta, dispositivo offline, arranque/reentrada no ecrã, saída durante áudio, TalkBack.

## Fora do âmbito
A PE-022 formaliza exclusão mútua de múltiplos controlos e tratamento de callbacks; a PE-023 disponibiliza o catálogo PT→EN, navegação e replay. A pronúncia não produz resultados de quiz nem `RESULT_OK`.
