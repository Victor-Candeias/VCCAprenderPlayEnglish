# PE-020 — Integração de resultados reais com VCCAprender

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/21

## Fluxo
1. Depois de escolher categoria/nível, o baralho PE-019 é preparado com perguntas e opções reais.
2. Na primeira confirmação efetiva da questão, o modo plugin inicia `PluginSessionStateHolder.beginQuiz(questionIds)` com todos os IDs da partida. Se o host recusar, a confirmação não progride.
3. Para cada resposta aceite pelo `QuizViewModel.confirmOption`, envia-se `recordAnswer(questionId, correct)`, calculando `correct` a partir da pergunta do domínio e não da letra A/B/C. Eventos repetidos não duplicam o score.
4. `Concluir` só é possível depois da última resposta; `CompleteQuizUseCase` produz `QuizScore` real. O ecrã de resultados mostra `X de Y`.
5. No modo plugin, `Terminar e guardar resultados` valida que os totais e acertos do ecrã coincidem com os eventos da sessão; depois `finishQuizOnce` usa `GameProtocol.createResultIntent` e a Activity executa `setResult(RESULT_OK, intent)` e `finish()` uma única vez.
6. Back/Sair ou sessão incompleta continuam `RESULT_CANCELED`. O modo APK independente não possui callback de host e conserva apenas o resumo local.
7. Pronúncia não regista respostas nem score; não existe envio de dados pessoais adicional.

## Segurança
- O SDK v1 e a verificação do host/assinatura/identidade não foram alterados.
- O holder mantém os IDs imutáveis de sessão/perfil da request original.
- Alterar categoria/nível num quiz alojado após a primeira resposta é bloqueado para evitar um deck diferente do guardado no host.
- Recriação do processo com estado de quiz divergente impede a conclusão; nunca se envia um score fabricado.
- Não se faz persistência remota direta pelo plugin: o host é o responsável por guardar a sessão ao receber um RESULT_OK válido.

## Testes
- `QuizProgressTest` testa contagens derivadas de respostas únicas e score antes da conclusão.
- Testes existentes cobrem duplicados, cancelamento, intents SDK v1, rejeição de replay e score 0/N.
- CI: `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug`
- QA Android ainda exigido: executar `connectedDebugAndroidTest` e E2E com a VCCAprender real (mesma assinatura e chamada `startActivityForResult` / Activity Result API).
