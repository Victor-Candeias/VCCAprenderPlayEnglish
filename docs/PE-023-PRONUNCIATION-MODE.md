# PE-023 — Modo Como se diz em inglês?

Issue #24. Ecrã ligado ao catálogo offline PT-PT / EN-GB filtrado por idade e categoria.

- Seleção: PlayEnglishNavHost fornece selectedContent.words ao PronunciationScreen.
- Apresentação: texto em português, tradução em inglês e ícone ilustrativo por categoria.
- Reprodução e replay: apenas word.audioText do catálogo com linguagem en-GB; nunca o texto português.
- Exclusão: ambos os botões de reprodução usam SpeechTurnGate da PE-022. Durante áudio, os controlos e navegação estão bloqueados.
- Navegação sem ciclos: Anterior e Seguinte param nos extremos; índice mantido em rotação.
- Sem dados disponíveis: mostrar instrução, bloquear reprodução.
- Voz indisponível: preservar indicação de instalação da voz do Android.
- Sem score, onAnswerRecorded, RESULT_OK ou conclusão do host na atividade de pronúncia.

Testes JVM: PronunciationNavigationTest. Testes instrumentados: Pe023PronunciationNavigationTest. CI compila androidTest mas não executa em dispositivo.

Comandos: ./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug

Dependência de integração: PE-022 está incorporada na branch feature/PE-021-english-tts mas ainda não na main; manter PR encadeado até à integração da dependência.
