# PE-040 — Testes unitários de catálogo, baralho e pontuação

Issue #41: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/41

## Cobertura adicionada
- Catálogo vazio não inventa pontuações; entradas e respostas inválidas não são aceites
- Acerto 0/total e total/total; uma tentativa por questão; cancelamento terminal
- Shuffle determinístico para seeds fixas e baralhos de 1, 2, 3, 9 e 100 perguntas
- IDs únicos, resposta correta preservada, equilíbrio A/B/C e rejeição de opções duplicadas
- Snapshots adulterados rejeitados; restauro não duplica respostas
- Protocolo v1: limites e versões inválidas, sessões falsas, pontuações divergentes
- Envio de resultado ao host uma única vez

## Execução
```sh
./gradlew :game-plugin-sdk:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

Estes testes utilizam JUnit JVM e são executados pelo workflow existente em `app:testDebugUnitTest`. A CI falha quando algum teste falha.

## Nota sobre CI
A `main` de origem falhava na compilação da PE-039 devido a `Voice.KEY_FEATURE_NOT_INSTALLED`. Esta branch inclui a substituição pontual por `TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED` para que os testes PE-040 possam ser executados. A correção já tinha sido publicada separadamente em `fix/PE-039-tts-voice-feature-constant`.

A PE-040 não substitui testes instrumentados de UI nem o teste real de integração com o VCCAprender.
