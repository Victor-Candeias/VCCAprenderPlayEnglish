# PE-018 — Feedback de erro e correção educativa

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/19

## Comportamento
- Antes da confirmação, os três cartões A/B/C permanecem visíveis e a resposta correta não é exposta na projeção Compose.
- Após uma confirmação **incorreta**, surge «Resposta errada!» em vermelho contrastante e uma explicação pedagógica com a tradução inglesa correta.
- As duas opções incorretas, incluindo a selecionada, deixam de ser compostas e não aparecem na árvore de acessibilidade. Mantém-se o cartão correto com a letra original, mas desativado.
- Não aparece smile, mensagem de sucesso nem segunda tentativa. «Pergunta seguinte» fica disponível; na última pergunta surge «Concluir».
- A pontuação resulta das escolhas efetivas do domínio; uma resposta errada conta como pergunta respondida, mas não como acerto.
- A próxima pergunta e a mudança de filtros limpam integralmente o feedback de erro.

## Segurança e acessibilidade
O modelo de UI expõe apenas `correctOptionAfterError` quando existe resposta confirmada e incorreta. Nunca expõe `correctChoiceId` no `QuizQuestionPreview`. A mensagem de erro é textual, não depende da cor, e oferece descrição acessível. A opção remanescente está bloqueada para toque, teclado e TalkBack.

## Testes
- JVM: não revelar a opção antes da resposta, revelar apenas após erro, bloquear 30 confirmações repetidas, limpar ao avançar, manter o score real.
- Android instrumentado: cenário `colors.red`, opção A incorreta e B correta; garantir remoção de A/C, aviso, explicação, ausência de smile, B bloqueada, reinício do ecrã na pergunta seguinte.
- CI: `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug`
- QA manual ainda necessário: `./gradlew :app:connectedDebugAndroidTest`, TalkBack e escala de fonte a 200%.

Não envia scores ao host VCCAprender: essa integração pertence à PE-020.
