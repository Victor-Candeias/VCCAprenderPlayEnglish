# PE-041 — Compose UI tests dos dois modos

Issue #42 — https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/42

## Implementação

O ficheiro `app/src/androidTest/java/com/vcc/aprender/playenglish/Pe041ComposeUiTest.kt` exerce interações reais:
- pressão curta não confirma, ação de acessibilidade confirma;
- sorriso exclusivo no acerto, mensagem de erro no engano e ocultação de duas opções incorretas;
- próxima pergunta restaura o ecrã e remove feedback da anterior;
- conclusão do baralho e passagem ao ecrã de resultados;
- pesquisa de vocabulário, limpeza de pesquisa, navegação de palavras e retorno;
- simulação de áudio indisponível por preferência local `audio_enabled=false`, botões desativados, texto visível sem qualquer reprodução real.

O workflow `.github/workflows/android-ci.yml` inclui o job `pe041-ui` que corre estes testes em emulador Android API 30 via `reactivecircus/android-emulator-runner@v2`. É obrigatório em PRs para a `main`; o job anterior continua a compilar e testar o APK.

## Comandos locais

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.vcc.aprender.playenglish.Pe041ComposeUiTest
```

Requer emulador/dispositivo Android ativo.

## Limites

A execução no emulador depende da disponibilidade de aceleração KVM no runner Linux. Um motor TTS real não é necessário, uma vez que os testes desativam a preferência de reprodução e não invocam áudio; o comportamento de callbacks do motor Android e de vozes instaladas pertence à PE-039/integração de dispositivo. Validar o resultado do job antes do merge.
